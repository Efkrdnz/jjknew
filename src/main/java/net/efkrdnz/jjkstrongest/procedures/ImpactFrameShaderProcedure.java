package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.Minecraft;

import java.util.List;


@EventBusSubscriber(value = Dist.CLIENT)
public class ImpactFrameShaderProcedure {
	private static final ResourceLocation CHARGED_SHADER = ResourceLocation.fromNamespaceAndPath("jjk_strongest", "shaders/post/impact_charged.json");
	private static boolean shaderLoaded = false;
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER)
			return;
		final Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		if (mc.screen != null) {
			forceShutdown(mc);
			return;
		}
		var state = ImpactFrameStateProcedure.INSTANCE;
		if (state.active && !shaderLoaded) {
			try {
				if (mc.gameRenderer.currentEffect() != null) {
					mc.gameRenderer.shutdownEffect();
				}
				mc.gameRenderer.loadEffect(CHARGED_SHADER);
				shaderLoaded = true;
			} catch (Exception e) {
				shaderLoaded = false;
			}
		}
		if (shaderLoaded && mc.gameRenderer.currentEffect() != null) {
			try {
				updateShaderUniforms(mc.gameRenderer.currentEffect(), state);
			} catch (Exception ignored) {
			}
		}
		if (!state.active && shaderLoaded) {
			forceShutdown(mc);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void updateShaderUniforms(PostChain postChain, ImpactFrameStateProcedure state) {
		try {
			// PostChain#passes is opened by the mod's access transformer; on
			// 1.20.1 this needed reflection plus an SRG-name fallback
			List<PostPass> passes = postChain.passes;
			if (passes == null)
				return;
			float prog = state.getProgress01();
			for (PostPass pass : passes) {
				EffectInstance effect = pass.getEffect();
				if (effect == null)
					continue;
				if (effect.getUniform("DesaturateAmount") != null)
					effect.safeGetUniform("DesaturateAmount").set(state.desaturateAmount);
				if (effect.getUniform("GammaBoost") != null)
					effect.safeGetUniform("GammaBoost").set(state.gammaBoost);
				if (effect.getUniform("Contrast") != null)
					effect.safeGetUniform("Contrast").set(state.contrast);
				if (effect.getUniform("RedTint") != null)
					effect.safeGetUniform("RedTint").set(state.redTint);
				if (effect.getUniform("Saturation") != null)
					effect.safeGetUniform("Saturation").set(state.saturation);
				// only set if shader actually has it
				if (effect.getUniform("Progress") != null)
					effect.safeGetUniform("Progress").set(prog);
			}
		} catch (Exception e) {
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void forceShutdown(Minecraft mc) {
		if (shaderLoaded || mc.gameRenderer.currentEffect() != null) {
			try {
				mc.gameRenderer.shutdownEffect();
			} catch (Exception ignored) {
			}
			shaderLoaded = false;
		}
	}
}
