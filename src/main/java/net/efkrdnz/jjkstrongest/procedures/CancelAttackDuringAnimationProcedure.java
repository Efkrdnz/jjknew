package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import net.minecraft.world.entity.player.Player;

@EventBusSubscriber
public class CancelAttackDuringAnimationProcedure {
	// cancel server-side attack on entities
	@SubscribeEvent
	public static void onPlayerAttack(AttackEntityEvent event) {
		Player player = event.getEntity();
		String animName = player.getPersistentData().getString("current_arm_animation");
		if (!animName.isEmpty()) {
			event.setCanceled(true);
		}
	}

	// cancel left click on block
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		String animName = player.getPersistentData().getString("current_arm_animation");
		if (!animName.isEmpty()) {
			event.setCanceled(true);
		}
	}
}
