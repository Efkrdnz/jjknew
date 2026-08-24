package net.efkrdnz.jjkstrongest.procedures;

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

public class CursedSpeechPullEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.0)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 30, 0.3, 0.4, 0.3, 0.1);
		// pull target toward source
		Vec3 towardDir = source.position().subtract(target.position()).normalize();
		double pullStrength = Math.max(0.5, 2.2 - powerRatio * 0.4);
		target.setDeltaMovement(towardDir.x * pullStrength, 0.25, towardDir.z * pullStrength);
		target.hurtMarked = true;
		// slow target slightly after being dragged
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
		}
		// small immediate damage (slamming into caster range)
		float damage = (float) (6.0 * Math.max(0.1, 1.1 - powerRatio * 0.25));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cursedspeech"))), source), damage);
		// tiny backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:jujutsu"))), source), (float) (3.0 * powerRatio));
	}
}
