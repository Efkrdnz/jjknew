package net.efkrdnz.jjkstrongest.client;

import org.joml.Matrix4f;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffectInstance;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * The screen layer for Information Overload.
 *
 * <p>Where the difficulty lives now. The effect used to work by setting fog to pure black
 * a block from your face, which meant everyone the domain caught saw nothing at all — the
 * domain included. The fog has been softened to a heavy veil that still reaches the
 * barrier, and the disorientation moved here, on top of the world rather than instead of
 * it.
 *
 * <p>The shader this draws was already written, registered and complete; nothing had ever
 * fetched its render type.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class InformationOverloadOverlayRenderer {

	/** Ticks of ramp at each end, so it neither pops in nor cuts out. */
	private static final float FADE_TICKS = 12.0f;

	@SubscribeEvent
	public static void onRenderGui(RenderGuiEvent.Pre event) {
		if (JjkShaderManager.INFORMATION_OVERLOAD_OVERLAY_RENDER_TYPE == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || player.isSpectator())
			return;

		MobEffectInstance overload = player.getEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD);
		if (overload == null)
			return;

		float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		float remaining = overload.getDuration() - partial;
		if (remaining <= 0.0f)
			return;

		// Ramp up over the first few ticks of the effect and back down over its last few.
		// Duration is not exposed as a total, so the rise is inferred from how long it has
		// been running rather than how long it has left.
		float elapsed = Math.max(0.0f, 200.0f - remaining);
		float fade = Math.min(1.0f, Math.min(elapsed, remaining) / FADE_TICKS);
		if (fade <= 0.01f)
			return;

		float strength = Math.min(1.0f, 0.85f + overload.getAmplifier() * 0.15f);
		float timeSeconds = (player.tickCount + partial) / 20.0f;
		if (!JjkShaderManager.beginInformationOverloadOverlayEffect(timeSeconds, strength, fade))
			return;

		PoseStack poseStack = event.getGuiGraphics().pose();
		poseStack.pushPose();
		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.INFORMATION_OVERLOAD_OVERLAY_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		vc.addVertex(matrix, 0, height, 0).setUv(0, 1);
		vc.addVertex(matrix, width, height, 0).setUv(1, 1);
		vc.addVertex(matrix, width, 0, 0).setUv(1, 0);
		vc.addVertex(matrix, 0, 0, 0).setUv(0, 0);
		bufferSource.endBatch(JjkShaderManager.INFORMATION_OVERLOAD_OVERLAY_RENDER_TYPE);
		poseStack.popPose();
	}
}
