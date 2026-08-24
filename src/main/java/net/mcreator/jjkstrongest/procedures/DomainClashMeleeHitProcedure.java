package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

@Mod.EventBusSubscriber(modid = "jjk_strongest")
public class DomainClashMeleeHitProcedure {
	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
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
