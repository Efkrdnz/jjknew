package net.mcreator.jjkstrongest.client;

import org.joml.Matrix4f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.PurpleChargeOverlayRenderer;
import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PurpleChargeOverlayRenderer {
	private static float chargeProgress = 0.0f;
	private static boolean shouldRender = false;

	public static void setChargeProgress(float progress) {
		chargeProgress = progress;
		shouldRender = progress > 0.0f;
	}

	@SubscribeEvent
	public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
		if (!shouldRender || JjkShaderManager.PURPLE_CHARGE_RENDER_TYPE == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null)
			return;
		float timeSeconds = (player.tickCount + event.getPartialTick()) / 20.0f;
		if (!JjkShaderManager.beginPurpleChargeEffect(timeSeconds, chargeProgress))
			return;
		PoseStack poseStack = event.getGuiGraphics().pose();
		poseStack.pushPose();
		// render fullscreen quad in screen space
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.PURPLE_CHARGE_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		// fullscreen quad
		vc.vertex(matrix, 0, screenHeight, 0).uv(0, 1).endVertex();
		vc.vertex(matrix, screenWidth, screenHeight, 0).uv(1, 1).endVertex();
		vc.vertex(matrix, screenWidth, 0, 0).uv(1, 0).endVertex();
		vc.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();
		bufferSource.endBatch(JjkShaderManager.PURPLE_CHARGE_RENDER_TYPE);
		poseStack.popPose();
	}
}
