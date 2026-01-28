package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.player.Player;

import net.mcreator.jjkstrongest.client.PurpleChargeOverlayRenderer;

public class RenderPurpleChargeOverlayProcedure {
	// sets the charge progress to render
	public static void render(Player player, float chargeProgress) {
		PurpleChargeOverlayRenderer.setChargeProgress(chargeProgress);
	}
}
