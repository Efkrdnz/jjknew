package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;

@EventBusSubscriber
public class BlackFlashQTETickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event.getEntity());
	}

	public static void execute(Entity entity) {
		if (entity == null)
			return;
		// handle cooldown
		double cooldown = entity.getPersistentData().getDouble("blackflash_qte_cooldown");
		if (cooldown > 0) {
			entity.getPersistentData().putDouble("blackflash_qte_cooldown", cooldown - 1);
		}
		// check for timeout
		if (entity.getPersistentData().getString("chanting").equals("blackflash_qte")) {
			if (entity.level().isClientSide()) {
				if (BlackFlashQTEStateProcedure.INSTANCE.hasTimedOut()) {
					// force end qte on timeout
					BlackFlashQTEStateProcedure.INSTANCE.cancelQTE();
					BlackFlashQTEMasterProcedure.onKeyRelease(entity);
				}
			}
		}
	}
}
