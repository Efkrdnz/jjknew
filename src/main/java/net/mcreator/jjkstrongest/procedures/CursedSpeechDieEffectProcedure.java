package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

public class CursedSpeechDieEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		Entity src = null;
		Entity trgt = null;
		double powerRatio = 0;
		double damagePercent = 0;
		double backlashDamage = 0;
		src = source;
		trgt = target;
		powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio <= 0.3) {
			if (powerRatio <= 0.7) {
				damagePercent = 105;
				backlashDamage = 20;
			} else if (powerRatio <= 1.0) {
				damagePercent = 50;
				backlashDamage = 25;
			} else if (powerRatio <= 1.3) {
				damagePercent = 30;
				backlashDamage = 30;
			} else if (powerRatio <= 1.7) {
				damagePercent = 20;
				backlashDamage = 35;
			} else if (powerRatio <= 2.5) {
				damagePercent = 15;
				backlashDamage = 40;
			} else {
				damagePercent = 0;
				backlashDamage = 50;
			}
		}
		if (powerRatio < 5) {
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.CRIT, (target.getX()), (target.getY() + target.getBbHeight() / 2), (target.getZ()), 50, 0.2, (target.getBbHeight()), 0.2, 0.2);
			target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:technique_cursedspeech"))), source),
					(float) (damagePercent * ((target instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / 100)));
			source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) backlashDamage);
		}
	}
}
