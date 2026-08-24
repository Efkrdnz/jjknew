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

public class CursedSpeechKneelEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.0)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(), target.getZ(), 30, 0.3, 0.2, 0.3, 0.1);
		// snap downward — forced to kneel
		target.setDeltaMovement(target.getDeltaMovement().x * 0.2, -1.2, target.getDeltaMovement().z * 0.2);
		target.hurtMarked = true;
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (100 * Math.max(0.3, 1.3 - powerRatio * 0.3));
			// legs give out — significant slowness + weakness
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false));
			// nausea from being slammed down
			le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
		}
		// moderate impact damage
		float damage = (float) (10.0 * Math.max(0.1, 1.1 - powerRatio * 0.3));
		target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:technique_cursedspeech"))), source), damage);
		// backlash
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (5.0 * powerRatio));
	}
}
