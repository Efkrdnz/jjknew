package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.entity.Entity;

@Mod.EventBusSubscriber
public class BlackFlashQTETickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event.player);
		}
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
