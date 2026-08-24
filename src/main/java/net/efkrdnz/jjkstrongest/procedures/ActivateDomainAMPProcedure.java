package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

public class ActivateDomainAMPProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.DOMAIN_AMPLIFICATION, 9999, 1, false, false));
	}
}
