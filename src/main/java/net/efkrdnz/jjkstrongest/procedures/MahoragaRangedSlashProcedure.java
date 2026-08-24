package net.efkrdnz.jjkstrongest.procedures;


import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaRangedSlashProcedure {
	// ranged hit + spawn dismantle vfx (uses existing SpawnDismantleSlashProcedure)
	public static void execute(ServerLevel level, Entity entity, LivingEntity target) {
		if (level == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		// start windup
		if (t == 1) {
			if (entity instanceof MahoragaEntity m) {
				m.setAnimation("attack_normal");
			}
			entity.getPersistentData().putInt("maho_cd_global", 20);
		}
		// fire moment
		if (t == 8) {
			// only slash if target is valid + line of sight
			if (!target.isAlive()) {
				end(entity);
				return;
			}
			if (entity instanceof Mob mob) {
				if (!mob.getSensing().hasLineOfSight(target)) {
					end(entity);
					return;
				}
			}
			Vec3 start = entity.position().add(0, 1.4, 0);
			Vec3 aim = target.position().add(0, target.getBbHeight() * 0.6, 0);
			Vec3 dir = aim.subtract(start);
			if (dir.lengthSqr() < 1.0e-6)
				dir = entity.getLookAngle();
			dir = dir.normalize();
			// optional: stop slash if block is between (keeps it fair)
			double range = 22.0;
			Vec3 end = start.add(dir.scale(range));
			BlockHitResult bhr = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
			if (bhr.getType() != HitResult.Type.MISS) {
				// block in front -> cancel attack (or you can still hit if you want)
				end(entity);
				return;
			}
			// do damage (hitscan)
			float dmg = 10.0F;
			target.hurt(level.damageSources().mobAttack((LivingEntity) entity), dmg);
			target.setDeltaMovement(target.getDeltaMovement().add(dir.x * 1.2, 0.15, dir.z * 1.2));
			target.hurtMarked = true;
			// spawn your dismantle visual at target (unchanged procedure)
			SpawnDismantleSlashProcedure.execute(level, entity, target, dir, 1.0);
			//entity.getPersistentData().putInt("maho_cd_slash", 120);
			entity.getPersistentData().putInt("maho_cd_cannon", 40); // same as cannon
			entity.getPersistentData().putInt("maho_cd_global", 6);
		}
		// end
		if (t >= 18) {
			end(entity);
		}
	}

	private static void end(Entity entity) {
		entity.getPersistentData().putString("maho_state", "TARGETING");
		entity.getPersistentData().putInt("maho_t", 0);
	}
}
