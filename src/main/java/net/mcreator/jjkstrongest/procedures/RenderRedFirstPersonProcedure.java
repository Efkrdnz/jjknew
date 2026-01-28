package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

public class RenderRedFirstPersonProcedure {
	private static final ResourceLocation[] RED_EMITTER_FRAMES = new ResourceLocation[]{new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_0.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_1.png"),
			new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_2.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_3.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_4.png"),
			new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_5.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_6.png")};
	private static final int EMITTER_FRAMES = 7;
	private static final int EMITTER_FRAME_TICKS = 2;

	public static void execute(Minecraft mc, Player player, PoseStack poseStack, InteractionHand hand, float partialTick) {
		if (mc == null || player == null || poseStack == null || hand == null)
			return;
		if (hand != InteractionHand.MAIN_HAND)
			return;
		if (!"red".equals(player.getPersistentData().getString("chanting")))
			return;
		double cc = player.getPersistentData().getDouble("ChantCounter");
		float chargeProgress = (float) Math.min(1.0, cc / 40.0);
		float timeSeconds = (player.tickCount + partialTick) / 20.0f;
		float scale = 0.12f + chargeProgress * 0.18f;
		float alpha = 0.35f + chargeProgress * 0.65f;
		float pulse = (float) (0.85 + 0.15 * Math.sin((player.tickCount + partialTick) * 0.6));
		scale *= pulse;
		alpha *= pulse;
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
		// layer 1: shader-based red orb
		if (JjkShaderManager.RED_ORB_RENDER_TYPE != null && JjkShaderManager.beginRedOrbEffect(timeSeconds, chargeProgress)) {
			poseStack.pushPose();
			poseStack.translate(0, 0, -0.60);
			poseStack.scale(scale, scale, scale);
			var bufferSource = mc.renderBuffers().bufferSource();
			VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.RED_ORB_RENDER_TYPE);
			Matrix4f matrix = poseStack.last().pose();
			vc.vertex(matrix, -1, -1, 0).uv(0, 1).endVertex();
			vc.vertex(matrix, 1, -1, 0).uv(1, 1).endVertex();
			vc.vertex(matrix, 1, 1, 0).uv(1, 0).endVertex();
			vc.vertex(matrix, -1, 1, 0).uv(0, 0).endVertex();
			bufferSource.endBatch(JjkShaderManager.RED_ORB_RENDER_TYPE);
			poseStack.popPose();
		}
		// layer 2: animated red emitter frames
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		poseStack.pushPose();
		poseStack.translate(0, 0, -0.60);
		float emitterScale = scale * 1.45f;
		poseStack.scale(emitterScale, emitterScale, emitterScale);
		int frame = (player.tickCount / EMITTER_FRAME_TICKS) % EMITTER_FRAMES;
		float emitterAlpha = alpha * 0.6f;
		RenderSystem.setShaderTexture(0, RED_EMITTER_FRAMES[frame]);
		RenderSystem.setShaderColor(1f, 1f, 1f, emitterAlpha);
		drawQuad(poseStack, 0f, 0f, 1f, 1f);
		poseStack.popPose();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}

	private static void drawQuad(PoseStack poseStack, float u0, float v0, float u1, float v1) {
		var mat = poseStack.last().pose();
		BufferBuilder bb = Tesselator.getInstance().getBuilder();
		bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bb.vertex(mat, -0.5f, -0.5f, 0f).uv(u0, v1).endVertex();
		bb.vertex(mat, 0.5f, -0.5f, 0f).uv(u1, v1).endVertex();
		bb.vertex(mat, 0.5f, 0.5f, 0f).uv(u1, v0).endVertex();
		bb.vertex(mat, -0.5f, 0.5f, 0f).uv(u0, v0).endVertex();
		Tesselator.getInstance().end();
	}
}
