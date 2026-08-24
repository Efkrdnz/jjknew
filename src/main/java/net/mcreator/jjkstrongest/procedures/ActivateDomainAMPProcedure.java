package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;

public class ActivateDomainAMPProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.DOMAIN_AMPLIFICATION.get(), 9999, 1, false, false));
	}
}
