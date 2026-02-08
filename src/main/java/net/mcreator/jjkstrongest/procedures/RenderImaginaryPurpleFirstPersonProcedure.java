package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

public class RenderImaginaryPurpleFirstPersonProcedure {
	public static void execute(Minecraft mc, Player player, PoseStack poseStack, InteractionHand hand, float partialTick) {
		if (mc == null || player == null || poseStack == null || hand == null)
			return;
		if (hand != InteractionHand.MAIN_HAND)
			return;
		if (!"imaginary_purple".equals(player.getPersistentData().getString("chanting")))
			return;
		// fast charge - completes in 20 ticks (1 second)
		double cc = player.getPersistentData().getDouble("ChantCounter");
		float chargeProgress = (float) Math.min(1.0, cc / 20.0);
		if (chargeProgress <= 0.0f)
			return;
		if (JjkShaderManager.IMAGINARY_PURPLE_RENDER_TYPE == null)
			return;
		float timeSeconds = (player.tickCount + partialTick) / 20.0f;
		if (!JjkShaderManager.beginImaginaryPurpleEffect(timeSeconds, chargeProgress))
			return;
		// render - smaller, less wild
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
		poseStack.pushPose();
		poseStack.translate(0.125, -0.075, -0.60);
		// smaller base scale, gentle pulse
		float scale = 0.25f + chargeProgress * 0.15f;
		float pulse = (float) (0.95 + 0.05 * Math.sin((player.tickCount + partialTick) * 0.8));
		scale *= pulse;
		poseStack.scale(scale, scale, scale);
		var bufferSource = mc.renderBuffers().bufferSource();
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.IMAGINARY_PURPLE_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		vc.vertex(matrix, -1, -1, 0).uv(0, 1).endVertex();
		vc.vertex(matrix, 1, -1, 0).uv(1, 1).endVertex();
		vc.vertex(matrix, 1, 1, 0).uv(1, 0).endVertex();
		vc.vertex(matrix, -1, 1, 0).uv(0, 0).endVertex();
		bufferSource.endBatch(JjkShaderManager.IMAGINARY_PURPLE_RENDER_TYPE);
		poseStack.popPose();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}
}
