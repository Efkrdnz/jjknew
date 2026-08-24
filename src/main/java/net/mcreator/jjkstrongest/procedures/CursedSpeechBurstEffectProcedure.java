package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

public class CursedSpeechBurstEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 3.0)
			return;
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 5, 0.6, 0.6, 0.6, 0.2);
			_level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(), target.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
		}
		// create a non-griefing explosion at target
		if (world instanceof Level level && !level.isClientSide()) {
			float explosionStrength = (float) Math.max(1.0, 3.5 - powerRatio * 0.8);
			// null entity source so explosion damage has no player attached — prevents Black Flash trigger
			level.explode(null, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
					explosionStrength, Level.ExplosionInteraction.NONE);
		}
		// extra direct damage on top of explosion
		float damage = (float) (15.0 * Math.max(0.1, 1.1 - powerRatio * 0.4));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:technique_cursedspeech"))), source), damage);
		// heavy backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (14.0 * powerRatio));
	}
}
