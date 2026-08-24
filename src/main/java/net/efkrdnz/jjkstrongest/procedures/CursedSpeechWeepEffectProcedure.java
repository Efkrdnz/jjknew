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

public class CursedSpeechWeepEffectProcedure {
	public static void execute(LevelAccessor world, Entity source, Entity target) {
		if (source == null || target == null)
			return;
		double powerRatio = CursedSpeechPowerCalculationProcedure.execute(source, target);
		if (powerRatio >= 4.0)
			return;
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.DRIPPING_WATER, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 30, 0.3, 0.1, 0.3, 0.01);
			_level.sendParticles(ParticleTypes.MYCELIUM, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 20, 0.3, 0.4, 0.3, 0.03);
		}
		// stop target in place — overcome with emotion
		target.setDeltaMovement(new Vec3(0, 0, 0));
		target.hurtMarked = true;
		if (target instanceof LivingEntity le && !le.level().isClientSide()) {
			int duration = (int) (180 * Math.max(0.3, 1.4 - powerRatio * 0.35));
			// overwhelmed — can't see, barely move, too weak to fight
			le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false));
			le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration / 2, 1, false, false));
		}
		// no direct damage — this is pure psychological control
		// backlash — emotional toll on caster too
		source.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:jujutsu"))), source), (float) (6.0 * powerRatio));
	}
}
