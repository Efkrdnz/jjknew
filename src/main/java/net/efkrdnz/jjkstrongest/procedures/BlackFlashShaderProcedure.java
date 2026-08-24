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
public class BlackFlashShaderProcedure {
	private static final ResourceLocation SHADER_LOCATION = ResourceLocation.fromNamespaceAndPath("jjk_strongest", "shaders/post/blackflash_shatter.json");
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
		var state = BlackFlashShaderStateProcedure.INSTANCE;
		// don't load if impact frame is active (let impact take priority)
		if (ImpactFrameStateProcedure.INSTANCE.active) {
			if (shaderLoaded) {
				forceShutdown(mc);
			}
			return;
		}
		// load shader when active
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
		// update uniforms
		if (shaderLoaded && mc.gameRenderer.currentEffect() != null) {
			try {
				updateShaderUniforms(mc.gameRenderer.currentEffect(), state);
			} catch (Exception ignored) {
			}
		}
		// unload when done
		if (!state.active && shaderLoaded) {
			forceShutdown(mc);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void updateShaderUniforms(PostChain postChain, BlackFlashShaderStateProcedure state) {
		try {
			// PostChain#passes is opened by the mod's access transformer; on
			// 1.20.1 this needed reflection plus an SRG-name fallback
			List<PostPass> passes = postChain.passes;
			if (passes == null)
				return;
			for (PostPass pass : passes) {
				EffectInstance effect = pass.getEffect();
				if (effect != null) {
					if (effect.getUniform("Intensity") != null) {
						effect.safeGetUniform("Intensity").set(state.intensity);
					}
					if (effect.getUniform("Time") != null) {
						effect.safeGetUniform("Time").set(state.time);
					}
				}
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
