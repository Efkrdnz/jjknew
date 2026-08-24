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
public class CleaveDistortionShaderProcedure {
	private static final ResourceLocation SHADER_LOCATION = ResourceLocation.fromNamespaceAndPath("jjk_strongest", "shaders/post/cleave_distortion.json");
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
		var state = CleaveDistortionStateProcedure.INSTANCE;
		// impact has priority
		if (ImpactFrameStateProcedure.INSTANCE.active) {
			if (shaderLoaded)
				forceShutdown(mc);
			return;
		}
		if (state.active && !shaderLoaded) {
			try {
				if (mc.gameRenderer.currentEffect() != null) {
					mc.gameRenderer.shutdownEffect();
				}
				mc.gameRenderer.loadEffect(SHADER_LOCATION);
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
	private static void updateShaderUniforms(PostChain postChain, CleaveDistortionStateProcedure state) {
		try {
			// PostChain#passes is opened by the mod's access transformer; on
			// 1.20.1 this needed reflection plus an SRG-name fallback
			List<PostPass> passes = postChain.passes;
			if (passes == null)
				return;
			float progress = state.getProgress01();
			float intensity = state.intensity;
			for (PostPass pass : passes) {
				EffectInstance effect = pass.getEffect();
				if (effect == null)
					continue;
				if (effect.getUniform("DistortionIntensity") != null)
					effect.safeGetUniform("DistortionIntensity").set(intensity);
				if (effect.getUniform("SlashCount") != null)
					effect.safeGetUniform("SlashCount").set((float) state.getActiveSlashCount());
				if (effect.getUniform("Progress") != null)
					effect.safeGetUniform("Progress").set(progress);
				if (effect.getUniform("Slash1") != null)
					effect.safeGetUniform("Slash1").set(state.slash1);
				if (effect.getUniform("Slash2") != null)
					effect.safeGetUniform("Slash2").set(state.slash2);
				if (effect.getUniform("Slash3") != null)
					effect.safeGetUniform("Slash3").set(state.slash3);
				if (effect.getUniform("Slash4") != null)
					effect.safeGetUniform("Slash4").set(state.slash4);
				if (effect.getUniform("Slash5") != null)
					effect.safeGetUniform("Slash5").set(state.slash5);
				if (effect.getUniform("Slash6") != null)
					effect.safeGetUniform("Slash6").set(state.slash6);
				if (effect.getUniform("Slash7") != null)
					effect.safeGetUniform("Slash7").set(state.slash7);
				if (effect.getUniform("Slash8") != null)
					effect.safeGetUniform("Slash8").set(state.slash8);
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
