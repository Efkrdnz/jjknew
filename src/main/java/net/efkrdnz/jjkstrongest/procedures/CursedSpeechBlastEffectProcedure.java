package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

public class CursedSpeechBlastEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.0)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 3, 0.5, 0.5, 0.5, 0.1);
		// direction away from caster
		Vec3 awayDir = target.position().subtract(source.position()).normalize();
		double blastStrength = Math.max(0.5, 2.5 - powerRatio * 0.5);
		target.setDeltaMovement(awayDir.x * blastStrength, 0.6 + blastStrength * 0.3, awayDir.z * blastStrength);
		target.hurtMarked = true;
		// deal moderate impact damage
		float damage = (float) (12.0 * Math.max(0.1, 1.0 - powerRatio * 0.25));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cursedspeech"))), source), damage);
		// backlash - caster gets pushed back slightly
		Vec3 backDir = source.position().subtract(target.position()).normalize();
		source.setDeltaMovement(source.getDeltaMovement().add(backDir.x * 0.4, 0.15, backDir.z * 0.4));
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:jujutsu"))), source), (float) (8.0 * powerRatio));
	}
}
