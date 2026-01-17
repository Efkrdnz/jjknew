package net.mcreator.jjkstrongest.client;

import org.checkerframework.checker.units.qual.h;
import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JjkShaderManager {
	public static ShaderInstance DISMANTLE_SHADER;
	public static RenderType DISMANTLE_RENDER_TYPE;
	private static TextureTarget SCENE_COPY;
	private static int lastW = -1;
	private static int lastH = -1;

	@net.minecraftforge.eventbus.api.SubscribeEvent
	public static void registerShaders(net.minecraftforge.client.event.RegisterShadersEvent event) {
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("jjk_strongest", "dismantle_slash"), DefaultVertexFormat.POSITION_TEX), shader -> {
				DISMANTLE_SHADER = shader;
				DISMANTLE_RENDER_TYPE = makeRenderType("dismantle_slash", () -> DISMANTLE_SHADER);
				System.out.println("[JJK Strongest] Dismantle shader loaded successfully");
			});
		} catch (Exception e) {
			DISMANTLE_SHADER = null;
			DISMANTLE_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] Failed to load Dismantle shader");
			e.printStackTrace();
		}
	}

	private static RenderType makeRenderType(String name, java.util.function.Supplier<ShaderInstance> shaderSup) {
		return RenderType.create(name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(shaderSup)).setDepthTestState(new RenderStateShard.DepthTestStateShard("lequal", 515)).setCullState(new RenderStateShard.CullStateShard(false))
						.setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false)).setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
							com.mojang.blaze3d.systems.RenderSystem.enableBlend();
							com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
						}, () -> {
							com.mojang.blaze3d.systems.RenderSystem.disableBlend();
							com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
						})).setOutputState(new RenderStateShard.OutputStateShard("main_target", () -> {
						}, () -> {
						})).createCompositeState(true));
	}

	public static boolean beginFrameCaptureDismantle(float timeSeconds, int style, float seed, float slashLength, float slashWidth, float r, float g, float b) {
		if (DISMANTLE_SHADER == null)
			return false;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return false;
		com.mojang.blaze3d.pipeline.RenderTarget main = mc.getMainRenderTarget();
		int w = main.width;
		int h = main.height;
		if (w <= 0 || h <= 0)
			return false;
		ensureSceneCopy(w, h);
		copyMainToSceneCopy(main, SCENE_COPY, w, h);
		try {
			DISMANTLE_SHADER.setSampler("SceneSampler", SCENE_COPY.getColorTextureId());
		} catch (Exception ignored) {
		}
		setUniformIfExists("OutSize", (float) w, (float) h);
		setUniformIfExists("Time", timeSeconds);
		setUniformIfExists("Style", (float) style);
		setUniformIfExists("Seed", seed);
		setUniformIfExists("SlashLength", slashLength);
		setUniformIfExists("SlashWidth", slashWidth);
		setUniformIfExists("RandA", r);
		setUniformIfExists("RandB", g);
		setUniformIfExists("RandC", b);
		return true;
	}

	private static void setUniformIfExists(String name, float... values) {
		var uniform = DISMANTLE_SHADER.getUniform(name);
		if (uniform != null) {
			if (values.length == 1)
				uniform.set(values[0]);
			else if (values.length == 2)
				uniform.set(values[0], values[1]);
			else if (values.length == 3)
				uniform.set(values[0], values[1], values[2]);
			else if (values.length == 4)
				uniform.set(values[0], values[1], values[2], values[3]);
		}
	}

	private static void ensureSceneCopy(int w, int h) {
		if (SCENE_COPY == null || w != lastW || h != lastH) {
			lastW = w;
			lastH = h;
			if (SCENE_COPY != null) {
				try {
					SCENE_COPY.destroyBuffers();
				} catch (Exception ignored) {
				}
			}
			SCENE_COPY = new TextureTarget(w, h, false, Minecraft.ON_OSX);
			try {
				SCENE_COPY.setFilterMode(org.lwjgl.opengl.GL11.GL_LINEAR);
			} catch (Exception ignored) {
			}
		}
	}

	private static void copyMainToSceneCopy(com.mojang.blaze3d.pipeline.RenderTarget from, com.mojang.blaze3d.pipeline.RenderTarget to, int w, int h) {
		org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER, from.frameBufferId);
		org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER, to.frameBufferId);
		org.lwjgl.opengl.GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT, org.lwjgl.opengl.GL11.GL_NEAREST);
		org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER, 0);
		org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER, 0);
		from.bindWrite(true);
	}
}
