package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

public class RenderFlameArrowFirstPersonProcedure {
	private static final ResourceLocation FIRE_ARROW = new ResourceLocation("jjk_strongest", "textures/screens/fire_arrow.png");

	public static void execute(Minecraft mc, Player player, PoseStack poseStack, InteractionHand hand, float partialTick) {
		if (mc == null || player == null || poseStack == null || hand == null)
			return;
		if (hand != InteractionHand.MAIN_HAND)
			return;
		if (!"flame_arrow".equals(player.getPersistentData().getString("chanting")))
			return;
		double cc = player.getPersistentData().getDouble("ChantCounter");
		float chargeProgress = (float) Math.min(1.0, cc / 40.0);
		float timeSeconds = (player.tickCount + partialTick) / 20.0f;
		float tx = 0.1f;
		float ty = -0.1f;
		float tz = -0.40f;
		float rx = 90f;
		float ry = 20f;
		float rz = 90f;
		float scale = 0.35f + chargeProgress * 0.15f;
		float alpha = 0.85f + chargeProgress * 0.15f;
		float pulse = (float) (0.9 + 0.1 * Math.sin((player.tickCount + partialTick) * 0.5));
		scale *= pulse;
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
		RenderSystem.disableCull();
		// layer 1: radial fire glow shader (spherical, looks good from any angle)
		if (JjkShaderManager.FLAME_ARROW_RENDER_TYPE != null && JjkShaderManager.beginFlameArrowEffect(timeSeconds, chargeProgress)) {
			var bufferSource = mc.renderBuffers().bufferSource();
			// shader quad 1
			poseStack.pushPose();
			poseStack.translate(tx, ty, tz);
			poseStack.mulPose(Axis.XP.rotationDegrees(rx));
			poseStack.mulPose(Axis.YP.rotationDegrees(ry));
			poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
			poseStack.scale(-scale, scale, scale);
			VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.FLAME_ARROW_RENDER_TYPE);
			Matrix4f matrix = poseStack.last().pose();
			vc.vertex(matrix, -0.5f, -0.5f, 0).uv(0, 1).endVertex();
			vc.vertex(matrix, 0.5f, -0.5f, 0).uv(1, 1).endVertex();
			vc.vertex(matrix, 0.5f, 0.5f, 0).uv(1, 0).endVertex();
			vc.vertex(matrix, -0.5f, 0.5f, 0).uv(0, 0).endVertex();
			bufferSource.endBatch(JjkShaderManager.FLAME_ARROW_RENDER_TYPE);
			poseStack.popPose();
			// shader quad 2 - perpendicular
			poseStack.pushPose();
			poseStack.translate(tx, ty, tz);
			poseStack.mulPose(Axis.XP.rotationDegrees(rx));
			poseStack.mulPose(Axis.YP.rotationDegrees(ry + 90f));
			poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
			poseStack.scale(-scale, scale, scale);
			vc = bufferSource.getBuffer(JjkShaderManager.FLAME_ARROW_RENDER_TYPE);
			matrix = poseStack.last().pose();
			vc.vertex(matrix, -0.5f, -0.5f, 0).uv(0, 1).endVertex();
			vc.vertex(matrix, 0.5f, -0.5f, 0).uv(1, 1).endVertex();
			vc.vertex(matrix, 0.5f, 0.5f, 0).uv(1, 0).endVertex();
			vc.vertex(matrix, -0.5f, 0.5f, 0).uv(0, 0).endVertex();
			bufferSource.endBatch(JjkShaderManager.FLAME_ARROW_RENDER_TYPE);
			poseStack.popPose();
		}
		// layer 2: arrow texture defines the actual shape (increased opacity)
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, FIRE_ARROW);
		float textureAlpha = alpha * 0.65f; // increased from 0.3 to be more visible
		RenderSystem.setShaderColor(1f, 1f, 1f, textureAlpha);
		// texture quad 1
		poseStack.pushPose();
		poseStack.translate(tx, ty, tz);
		poseStack.mulPose(Axis.XP.rotationDegrees(rx));
		poseStack.mulPose(Axis.YP.rotationDegrees(ry));
		poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
		poseStack.scale(-scale, scale, scale);
		drawQuad(poseStack);
		poseStack.popPose();
		// texture quad 2 - perpendicular for volume
		poseStack.pushPose();
		poseStack.translate(tx, ty, tz);
		poseStack.mulPose(Axis.XP.rotationDegrees(rx));
		poseStack.mulPose(Axis.YP.rotationDegrees(ry + 90f));
		poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
		poseStack.scale(-scale, scale, scale);
		drawQuad(poseStack);
		poseStack.popPose();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}

	private static void drawQuad(PoseStack poseStack) {
		var mat = poseStack.last().pose();
		BufferBuilder bb = Tesselator.getInstance().getBuilder();
		bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bb.vertex(mat, -0.5f, -0.5f, 0f).uv(0f, 1f).endVertex();
		bb.vertex(mat, 0.5f, -0.5f, 0f).uv(1f, 1f).endVertex();
		bb.vertex(mat, 0.5f, 0.5f, 0f).uv(1f, 0f).endVertex();
		bb.vertex(mat, -0.5f, 0.5f, 0f).uv(0f, 0f).endVertex();
		Tesselator.getInstance().end();
	}
}
