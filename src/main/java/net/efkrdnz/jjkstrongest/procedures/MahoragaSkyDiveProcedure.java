package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

import java.util.List;

public class MahoragaSkyDiveProcedure {
	// jumps up, then dives to slam and knock hard
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		int dive = entity.getPersistentData().getInt("maho_dive_started");
		int hit = entity.getPersistentData().getInt("maho_dive_hit");
		// start jump chase
		if (t == 1) {
			entity.getPersistentData().putInt("maho_dive_started", 0);
			entity.getPersistentData().putInt("maho_dive_hit", 0);
			if (entity instanceof MahoragaEntity _m)
				_m.setAnimation("jump");
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			double dirx = entity.getLookAngle().x;
			double dirz = entity.getLookAngle().z;
			if (len > 0.001) {
				dirx = dx / len;
				dirz = dz / len;
			}
			entity.getPersistentData().putDouble("maho_dive_dirx", dirx);
			entity.getPersistentData().putDouble("maho_dive_dirz", dirz);
			entity.setDeltaMovement(dirx * 0.60, 1.25, dirz * 0.60);
			entity.hurtMarked = true;
		}
		// homing while rising
		if (dive == 0) {
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			double dirx = entity.getPersistentData().getDouble("maho_dive_dirx");
			double dirz = entity.getPersistentData().getDouble("maho_dive_dirz");
			if (len > 0.001) {
				dirx = dx / len;
				dirz = dz / len;
				entity.getPersistentData().putDouble("maho_dive_dirx", dirx);
				entity.getPersistentData().putDouble("maho_dive_dirz", dirz);
			}
			Vec3 cur = entity.getDeltaMovement();
			double steer = 0.22;
			double vy = cur.y;
			if (entity.getY() < target.getY() + 10) {
				vy = Math.min(1.35, vy + 0.06);
			}
			entity.setDeltaMovement(cur.x * 0.75 + dirx * steer, vy, cur.z * 0.75 + dirz * steer);
			entity.hurtMarked = true;
			// start dive when high enough or after time
			if (t >= 12 && (entity.getY() >= target.getY() + 12 || t >= 28)) {
				entity.getPersistentData().putInt("maho_dive_started", 1);
				if (entity instanceof MahoragaEntity _m)
					_m.setAnimation("attack_overhead");
				entity.setDeltaMovement(dirx * 1.15, -2.75, dirz * 1.15);
				entity.hurtMarked = true;
			}
		}
		// dive phase: try to hit target midair, then slam on ground
		if (entity.getPersistentData().getInt("maho_dive_started") == 1) {
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			double dirx = entity.getPersistentData().getDouble("maho_dive_dirx");
			double dirz = entity.getPersistentData().getDouble("maho_dive_dirz");
			if (len > 0.001) {
				dirx = dx / len;
				dirz = dz / len;
			}
			// small steering toward target during dive
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(cur.x * 0.85 + dirx * 0.12, cur.y, cur.z * 0.85 + dirz * 0.12);
			entity.hurtMarked = true;
			// midair hit (one time)
			if (hit == 0 && entity.distanceTo(target) <= 2.9) {
				entity.getPersistentData().putInt("maho_dive_hit", 1);
				target.invulnerableTime = 0;
				target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 24.0F);
				// spike them down hard
				target.setDeltaMovement(dirx * 0.25, -2.9, dirz * 0.25);
				target.hurtMarked = true;
				// keep fall damage disabled briefly so the slam is the real punish
				target.getPersistentData().putInt("maho_launch_ticks", 40);
			}
			// ground impact aoe
			if (entity.onGround()) {
				double r = 4.6;
				AABB box = new AABB(entity.blockPosition()).inflate(r);
				List<Entity> list = entity.level().getEntities(entity, box, e -> e instanceof LivingEntity && e != entity);
				for (Entity e : list) {
					LivingEntity le = (LivingEntity) e;
					le.invulnerableTime = 0;
					le.hurt(le.damageSources().mobAttack((LivingEntity) entity), 18.0F);
					double kx = le.getX() - entity.getX();
					double kz = le.getZ() - entity.getZ();
					double klen = Math.sqrt(kx * kx + kz * kz);
					if (klen > 0.001) {
						le.setDeltaMovement(le.getDeltaMovement().add(kx / klen * 2.2, 0.85, kz / klen * 2.2));
					} else {
						le.setDeltaMovement(le.getDeltaMovement().add(0, 0.85, 0));
					}
					le.hurtMarked = true;
				}
				MahoragaBreakBlocksByHardnessProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), 3);
				entity.getPersistentData().putString("maho_state", "TARGETING");
				entity.getPersistentData().putInt("maho_t", 0);
				entity.getPersistentData().putInt("maho_cd_global", 16);
				entity.getPersistentData().putInt("maho_dive_started", 0);
				entity.getPersistentData().putInt("maho_dive_hit", 0);
				return;
			}
		}
		// failsafe
		if (t >= 90) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_global", 14);
			entity.getPersistentData().putInt("maho_dive_started", 0);
			entity.getPersistentData().putInt("maho_dive_hit", 0);
		}
	}
}
