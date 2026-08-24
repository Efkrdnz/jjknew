package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

@EventBusSubscriber(modid = "jjk_strongest")
public class DomainClashMeleeHitProcedure {
	@SubscribeEvent
	public static void onLivingHurt(LivingDamageEvent.Pre event) {
		// only run server side
		if (event.getEntity().level().isClientSide())
			return;
		if (!(event.getEntity().level() instanceof ServerLevel serverLevel))
			return;
		Entity victim = event.getEntity();
		Entity attacker = event.getSource().getEntity();
		if (attacker == null || victim == null)
			return;
		// only count direct melee (no projectiles, no magic sources)
		if (event.getSource().getDirectEntity() != attacker)
			return;
		// forward to clash manager — it will validate ownership and clash state
		DomainClashManagerProcedure.onMeleeHitShrineOwner(serverLevel, attacker, victim);
	}
}
