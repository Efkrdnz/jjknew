package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaFeintProcedure {
	// Phase 2+: burst straight at the target for 6 ticks.
	// If the gap is closed → immediately convert into BARRAGE (real attack).
	// If not close enough  → sidestep hard and disengage (pure deception).
	// This makes the player constantly unsure whether an approach is an attack or a fake.
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

		if (t == 1) {
			if (entity instanceof MahoragaEntity m)
				m.setAnimation("run");
			entity.getPersistentData().putInt("maho_feint_broke", 0);
		}

		// ── Rush phase (t 1-6) ────────────────────────────────────────────────────
		if (t <= 6 && entity.getPersistentData().getInt("maho_feint_broke") == 0) {
			if (dist2d > 0.001) {
				// slightly faster than normal approach, slightly slower than dash
				entity.setDeltaMovement(
					dx / dist2d * 1.10,
					entity.getDeltaMovement().y * 0.25 + 0.04,
					dz / dist2d * 1.10);
				entity.hurtMarked = true;
			}
		}

		// ── Decision (t 7) ────────────────────────────────────────────────────────
		if (t == 7) {
			// If we closed the gap: convert to BARRAGE — the feint is now a real attack
			if (entity.distanceTo(target) <= 3.2
					&& entity.getPersistentData().getInt("maho_cd_barrage") <= 0) {
				entity.getPersistentData().putString("maho_state", "BARRAGE");
				entity.getPersistentData().putInt("maho_t", 0);
				entity.getPersistentData().putInt("maho_cd_feint", 140);
				return;
			}
			// Otherwise: break off — pick a sidestep direction (opposite to last dodge)
			entity.getPersistentData().putInt("maho_feint_broke", 1);
			int last = entity.getPersistentData().getInt("maho_last_dodge_dir");
			int dir = (last >= 0) ? -1 : 1;
			entity.getPersistentData().putInt("maho_last_dodge_dir", dir);
			entity.getPersistentData().putInt("maho_feint_dir", dir);

			double perpX = (dist2d > 0.001) ? -dz / dist2d : 1.0;
			double perpZ = (dist2d > 0.001) ?  dx / dist2d : 0.0;
			// sharp sidestep + tiny backward component
			double backX = (dist2d > 0.001) ? -dx / dist2d * 0.25 : 0;
			double backZ = (dist2d > 0.001) ? -dz / dist2d * 0.25 : 0;
			entity.setDeltaMovement(
				perpX * dir * 0.90 + backX,
				0.16,
				perpZ * dir * 0.90 + backZ);
			entity.hurtMarked = true;
		}

		// ── Sidestep carry (t 8-13) ───────────────────────────────────────────────
		if (t >= 8 && t <= 13 && entity.getPersistentData().getInt("maho_feint_broke") == 1) {
			int dir = entity.getPersistentData().getInt("maho_feint_dir");
			double perpX = (dist2d > 0.001) ? -dz / dist2d : 1.0;
			double perpZ = (dist2d > 0.001) ?  dx / dist2d : 0.0;
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(
				cur.x * 0.76 + perpX * dir * 0.065,
				cur.y,
				cur.z * 0.76 + perpZ * dir * 0.065);
			entity.hurtMarked = true;
		}

		// ── End ───────────────────────────────────────────────────────────────────
		if (t >= 16) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_feint", 140);
			entity.getPersistentData().putInt("maho_cd_global", 5);
		}
	}
}
