package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaDodgeProcedure {
	// Reactive sidestep when hit — alternates L/R each time so it feels intentional,
	// not random. Brief recovery window before re-engaging.
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		MahoragaFaceTargetProcedure.execute(entity, target);

		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double dist2d = Math.sqrt(dx * dx + dz * dz);

		// perpendicular unit vector (left of approach direction)
		double perpX = (dist2d > 0.001) ? -dz / dist2d : 1.0;
		double perpZ = (dist2d > 0.001) ?  dx / dist2d : 0.0;

		if (t == 1) {
			if (entity instanceof MahoragaEntity m)
				m.setAnimation("run");
			// alternate L/R so consecutive dodges go opposite directions
			int last = entity.getPersistentData().getInt("maho_last_dodge_dir");
			int dir = (last >= 0) ? -1 : 1; // flip from last time
			entity.getPersistentData().putInt("maho_last_dodge_dir", dir);
			entity.getPersistentData().putInt("maho_dodge_dir", dir);

			// strong sideways burst + slight backward step away from target
			double backX = (dist2d > 0.001) ? -dx / dist2d * 0.30 : 0;
			double backZ = (dist2d > 0.001) ? -dz / dist2d * 0.30 : 0;
			entity.setDeltaMovement(
				perpX * dir * 1.0 + backX,
				0.20,
				perpZ * dir * 1.0 + backZ);
			entity.hurtMarked = true;

			// brief poof to sell the dodge
			if (world instanceof ServerLevel level) {
				level.sendParticles(ParticleTypes.POOF,
					entity.getX(), entity.getY() + 0.5, entity.getZ(),
					10, 0.45, 0.3, 0.45, 0.06);
			}
		}

		// t 2-7: momentum carry + gentle perpendicular nudge to keep the arc going
		if (t >= 2 && t <= 7) {
			int dir = entity.getPersistentData().getInt("maho_dodge_dir");
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(
				cur.x * 0.74 + perpX * dir * 0.07,
				cur.y,
				cur.z * 0.74 + perpZ * dir * 0.07);
			entity.hurtMarked = true;
		}

		// t 8-9: brief pause before re-engaging — face target, nearly stopped
		if (t >= 8 && t <= 9) {
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(cur.x * 0.4, cur.y, cur.z * 0.4);
		}

		if (t >= 11) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_dodge", 38);
			entity.getPersistentData().putInt("maho_cd_global", 3);
		}
	}
}
