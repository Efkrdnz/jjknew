package net.mcreator.jjkstrongest.procedures;

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

public class CursedSpeechSpitEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.5)
			return;
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 40, 0.3, 0.4, 0.3, 0.15);
		}
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (160 * Math.max(0.3, 1.4 - powerRatio * 0.3));
			le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration + 40, 1, false, true));
			le.addEffect(new MobEffectInstance(MobEffects.POISON, duration, Math.max(0, (int)(1 - powerRatio * 0.5)), false, true));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration / 2, 0, false, false));
		}
		// initial hit from the forced expulsion
		float damage = (float) (8.0 * Math.max(0.1, 1.1 - powerRatio * 0.25));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:technique_cursedspeech"))), source), damage);
		// small backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (4.0 * powerRatio));
	}
}
