package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import net.minecraft.world.entity.player.Player;

@Mod.EventBusSubscriber
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
