package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * A Malevolent Shrine ends when its caster can no longer hold it.
 *
 * <p>An open domain has no barrier to break — that is the whole distinction — so hurting
 * the surface is not an option and the only way through it is the person projecting it.
 * This replaces a counter of twenty melee hits, which cared how often the caster was hit
 * and not at all how hard, and which ignored everything that was not a direct swing.
 */
@EventBusSubscriber(modid = "jjk_strongest")
public class DomainOwnerDamageProcedure {

	@SubscribeEvent
	public static void onLivingHurt(LivingDamageEvent.Pre event) {
		Entity victim = event.getEntity();
		if (victim == null || !(victim.level() instanceof ServerLevel serverLevel))
			return;
		float amount = event.getNewDamage();
		if (amount <= 0f)
			return;
		// The shrine's own cleave should not count against the caster's grip on it.
		Entity source = event.getSource().getEntity();
		if (source != null && source.getStringUUID().equals(victim.getStringUUID()))
			return;
		DomainClashManagerProcedure.onShrineOwnerHurt(serverLevel, victim, amount);
	}
}
