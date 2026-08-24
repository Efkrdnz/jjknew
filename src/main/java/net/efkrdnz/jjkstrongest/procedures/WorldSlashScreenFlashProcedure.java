package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.systems.RenderSystem;

@EventBusSubscriber(Dist.CLIENT)
public class WorldSlashScreenFlashProcedure {
	private static int flashTimer = 0;
	private static final int FLASH_DURATION = 10;

	// trigger screen flash
	public static void triggerFlash() {
		flashTimer = FLASH_DURATION;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiEvent.Post event) {
		if (flashTimer <= 0)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null)
			return;
		GuiGraphics guiGraphics = event.getGuiGraphics();
		int screenWidth = guiGraphics.guiWidth();
		int screenHeight = guiGraphics.guiHeight();
		// calculate flash alpha based on timer
		float alpha = (flashTimer / (float) FLASH_DURATION) * 0.7f;
		int alphaInt = (int) (alpha * 255);
		// white flash with red tint
		int color = (alphaInt << 24) | 0xFFFFFF;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.fill(0, 0, screenWidth, screenHeight, color);
		RenderSystem.disableBlend();
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		// the flash fades on the tick clock, not the render clock, so its
		// duration does not depend on framerate
		if (flashTimer > 0)
			flashTimer--;
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			// check if world slash was just executed
			if (player.getPersistentData().getBoolean("WorldSlashFlash")) {
				triggerFlash();
				player.getPersistentData().putBoolean("WorldSlashFlash", false);
			}
		}
	}
}
