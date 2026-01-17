package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexFormat;
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
		float tx = 0.1f;
		float ty = -0.1f;
		float tz = -0.40f;
		float rx = 90f;
		float ry = 20f;
		float rz = 90f;
		float scale = 0.50f;
		float alpha = 0.95f;
		float pulse = (float) (0.92 + 0.08 * Math.sin((player.tickCount + partialTick) * 0.5));
		scale *= pulse;
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, FIRE_ARROW);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		// quad 1
		poseStack.pushPose();
		poseStack.translate(tx, ty, tz);
		poseStack.mulPose(Axis.XP.rotationDegrees(rx));
		poseStack.mulPose(Axis.YP.rotationDegrees(ry));
		poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
		poseStack.scale(-scale, scale, scale);
		drawQuad(poseStack);
		poseStack.popPose();
		// quad 2
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
