package net.efkrdnz.jjkstrongest.procedures;

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

public class CursedSpeechShrinkEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 3.5)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 60, 0.3, target.getBbHeight() / 2, 0.3, 0.1);
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (140 * Math.max(0.3, 1.3 - powerRatio * 0.35));
			// body constricting — can't move, can't hit hard, can't dig
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 1, false, false));
		}
		// minor damage — more of a control ability
		float damage = (float) (6.0 * Math.max(0.1, 1.0 - powerRatio * 0.3));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cursedspeech"))), source), damage);
		// small backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:jujutsu"))), source), (float) (5.0 * powerRatio));
	}
}
