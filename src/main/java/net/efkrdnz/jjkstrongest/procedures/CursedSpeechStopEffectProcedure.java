package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

public class CursedSpeechStopEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		Entity src = null;
		Entity trgt = null;
		double powerRatio = 0;
		src = source;
		trgt = target;
		powerRatio = CursedSpeechPowerCalculationProcedure.execute(src, trgt);
		if (powerRatio < 5) {
			if (world instanceof ServerLevel _level)
				_level.sendParticles(ParticleTypes.CRIT, (trgt.getX()), (trgt.getY() + trgt.getBbHeight() / 2), (trgt.getZ()), 50, 0.2, (trgt.getBbHeight()), 0.2, 0.2);
			trgt.setDeltaMovement(new Vec3(0, 0, 0));
			if (trgt instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 254, false, false));
		}
	}
}
