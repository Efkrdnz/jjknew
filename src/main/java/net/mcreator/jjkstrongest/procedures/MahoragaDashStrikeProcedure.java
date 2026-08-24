package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaDashStrikeProcedure {
	// burst forward strike; combos into BARRAGE if hit lands and target is still close
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t");
		t++;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		MahoragaFaceTargetProcedure.execute(entity, target);
		if (t == 1) {
			if (entity instanceof MahoragaEntity)
				((MahoragaEntity) entity).setAnimation("run");
			entity.getPersistentData().putInt("maho_dash_hit", 0);
		}
		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len > 0.001) {
			Vec3 push = new Vec3(dx / len * 1.35, entity.getDeltaMovement().y * 0.2 + 0.05, dz / len * 1.35);
			entity.setDeltaMovement(push);
			entity.hurtMarked = true;
		}
		if (t == 4) {
			if (entity.distanceTo(target) <= 2.8) {
				target.invulnerableTime = 0;
				target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 22.0F);
				double kbl = len;
				if (kbl > 0.001) {
					target.setDeltaMovement(target.getDeltaMovement().add(dx / kbl * 1.6, 0.25, dz / kbl * 1.6));
				}
				entity.getPersistentData().putInt("maho_dash_hit", 1);
			}
		}
		if (t >= 12) {
			entity.getPersistentData().putInt("maho_cd_dash", 60);
			// combo: if hit landed and target is still within barrage range, chain into BARRAGE
			boolean hitLanded = entity.getPersistentData().getInt("maho_dash_hit") == 1;
			boolean canChainBarrage = entity.getPersistentData().getInt("maho_cd_barrage") <= 0
					&& entity.distanceTo(target) <= 3.5;
			if (hitLanded && canChainBarrage && entity.level().getRandom().nextDouble() < 0.45) {
				entity.getPersistentData().putString("maho_state", "BARRAGE");
			} else {
				entity.getPersistentData().putString("maho_state", "TARGETING");
				entity.getPersistentData().putInt("maho_cd_global", 10);
			}
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_dash_hit", 0);
		}
	}
}
