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

public class CursedSpeechRotEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 3.5)
			return;
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.WARPED_SPORE, target.getX(), target.getY(), target.getZ(), 40, 0.4, target.getBbHeight() / 2, 0.4, 0.04);
			_level.sendParticles(ParticleTypes.SMALL_FLAME, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 15, 0.3, 0.4, 0.3, 0.02);
		}
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (200 * Math.max(0.3, 1.3 - powerRatio * 0.35));
			int amplifier = (int) Math.max(0, 2 - powerRatio);
			// wither for sustained HP drain
			le.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, amplifier, false, true));
			// poison on top for stacking DoT
			le.addEffect(new MobEffectInstance(MobEffects.POISON, duration, Math.max(0, amplifier - 1), false, true));
			// weakness — body is decaying
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false));
		}
		// initial rot damage
		float damage = (float) (8.0 * Math.max(0.1, 1.0 - powerRatio * 0.3));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:technique_cursedspeech"))), source), damage);
		// backlash — caster's own cursed energy starts eating at them
		if (source instanceof LivingEntity le && !le.level().isClientSide()) {
			le.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, false, false));
		}
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (7.0 * powerRatio));
	}
}
