package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

public class DeactivateDomainAMPProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(JjkStrongestModMobEffects.DOMAIN_AMPLIFICATION)) {
			if (entity instanceof LivingEntity _entity)
				_entity.removeEffect(JjkStrongestModMobEffects.DOMAIN_AMPLIFICATION);
		}
	}
}
