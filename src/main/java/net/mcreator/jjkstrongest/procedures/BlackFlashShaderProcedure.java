package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.Minecraft;

import java.util.List;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class BlackFlashShaderProcedure {
	private static final ResourceLocation SHADER_LOCATION = new ResourceLocation("minecraft", "shaders/post/blackflash_shatter.json");
	private static boolean shaderLoaded = false;
	private static Field passesField = null;
	static {
		// reflection setup
		try {
			passesField = PostChain.class.getDeclaredField("passes");
			passesField.setAccessible(true);
		} catch (Exception e) {
			try {
				passesField = PostChain.class.getDeclaredField("f_110008_");
				passesField.setAccessible(true);
			} catch (Exception ignored) {
			}
		}
	}

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
	@SuppressWarnings("unchecked")
	private static void updateShaderUniforms(PostChain postChain, BlackFlashShaderStateProcedure state) {
		if (passesField == null)
			return;
		try {
			List<PostPass> passes = (List<PostPass>) passesField.get(postChain);
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
