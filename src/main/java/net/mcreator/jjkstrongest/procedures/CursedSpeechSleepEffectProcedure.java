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

public class CursedSpeechSleepEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.0)
			return;
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.MYCELIUM, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 30, 0.3, 0.3, 0.3, 0.02);
		// zero out movement — target collapses
		target.setDeltaMovement(new Vec3(0, 0, 0));
		target.hurtMarked = true;
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (160 * Math.max(0.4, 1.4 - powerRatio * 0.3));
			// blindness simulates eyes closing
			le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false));
			// full movement stop
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 254, false, false));
			// nausea on wake-up disorientation
			le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration + 40, 0, false, false));
			// weakness while asleep
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2, false, false));
		}
		// small backlash (throat strain)
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), source), (float) (5.0 * powerRatio));
	}
}
