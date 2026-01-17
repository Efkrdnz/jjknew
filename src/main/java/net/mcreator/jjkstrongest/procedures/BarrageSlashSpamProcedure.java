package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

public class BarrageSlashSpamProcedure {
	public static void execute(Level world, Entity entity) {
		if (entity == null || world == null)
			return;
		double output_multiplier = ReturnOutputDismantleProcedure.execute(world, entity);
		double output = output_multiplier + 1;
		double techniquePower = 1.6;
		boolean isVertical = entity.isShiftKeyDown();
		executeSingleTargetSlash(world, entity, techniquePower, output, isVertical);
	}

	// single target slash with stacking slowness
	private static void executeSingleTargetSlash(Level world, Entity entity, double techniquePower, double output, boolean isVertical) {
		double baseRange = 15.0;
		double range = baseRange * Math.sqrt(techniquePower);
		Entity target = findTargetEntity(entity, range);
		if (target != null && target instanceof LivingEntity) {
			double baseDamage = 1.5;
			double finalDamage = baseDamage * techniquePower * output;
			applySlashDamage(world, entity, target, finalDamage, isVertical, techniquePower);
		}
	}

	// find closest target entity using raycast
	private static Entity findTargetEntity(Entity shooter, double range) {
		Vec3 eyePos = shooter.getEyePosition(1.0f);
		Vec3 lookVec = shooter.getViewVector(1.0f);
		Vec3 endPos = eyePos.add(lookVec.scale(range));
		double searchRadius = 2.0;
		AABB searchBox = new AABB(Math.min(eyePos.x, endPos.x) - searchRadius, Math.min(eyePos.y, endPos.y) - searchRadius, Math.min(eyePos.z, endPos.z) - searchRadius, Math.max(eyePos.x, endPos.x) + searchRadius,
				Math.max(eyePos.y, endPos.y) + searchRadius, Math.max(eyePos.z, endPos.z) + searchRadius);
		List<Entity> nearbyEntities = shooter.level().getEntities(shooter, searchBox, e -> e instanceof LivingEntity && !e.isSpectator() && e.isAlive());
		Entity closestTarget = null;
		double closestDot = 0.95;
		double closestDistance = range;
		for (Entity target : nearbyEntities) {
			Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2, 0).subtract(eyePos);
			double distance = toTarget.length();
			if (distance > range)
				continue;
			Vec3 toTargetNorm = toTarget.normalize();
			double dot = lookVec.dot(toTargetNorm);
			if (dot > closestDot || (Math.abs(dot - closestDot) < 0.05 && distance < closestDistance)) {
				closestDot = dot;
				closestDistance = distance;
				closestTarget = target;
			}
		}
		return closestTarget;
	}

	// apply slash damage with progressive slowness
	private static void applySlashDamage(Level world, Entity shooter, Entity target, double damage, boolean isVertical, double techniquePower) {
		Vec3 slashDir = new Vec3(world.random.nextDouble() - 0.5, Math.abs(world.random.nextDouble() - 0.5), world.random.nextDouble() - 0.5).normalize();
		target.getPersistentData().putDouble("dismantle_slash_x", slashDir.x);
		target.getPersistentData().putDouble("dismantle_slash_y", slashDir.y);
		target.getPersistentData().putDouble("dismantle_slash_z", slashDir.z);
		target.getPersistentData().putLong("dismantle_slash_time", world.getGameTime());
		// progressive slowness stacking system
		if (target instanceof LivingEntity livingTarget) {
			// track hit count for this barrage session
			int hitCount = target.getPersistentData().getInt("barrage_hit_count");
			long lastHitTime = target.getPersistentData().getLong("barrage_last_hit");
			long currentTime = world.getGameTime();
			// reset counter if more than 3 seconds since last hit
			if (currentTime - lastHitTime > 60) {
				hitCount = 0;
			}
			// increment hit count
			hitCount++;
			target.getPersistentData().putInt("barrage_hit_count", hitCount);
			target.getPersistentData().putLong("barrage_last_hit", currentTime);
			if (shooter instanceof LivingEntity _entity && !_entity.level().isClientSide() && hitCount % 2 == 0)
				_entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 4, 1, false, false));
			// === FLASH EFFECT EVERY 2 SLASHES ===
			if (world.isClientSide && hitCount % 2 == 0) {
				// random intensity values for variety
				float randomGamma = 1.5f + (float) (Math.random() * 1.0); // 1.5 to 2.3
				float randomContrast = 1.8f + (float) (Math.random() * 1.0); // 1.8 to 2.6
				// quick flash (2 ticks)
				TriggerChargedImpactProcedure.execute(world, shooter, 4, 1.0f, 1.77f, 2.65f);
				//System.out.println(randomGamma);
				//System.out.println(randomContrast);
				// screen shake (subtle for barrage)
				TriggerScreenShakeProcedure.execute(world, shooter, 5, 2.5f);
			}
			// progressive slowness amplifier based on hit count
			int amplifier = Math.min(hitCount / 3, 4); // caps at level 5 (amplifier 4)
			// apply slowness effect
			livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, amplifier, false, false));
			// at high hit counts, also add mining fatigue for full lockdown
			if (hitCount >= 10) {
				livingTarget.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 2, false, false));
			}
		}
		// no knockback application
		DamageSource damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), shooter);
		double originalKnockbackResist = 0;
		if (target instanceof LivingEntity livingTarget) {
			originalKnockbackResist = livingTarget.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
			livingTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
		}
		target.hurt(damageSource, (float) damage);
		if (target instanceof LivingEntity livingTarget) {
			livingTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(originalKnockbackResist);
		}
		// spawn NEW shader-based slash effect
		SpawnDismantleSlashProcedure.execute(world, shooter, target, slashDir, techniquePower);
		if (world instanceof ServerLevel serverLevel) {
			Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
			serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetPos.x, targetPos.y, targetPos.z, 2, 0.3, 0.3, 0.3, 0.0);
		}
		target.invulnerableTime = 0;
	}
}
