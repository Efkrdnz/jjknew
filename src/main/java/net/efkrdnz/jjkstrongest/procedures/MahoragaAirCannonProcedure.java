package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaAirCannonProcedure {
	// Phase 1-2: single mid-range blast that knocks back
	// Phase 3+:  three rapid-fire bursts with slight spread
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t");
		t++;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		entity.setDeltaMovement(new Vec3(0, entity.getDeltaMovement().y * 0.2, 0));
		MahoragaFaceTargetProcedure.execute(entity, target);

		int phase = getPhase(entity);

		if (t == 1) {
			if (entity instanceof MahoragaEntity)
				((MahoragaEntity) entity).setAnimation("attack_normal");
		}

		if (phase >= 3) {
			// ── Phase 3+: three bursts at t=10, 14, 18 with increasing spread ──────
			int[] fireTicks = { 10, 14, 18 };
			float[] spreadAngles = { 0f, 12f, -12f }; // degrees of yaw offset
			for (int i = 0; i < fireTicks.length; i++) {
				if (t == fireTicks[i]) {
					fireBlast(world, entity, target, spreadAngles[i]);
				}
			}
			if (t >= 28) {
				entity.getPersistentData().putString("maho_state", "TARGETING");
				entity.getPersistentData().putInt("maho_t", 0);
				entity.getPersistentData().putInt("maho_cd_cannon", 70);
				entity.getPersistentData().putInt("maho_cd_global", 14);
			}
		} else {
			// ── Phase 1-2: original single blast ─────────────────────────────────
			if (t == 12) {
				fireBlast(world, entity, target, 0f);
			}
			if (t >= 22) {
				entity.getPersistentData().putString("maho_state", "TARGETING");
				entity.getPersistentData().putInt("maho_t", 0);
				entity.getPersistentData().putInt("maho_cd_cannon", 70);
				entity.getPersistentData().putInt("maho_cd_global", 12);
			}
		}
	}

	private static void fireBlast(LevelAccessor world, Entity entity, LivingEntity target, float yawOffsetDeg) {
		// build aim direction from entity eye toward target center, then rotate by offset
		double dx = target.getX() - entity.getX();
		double dy = (target.getY() + target.getBbHeight() * 0.5) - (entity.getY() + 1.5);
		double dz = target.getZ() - entity.getZ();
		double hLen = Math.sqrt(dx * dx + dz * dz);

		// apply yaw spread in the horizontal plane
		if (yawOffsetDeg != 0f && hLen > 0.001) {
			double rad = Math.toRadians(yawOffsetDeg);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			double rotDx = dx * cos - dz * sin;
			double rotDz = dx * sin + dz * cos;
			dx = rotDx;
			dz = rotDz;
			hLen = Math.sqrt(dx * dx + dz * dz);
		}

		double fullLen = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (fullLen < 0.001)
			return;
		Vec3 look = new Vec3(dx / fullLen, dy / fullLen, dz / fullLen);

		// particle trail
		if (world instanceof ServerLevel level) {
			for (int i = 1; i <= 14; i++) {
				double px = entity.getX() + look.x * i;
				double py = entity.getY() + 1.5 + look.y * i;
				double pz = entity.getZ() + look.z * i;
				level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 2, 0.05, 0.05, 0.05, 0.01);
			}
		}

		// hitscan damage if in range and line of sight
		if (entity.distanceTo(target) <= 14 && entity instanceof Mob mob && mob.hasLineOfSight(target)) {
			target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 18.0F);
			if (hLen > 0.001) {
				target.setDeltaMovement(target.getDeltaMovement().add(
					dx / hLen * 2.0, 0.25, dz / hLen * 2.0));
			}
			target.invulnerableTime = 0;
		}
	}

	private static int getPhase(Entity entity) {
		if (!(entity instanceof LivingEntity le))
			return 1;
		float ratio = le.getHealth() / le.getMaxHealth();
		if (ratio > 0.75f) return 1;
		if (ratio > 0.50f) return 2;
		if (ratio > 0.25f) return 3;
		return 4;
	}
}
