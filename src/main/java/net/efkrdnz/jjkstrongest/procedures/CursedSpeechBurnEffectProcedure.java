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

public class CursedSpeechBurnEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 5.0)
			return;
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(), target.getZ(), 30, 0.3, target.getBbHeight() / 2, 0.3, 0.1);
			_level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
		}
		// set on fire — duration scales with power advantage
		int fireTicks = (int) (100 * Math.max(0.2, 1.5 - powerRatio * 0.3));
		target.igniteForSeconds(fireTicks / 20 + 1);
		// fire damage burst
		float damage = (float) (10.0 * Math.max(0.1, 1.2 - powerRatio * 0.25));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cursedspeech"))), source), damage);
		// tiny backlash — caster's throat burns
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:jujutsu"))), source), (float) (4.0 * powerRatio));
	}
}
