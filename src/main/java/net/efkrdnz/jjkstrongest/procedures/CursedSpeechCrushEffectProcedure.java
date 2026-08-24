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

public class CursedSpeechCrushEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 3.5)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 80, 0.3, target.getBbHeight() / 2, 0.3, 0.15);
		// slam target downward
		target.setDeltaMovement(target.getDeltaMovement().x * 0.2, -1.5, target.getDeltaMovement().z * 0.2);
		target.hurtMarked = true;
		// apply crushing effects
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (100 * Math.max(0.3, 1.3 - powerRatio * 0.3));
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 2, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false));
		}
		// crushing damage
		float damage = (float) (18.0 * Math.max(0.1, 1.2 - powerRatio * 0.35));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cursedspeech"))), source), damage);
		// backlash - arm pain
		if (source instanceof LivingEntity le && !le.level().isClientSide()) {
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false));
		}
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:jujutsu"))), source), (float) (10.0 * powerRatio));
	}
}
