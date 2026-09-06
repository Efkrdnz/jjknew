package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * A Malevolent Shrine ends when its caster can no longer hold it.
 *
 * <p>An open domain has no barrier to break — that is the whole distinction — so hurting
 * the surface is not an option and the only way through it is the person projecting it.
 * This replaces a counter of twenty melee hits, which cared how often the caster was hit
 * and not at all how hard, and which ignored everything that was not a direct swing.
 *
 * <p>Deliberately on {@code LivingIncomingDamageEvent} rather than {@code LivingDamageEvent.Pre}:
 * the incoming event fires before armour, enchantment and absorption are taken off, and the
 * Pre event fires after. Reading the later one meant Sukuna's twenty points of armour — a flat
 * three-quarters — stood between every swing and his own domain, and a ten-damage hit and a
 * twenty-damage hit arrived so small that the transfer clamped both to its floor. The gradient
 * that is supposed to reward a heavier weapon was being flattened by his breastplate.
 *
 * <p>What is still in the number, and should be: blocking and reverse cursed technique. Those
 * are applied inside the caster's own {@code hurt} override before it defers upward, so they
 * are already priced in by the time any event sees the damage. His armour protects his body;
 * his guard protects his grip on the domain.
 */
@EventBusSubscriber(modid = "jjk_strongest")
public class DomainOwnerDamageProcedure {

	@SubscribeEvent
	public static void onLivingHurt(LivingIncomingDamageEvent event) {
		// Another handler on this same event cancels damage to technique-tagged entities; a
		// hit that never lands must not count against anyone's hold on their domain.
		if (event == null || event.isCanceled())
			return;
		Entity victim = event.getEntity();
		if (victim == null || !(victim.level() instanceof ServerLevel serverLevel))
			return;
		float amount = event.getAmount();
		if (amount <= 0f)
			return;
		// The shrine's own cleave should not count against the caster's grip on it.
		Entity source = event.getSource().getEntity();
		if (source != null && source.getStringUUID().equals(victim.getStringUUID()))
			return;
		DomainClashManagerProcedure.onShrineOwnerHurt(serverLevel, victim, amount);
	}
}
