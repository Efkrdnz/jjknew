package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.Freezeframe;

import java.util.List;
import java.util.ArrayList;

public class RaycastDismantleAdvancedProcedure {
	public static void execute(Level world, Entity entity) {
		if (entity == null || world == null)
			return;
		// check if chanting is active
		boolean isChanting = !entity.getPersistentData().getString("chanting").isEmpty();
		double output_multiplier = ReturnOutputDismantleProcedure.execute(world, entity);
		double output = output_multiplier + 1;
		if (!isChanting) {
			// start chanting
			entity.getPersistentData().putString("chanting", "dismantle");
		} else {
			// execute instant raycast slash
			double techniquePower = entity.getPersistentData().getDouble("TechniquePower");
			if (techniquePower == 0) {
				techniquePower = 1.0;
			}
			// determine slash orientation
			boolean isVertical = entity.isShiftKeyDown();
			// check if fully charged for AOE effect
			if (techniquePower >= 2.0) {
				// charged attack - slash all enemies in front
				executeAOESlash(world, entity, techniquePower, output, isVertical);
			} else {
				// normal attack - single target
				executeSingleTargetSlash(world, entity, techniquePower, output, isVertical);
			}
			// reset chanting
			entity.getPersistentData().putString("chanting", "");
			entity.getPersistentData().putDouble("ChantCounter", 0);
			entity.getPersistentData().putDouble("TechniquePower", 0);
		}
	}

	// single target slash
	private static void executeSingleTargetSlash(Level world, Entity entity, double techniquePower, double output, boolean isVertical) {
		double baseRange = 15.0;
		double range = baseRange * Math.sqrt(techniquePower);
		Entity target = findTargetEntity(entity, range);
		if (target != null && target instanceof LivingEntity) {
			double baseDamage = 8.0;
			double finalDamage = baseDamage * techniquePower * output;
			applySlashDamage(world, entity, target, finalDamage, isVertical, techniquePower, true);
		}
	}

	// aoe slash for fully charged attacks
	private static void executeAOESlash(Level world, Entity entity, double techniquePower, double output, boolean isVertical) {
		double baseRange = 20.0;
		double range = baseRange * Math.sqrt(techniquePower);
		Vec3 eyePos = entity.getEyePosition(1.0f);
		Vec3 lookVec = entity.getViewVector(1.0f);
		Vec3 endPos = eyePos.add(lookVec.scale(range));
		// wider search cone for aoe
		double searchRadius = 5.0 * Math.sqrt(techniquePower);
		AABB searchBox = new AABB(Math.min(eyePos.x, endPos.x) - searchRadius, Math.min(eyePos.y, endPos.y) - searchRadius, Math.min(eyePos.z, endPos.z) - searchRadius, Math.max(eyePos.x, endPos.x) + searchRadius,
				Math.max(eyePos.y, endPos.y) + searchRadius, Math.max(eyePos.z, endPos.z) + searchRadius);
		List<Entity> nearbyEntities = entity.level().getEntities(entity, searchBox, e -> e instanceof LivingEntity && !e.isSpectator() && e.isAlive());
		// filter entities in front cone
		List<Entity> targetsInCone = new ArrayList<>();
		for (Entity target : nearbyEntities) {
			Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2, 0).subtract(eyePos);
			double distance = toTarget.length();
			if (distance > range)
				continue;
			Vec3 toTargetNorm = toTarget.normalize();
			double dot = lookVec.dot(toTargetNorm);
			if (dot > 0.7) {
				targetsInCone.add(target);
			}
		}
		// SET FLAG - freeze only triggers ONCE for this cast
		boolean freezeTriggered = false;
		// damage all targets in cone
		double baseDamage = 8.0;
		double finalDamage = baseDamage * techniquePower * output;
		for (Entity target : targetsInCone) {
			// pass the flag to prevent multiple freezes
			applySlashDamage(world, entity, target, finalDamage, isVertical, techniquePower, !freezeTriggered);
			freezeTriggered = true; // set flag after first target
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

	// apply slash damage and effects to target
	private static void applySlashDamage(Level world, Entity shooter, Entity target, double damage, boolean isVertical, double techniquePower, boolean canFreeze) {
		// TRIGGER CHARGED IMPACT FRAME for fully charged attacks
		if (shooter instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 5, 1, false, false));
		if (canFreeze && world.isClientSide && techniquePower >= 2.0) {
			TriggerChargedImpactProcedure.execute(world, shooter, 3, 1.0f, 1.77f, 2.65f);
			// FREEZE FRAME on client after 2 ticks (100ms)
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
			new java.util.Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					// execute on main thread
					mc.execute(() -> {
						Freezeframe.execute(75);
					});
				}
			}, 100); // 100ms = ~2 ticks
			TriggerScreenShakeProcedure.execute(world, shooter, 10, 3.0f);
		}
		if (!world.isClientSide()) {
			world.playSound(null, BlockPos.containing(shooter.getX(), shooter.getY(), shooter.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:kai")), SoundSource.NEUTRAL, 1, (float) 0.23);
		} else {
			world.playLocalSound((shooter.getX()), (shooter.getY()), (shooter.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:kai")), SoundSource.NEUTRAL, 1, (float) 0.25, false);
		}
		// generate random slash direction (same as visual effect)
		// Make sure Y component is non-negative so plane doesn't point downward
		Vec3 slashDir = new Vec3(world.random.nextDouble() - 0.5, // -0.5 to 0.5
				Math.abs(world.random.nextDouble() - 0.5), // 0 to 0.5 (ALWAYS POSITIVE OR ZERO)
				world.random.nextDouble() - 0.5 // -0.5 to 0.5
		).normalize();
		// tag target with slash direction for death handler
		target.getPersistentData().putDouble("dismantle_slash_x", slashDir.x);
		target.getPersistentData().putDouble("dismantle_slash_y", slashDir.y);
		target.getPersistentData().putDouble("dismantle_slash_z", slashDir.z);
		target.getPersistentData().putLong("dismantle_slash_time", world.getGameTime());
		// apply damage
		DamageSource damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), shooter);
		target.hurt(damageSource, (float) damage);
		// spawn NEW shader-based slash effect
		SpawnDismantleSlashProcedure.execute(world, shooter, target, slashDir, techniquePower);
		// spawn hit particles on server
		if (world instanceof ServerLevel serverLevel) {
			Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
			serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, targetPos.x, targetPos.y, targetPos.z, 3, 0.5, 0.5, 0.5, 0.0);
		}
	}
}
