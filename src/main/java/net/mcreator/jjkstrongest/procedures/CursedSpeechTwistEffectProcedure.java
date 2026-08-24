package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
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

public class CursedSpeechTwistEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.0)
			return;
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 60, 0.3, target.getBbHeight() / 2, 0.3, 0.3);
		}
		// twist their muscles — random spin + upward jerk
		java.util.Random rng = new java.util.Random();
		double spinX = (rng.nextDouble() - 0.5) * 1.8;
		double spinZ = (rng.nextDouble() - 0.5) * 1.8;
		target.setDeltaMovement(spinX, 0.5, spinZ);
		target.hurtMarked = true;
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (120 * Math.max(0.3, 1.3 - powerRatio * 0.3));
			// severe nausea — disorientation from twisted senses
			le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration + 60, 1, false, false));
			// slowness — muscles aren't responding right
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, false));
		}
		// internal injury damage — moderate
		float damage = (float) (14.0 * Math.max(0.1, 1.15 - powerRatio * 0.3));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:technique_cursedspeech"))), source), damage);
		// small backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (6.0 * powerRatio));
	}
}
