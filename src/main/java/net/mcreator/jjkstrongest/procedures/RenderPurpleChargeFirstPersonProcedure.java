package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

public class RenderPurpleChargeFirstPersonProcedure {
	public static void execute(Minecraft mc, Player player, PoseStack poseStack, InteractionHand hand, float partialTick) {
		if (mc == null || player == null || poseStack == null || hand == null)
			return;
		if (hand != InteractionHand.MAIN_HAND)
			return;
		if (!"purple".equals(player.getPersistentData().getString("chanting")))
			return;
		// calculate charge progress
		double cc = player.getPersistentData().getDouble("ChantCounter");
		float chargeProgress = (float) Math.min(1.0, cc / 70.0);
		if (chargeProgress <= 0.0f)
			return;
		if (JjkShaderManager.PURPLE_CHARGE_RENDER_TYPE == null)
			return;
		float timeSeconds = (player.tickCount + partialTick) / 20.0f;
		if (!JjkShaderManager.beginPurpleChargeEffect(timeSeconds, chargeProgress))
			return;
		// render fullscreen shader effect
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
		poseStack.pushPose();
		// position in front of camera
		poseStack.translate(0, 0, -0.75);
		// make it fill the screen
		float scale = 3.0f;
		poseStack.scale(scale, scale, scale);
		// get buffer source
		var bufferSource = mc.renderBuffers().bufferSource();
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.PURPLE_CHARGE_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		// render fullscreen quad
		vc.vertex(matrix, -1, -1, 0).uv(0, 1).endVertex();
		vc.vertex(matrix, 1, -1, 0).uv(1, 1).endVertex();
		vc.vertex(matrix, 1, 1, 0).uv(1, 0).endVertex();
		vc.vertex(matrix, -1, 1, 0).uv(0, 0).endVertex();
		bufferSource.endBatch(JjkShaderManager.PURPLE_CHARGE_RENDER_TYPE);
		poseStack.popPose();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}
}
