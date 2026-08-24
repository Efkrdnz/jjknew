package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

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

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

import java.util.List;

public class MahoragaWheelCrashProcedure {
	// Phase 3+ signature: high leap, apex pause, then screaming dive-bomb with massive impact AoE
	// maho_wc_phase: 0=launch, 1=apex, 2=dive
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();

		int wcPhase = entity.getPersistentData().getInt("maho_wc_phase");

		// ── Launch ────────────────────────────────────────────────────────────────
		if (t == 1) {
			entity.getPersistentData().putInt("maho_wc_phase", 0);
			entity.getPersistentData().putInt("maho_wc_hit", 0);
			if (entity instanceof MahoragaEntity m)
				m.setAnimation("jump");
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			double dirx = len > 0.001 ? dx / len : entity.getLookAngle().x;
			double dirz = len > 0.001 ? dz / len : entity.getLookAngle().z;
			entity.getPersistentData().putDouble("maho_wc_dx", dirx);
			entity.getPersistentData().putDouble("maho_wc_dz", dirz);
			entity.setDeltaMovement(dirx * 0.55, 1.9, dirz * 0.55);
			entity.hurtMarked = true;
			if (world instanceof ServerLevel level) {
				level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
					ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft:entity.iron_golem.attack")),
					SoundSource.HOSTILE, 2.0f, 0.65f);
			}
		}

		// transition launch → apex
		if (wcPhase == 0 && t >= 6 && entity.getDeltaMovement().y < 0) {
			entity.getPersistentData().putInt("maho_wc_phase", 1);
			wcPhase = 1;
		}

		// ── Apex pause: wheel-spin particles, brief hang ──────────────────────────
		if (wcPhase == 1) {
			entity.setDeltaMovement(entity.getDeltaMovement().x * 0.85,
				Math.max(-0.3, entity.getDeltaMovement().y * 0.6),
				entity.getDeltaMovement().z * 0.85);
			entity.hurtMarked = true;
			if (world instanceof ServerLevel level && t % 2 == 0) {
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					entity.getX(), entity.getY() + 1.0, entity.getZ(),
					5, 0.5, 0.5, 0.5, 0.08);
			}
			// start dive after 8 ticks at apex
			int apexStart = entity.getPersistentData().getInt("maho_wc_apex_t");
			if (apexStart == 0)
				entity.getPersistentData().putInt("maho_wc_apex_t", t);
			int apexElapsed = t - entity.getPersistentData().getInt("maho_wc_apex_t");
			if (apexElapsed >= 7) {
				entity.getPersistentData().putInt("maho_wc_phase", 2);
				entity.getPersistentData().putInt("maho_wc_apex_t", 0);
				wcPhase = 2;
				if (entity instanceof MahoragaEntity m)
					m.setAnimation("attack_overhead");
				// aim directly at current target position for the dive
				double dx2 = target.getX() - entity.getX();
				double dz2 = target.getZ() - entity.getZ();
				double len2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
				double dirx2 = len2 > 0.001 ? dx2 / len2 : entity.getPersistentData().getDouble("maho_wc_dx");
				double dirz2 = len2 > 0.001 ? dz2 / len2 : entity.getPersistentData().getDouble("maho_wc_dz");
				entity.getPersistentData().putDouble("maho_wc_dx", dirx2);
				entity.getPersistentData().putDouble("maho_wc_dz", dirz2);
				entity.setDeltaMovement(dirx2 * 1.5, -3.8, dirz2 * 1.5);
				entity.hurtMarked = true;
				if (world instanceof ServerLevel level) {
					level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
						ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft:entity.wither.shoot")),
						SoundSource.HOSTILE, 1.8f, 0.55f);
				}
			}
		}

		// ── Dive ──────────────────────────────────────────────────────────────────
		if (wcPhase == 2) {
			double dirx = entity.getPersistentData().getDouble("maho_wc_dx");
			double dirz = entity.getPersistentData().getDouble("maho_wc_dz");
			// light homing toward target
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			if (len > 0.001) {
				dirx = dx / len;
				dirz = dz / len;
				entity.getPersistentData().putDouble("maho_wc_dx", dirx);
				entity.getPersistentData().putDouble("maho_wc_dz", dirz);
			}
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(cur.x * 0.88 + dirx * 0.18, cur.y, cur.z * 0.88 + dirz * 0.18);
			entity.hurtMarked = true;
			// dive trail particles
			if (world instanceof ServerLevel level && t % 2 == 0) {
				level.sendParticles(ParticleTypes.CLOUD,
					entity.getX(), entity.getY() + 1, entity.getZ(),
					6, 0.3, 0.3, 0.3, 0.04);
			}
			// midair hit (once)
			if (entity.getPersistentData().getInt("maho_wc_hit") == 0 && entity.distanceTo(target) <= 3.0) {
				entity.getPersistentData().putInt("maho_wc_hit", 1);
				target.invulnerableTime = 0;
				target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 26.0F);
				target.setDeltaMovement(dirx * 0.35, -3.5, dirz * 0.35);
				target.getPersistentData().putInt("maho_launch_ticks", 50);
				target.hurtMarked = true;
			}
			// ground impact
			if (entity.onGround()) {
				double impactRadius = 7.0;
				if (world instanceof ServerLevel level) {
					level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
						entity.getX(), entity.getY() + 0.5, entity.getZ(),
						6, 0.8, 0.3, 0.8, 0.08);
					level.sendParticles(ParticleTypes.CLOUD,
						entity.getX(), entity.getY(), entity.getZ(),
						90, impactRadius * 0.55, 0.4, impactRadius * 0.55, 0.07);
					level.sendParticles(ParticleTypes.CRIT,
						entity.getX(), entity.getY() + 0.5, entity.getZ(),
						40, impactRadius * 0.4, 0.3, impactRadius * 0.4, 0.2);
					level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
						ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft:entity.generic.explode")),
						SoundSource.HOSTILE, 2.5f, 0.65f);
				}
				AABB box = new AABB(entity.blockPosition()).inflate(impactRadius);
				List<Entity> list = entity.level().getEntities(entity, box, e -> e instanceof LivingEntity && e != entity);
				for (Entity e : list) {
					LivingEntity le = (LivingEntity) e;
					double kx = le.getX() - entity.getX();
					double kz = le.getZ() - entity.getZ();
					double klen = Math.sqrt(kx * kx + kz * kz);
					double falloff = Math.max(0.25, 1.0 - klen / impactRadius);
					le.invulnerableTime = 0;
					le.hurt(le.damageSources().mobAttack((LivingEntity) entity), (float) (32.0 * falloff));
					if (klen > 0.001) {
						le.setDeltaMovement(le.getDeltaMovement().add(
							kx / klen * 3.2 * falloff, 1.3 * falloff, kz / klen * 3.2 * falloff));
					} else {
						le.setDeltaMovement(0, 1.8, 0);
					}
					le.hurtMarked = true;
				}
				MahoragaBreakBlocksByHardnessProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), 5);
				cleanup(entity);
				return;
			}
		}

		// failsafe
		if (t >= 110) {
			cleanup(entity);
		}
	}

	private static void cleanup(Entity entity) {
		entity.getPersistentData().putString("maho_state", "TARGETING");
		entity.getPersistentData().putInt("maho_t", 0);
		entity.getPersistentData().putInt("maho_cd_wheelcrash", 420);
		entity.getPersistentData().putInt("maho_cd_global", 24);
		entity.getPersistentData().putInt("maho_wc_phase", 0);
		entity.getPersistentData().putInt("maho_wc_hit", 0);
		entity.getPersistentData().putInt("maho_wc_apex_t", 0);
	}
}
