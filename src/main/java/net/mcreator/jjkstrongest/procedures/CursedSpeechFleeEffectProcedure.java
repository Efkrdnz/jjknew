package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

public class CursedSpeechFleeEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.5)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 10, 0.4, 0.3, 0.4, 0.1);
		// force target away from source
		Vec3 awayDir = target.position().subtract(source.position()).normalize();
		double fleeStrength = Math.max(0.8, 2.0 - powerRatio * 0.3);
		target.setDeltaMovement(awayDir.x * fleeStrength, 0.3, awayDir.z * fleeStrength);
		target.hurtMarked = true;
		// apply speed and panic effects
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (120 * Math.max(0.4, 1.3 - powerRatio * 0.25));
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2, false, false));
			// nausea to simulate panic (can't think straight)
			le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false));
		}
		// if mob, clear its current target so it runs aimlessly
		if (target instanceof Mob mob && !mob.level().isClientSide()) {
			mob.setTarget(null);
			mob.getNavigation().stop();
		}
		// very small backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (3.0 * powerRatio));
	}
}
