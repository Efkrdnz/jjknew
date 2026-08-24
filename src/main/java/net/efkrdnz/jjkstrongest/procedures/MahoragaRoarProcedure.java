package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

import java.util.List;

public class MahoragaRoarProcedure {
	// Phase 2+ attack: expanding-ring telegraph for ~20 ticks, then massive shockwave AoE
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		entity.setDeltaMovement(new Vec3(0, entity.getDeltaMovement().y * 0.2, 0));
		MahoragaFaceTargetProcedure.execute(entity, target);

		if (t == 1) {
			if (entity instanceof MahoragaEntity m)
				m.setAnimation("idle");
		}

		// expanding ring telegraph
		if (t >= 4 && t <= 20 && world instanceof ServerLevel level) {
			double progress = (t - 4) / 16.0;
			double r = progress * 5.5;
			int count = 10;
			for (int i = 0; i < count; i++) {
				double angle = (2 * Math.PI / count) * i + (t * 0.25);
				double px = entity.getX() + Math.cos(angle) * r;
				double pz = entity.getZ() + Math.sin(angle) * r;
				level.sendParticles(ParticleTypes.SOUL, px, entity.getY() + 0.3, pz, 1, 0, 0.1, 0, 0);
			}
			// rising mist on self
			if (t % 3 == 0) {
				level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					entity.getX(), entity.getY() + 0.5, entity.getZ(),
					4, 0.4, 0.1, 0.4, 0.02);
			}
		}

		// shockwave release
		if (t == 22) {
			if (world instanceof ServerLevel level) {
				level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
					entity.getX(), entity.getY() + 1, entity.getZ(),
					4, 0.5, 0.3, 0.5, 0.05);
				level.sendParticles(ParticleTypes.CLOUD,
					entity.getX(), entity.getY() + 0.3, entity.getZ(),
					70, 5.5, 0.4, 5.5, 0.06);
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					entity.getX(), entity.getY() + 1, entity.getZ(),
					25, 3.5, 0.5, 3.5, 0.12);
			}
			// sound
			if (world instanceof ServerLevel level) {
				level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
					BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("minecraft:entity.ravager.roar")),
					SoundSource.HOSTILE, 2.5f, 0.75f);
			}
			// AoE: radius 7.5, damage + knockback with distance falloff
			double radius = 7.5;
			AABB box = new AABB(entity.blockPosition()).inflate(radius);
			List<Entity> list = entity.level().getEntities(entity, box, e -> e instanceof LivingEntity && e != entity);
			for (Entity e : list) {
				LivingEntity le = (LivingEntity) e;
				double dx = le.getX() - entity.getX();
				double dz = le.getZ() - entity.getZ();
				double dist = Math.sqrt(dx * dx + dz * dz);
				double falloff = Math.max(0.2, 1.0 - dist / radius);
				le.invulnerableTime = 0;
				le.hurt(le.damageSources().mobAttack((LivingEntity) entity), (float) (16.0 * falloff));
				if (dist > 0.01) {
					le.setDeltaMovement(le.getDeltaMovement().add(
						dx / dist * 2.4 * falloff,
						0.65 * falloff,
						dz / dist * 2.4 * falloff));
				}
				le.hurtMarked = true;
			}
			MahoragaBreakBlocksByHardnessProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), 4);
		}

		if (t >= 38) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_roar", 320);
			entity.getPersistentData().putInt("maho_cd_global", 18);
		}
	}
}
