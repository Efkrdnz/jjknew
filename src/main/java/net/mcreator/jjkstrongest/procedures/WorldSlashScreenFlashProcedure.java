package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class WorldSlashScreenFlashProcedure {
	private static int flashTimer = 0;
	private static final int FLASH_DURATION = 10;

	// trigger screen flash
	public static void triggerFlash() {
		flashTimer = FLASH_DURATION;
	}

	@SubscribeEvent
	public static void onRenderOverlay(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END && flashTimer > 0) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null)
				return;
			GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
			int screenWidth = mc.getWindow().getGuiScaledWidth();
			int screenHeight = mc.getWindow().getGuiScaledHeight();
			// calculate flash alpha based on timer
			float alpha = (flashTimer / (float) FLASH_DURATION) * 0.7f;
			int alphaInt = (int) (alpha * 255);
			// white flash with red tint
			int color = (alphaInt << 24) | 0xFFFFFF;
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			guiGraphics.fill(0, 0, screenWidth, screenHeight, color);
			RenderSystem.disableBlend();
			flashTimer--;
		}
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
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
}
