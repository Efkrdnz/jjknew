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

public class RenderRedFirstPersonProcedure {
	private static final ResourceLocation RED_ORB = new ResourceLocation("jjk_strongest", "textures/screens/reversal_red_experimental.png");
	private static final ResourceLocation[] RED_EMITTER_FRAMES = new ResourceLocation[]{new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_0.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_1.png"),
			new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_2.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_3.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_4.png"),
			new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_5.png"), new ResourceLocation("jjk_strongest", "textures/screens/red_emitter_6.png")};
	// emitter strip settings
	private static final int EMITTER_FRAMES = 7; // change if needed
	private static final int EMITTER_FRAME_TICKS = 2; // animation speed

	public static void execute(Minecraft mc, Player player, PoseStack poseStack, InteractionHand hand, float partialTick) {
		if (mc == null || player == null || poseStack == null || hand == null)
			return;
		if (hand != InteractionHand.MAIN_HAND)
			return;
		if (!"red".equals(player.getPersistentData().getString("chanting")))
			return;
		double cc = player.getPersistentData().getDouble("ChantCounter");
		float charge = (float) Math.min(1.0, cc / 40.0);
		float scale = 0.12f + charge * 0.18f;
		float alpha = 0.35f + charge * 0.65f;
		float pulse = (float) (0.85 + 0.15 * Math.sin((player.tickCount + partialTick) * 0.6));
		scale *= pulse;
		alpha *= pulse;
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		// =========================
		// layer 1: spinning red orb
		// =========================
		poseStack.pushPose();
		poseStack.translate(0, 0, -0.60);
		float rot = (player.tickCount + partialTick) * 12.0f;
		poseStack.mulPose(Axis.ZP.rotationDegrees(-rot));
		poseStack.scale(scale, scale, scale);
		RenderSystem.setShaderTexture(0, RED_ORB);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		drawQuad(poseStack, 0f, 0f, 1f, 1f);
		poseStack.popPose();
		// =========================
		// layer 2: red emitter strip (bigger, no rotation)
		// =========================
		poseStack.pushPose();
		poseStack.translate(0, 0, -0.60);
		float emitterScale = scale * 1.45f; // bigger than orb
		poseStack.scale(emitterScale, emitterScale, emitterScale);
		int frame = (player.tickCount / EMITTER_FRAME_TICKS) % EMITTER_FRAMES;
		float emitterAlpha = alpha * 0.6f;
		RenderSystem.setShaderTexture(0, RED_EMITTER_FRAMES[frame]);
		RenderSystem.setShaderColor(1f, 1f, 1f, emitterAlpha);
		// full uv (no strip slicing)
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
