package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaStrongJumpProcedure {
	// strong jump with one midair dash to escape stuck spots
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		if (t == 1) {
			if (entity instanceof MahoragaEntity)
				((MahoragaEntity) entity).setAnimation("jump");
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			double dirx = entity.getLookAngle().x;
			double dirz = entity.getLookAngle().z;
			if (len > 0.001) {
				dirx = dx / len;
				dirz = dz / len;
			}
			// store direction for midair dash
			entity.getPersistentData().putDouble("maho_sj_dirx", dirx);
			entity.getPersistentData().putDouble("maho_sj_dirz", dirz);
			entity.getPersistentData().putInt("maho_sj_dashed", 0);
			// initial hop (not too much forward yet)
			double dy = (target.getY() + target.getBbHeight() * 0.5) - (entity.getY() + entity.getBbHeight() * 0.5);
			double yboost = 0.95 + Math.min(0.95, Math.max(0, dy) / 10.0);
			entity.setDeltaMovement(dirx * 0.45, yboost, dirz * 0.45);
			entity.hurtMarked = true;
			// tunnel break while moving
			entity.getPersistentData().putInt("maho_break_ticks", 10);
			entity.getPersistentData().putInt("maho_cd_strongjump", 70);
			// reset stuck counter so it doesn't instantly re-trigger
			entity.getPersistentData().putInt("maho_stuck_ticks", 0);
		}
		// midair dash once (makes it clear the spot)
		if (t >= 5 && entity.getPersistentData().getInt("maho_sj_dashed") == 0 && !entity.onGround()) {
			double dirx = entity.getPersistentData().getDouble("maho_sj_dirx");
			double dirz = entity.getPersistentData().getDouble("maho_sj_dirz");
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(cur.x * 0.25 + dirx * 1.25, Math.max(cur.y, 0.15), cur.z * 0.25 + dirz * 1.25);
			entity.hurtMarked = true;
			entity.getPersistentData().putInt("maho_sj_dashed", 1);
		}
		if (t >= 18) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_global", 10);
			// cleanup
			entity.getPersistentData().putInt("maho_sj_dashed", 0);
		}
	}
}
