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
	public static ShaderInstance HOLLOW_PURPLE_SHADER;
	public static RenderType HOLLOW_PURPLE_RENDER_TYPE;
	private static TextureTarget PURPLE_SCENE_COPY;
	private static int purpleLastW = -1;
	private static int purpleLastH = -1;
	public static ShaderInstance PURPLE_CHARGE_SHADER;
	public static RenderType PURPLE_CHARGE_RENDER_TYPE;
	public static ShaderInstance RED_ORB_SHADER;
	public static RenderType RED_ORB_RENDER_TYPE;
	public static ShaderInstance BLUE_ORB_SHADER;
	public static RenderType BLUE_ORB_RENDER_TYPE;
	public static ShaderInstance FLAME_ARROW_SHADER;
	public static RenderType FLAME_ARROW_RENDER_TYPE;

	@net.minecraftforge.eventbus.api.SubscribeEvent
	public static void registerShaders(net.minecraftforge.client.event.RegisterShadersEvent event) {
		// dismantle shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Dismantle shader...");
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
		// hollow purple shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Hollow Purple shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("jjk_strongest", "hollow_purple"), DefaultVertexFormat.POSITION_TEX), shader -> {
				HOLLOW_PURPLE_SHADER = shader;
				HOLLOW_PURPLE_RENDER_TYPE = makeRenderType("hollow_purple", () -> HOLLOW_PURPLE_SHADER);
				System.out.println("[JJK Strongest] ✓ Hollow Purple shader loaded successfully!");
			});
		} catch (Exception e) {
			HOLLOW_PURPLE_SHADER = null;
			HOLLOW_PURPLE_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Hollow Purple shader:");
			System.err.println("[JJK Strongest] Error type: " + e.getClass().getName());
			System.err.println("[JJK Strongest] Error message: " + e.getMessage());
			e.printStackTrace();
		}
		// purple charge shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Purple Charge shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("jjk_strongest", "purple_charge"), DefaultVertexFormat.POSITION_TEX), shader -> {
				PURPLE_CHARGE_SHADER = shader;
				PURPLE_CHARGE_RENDER_TYPE = makeRenderType("purple_charge", () -> PURPLE_CHARGE_SHADER);
				System.out.println("[JJK Strongest] ✓ Purple Charge shader loaded successfully!");
			});
		} catch (Exception e) {
			PURPLE_CHARGE_SHADER = null;
			PURPLE_CHARGE_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Purple Charge shader");
			e.printStackTrace();
		}
		// red orb shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Red Orb shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("jjk_strongest", "red_orb"), DefaultVertexFormat.POSITION_TEX), shader -> {
				RED_ORB_SHADER = shader;
				RED_ORB_RENDER_TYPE = makeRenderType("red_orb", () -> RED_ORB_SHADER);
				System.out.println("[JJK Strongest] ✓ Red Orb shader loaded successfully!");
			});
		} catch (Exception e) {
			RED_ORB_SHADER = null;
			RED_ORB_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Red Orb shader");
			e.printStackTrace();
		}
		// blue orb shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Blue Orb shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("jjk_strongest", "blue_orb"), DefaultVertexFormat.POSITION_TEX), shader -> {
				BLUE_ORB_SHADER = shader;
				BLUE_ORB_RENDER_TYPE = makeRenderType("blue_orb", () -> BLUE_ORB_SHADER);
				System.out.println("[JJK Strongest] ✓ Blue Orb shader loaded successfully!");
			});
		} catch (Exception e) {
			BLUE_ORB_SHADER = null;
			BLUE_ORB_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Blue Orb shader");
			e.printStackTrace();
		}
		try {
			System.out.println("[JJK Strongest] Attempting to load Flame Arrow shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("jjk_strongest", "flame_arrow"), DefaultVertexFormat.POSITION_TEX), shader -> {
				FLAME_ARROW_SHADER = shader;
				FLAME_ARROW_RENDER_TYPE = makeRenderType("flame_arrow", () -> FLAME_ARROW_SHADER);
				System.out.println("[JJK Strongest] ✓ Flame Arrow shader loaded successfully!");
			});
		} catch (Exception e) {
			FLAME_ARROW_SHADER = null;
			FLAME_ARROW_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Flame Arrow shader");
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
		setUniformIfExistsDismantle("OutSize", (float) w, (float) h);
		setUniformIfExistsDismantle("Time", timeSeconds);
		setUniformIfExistsDismantle("Style", (float) style);
		setUniformIfExistsDismantle("Seed", seed);
		setUniformIfExistsDismantle("SlashLength", slashLength);
		setUniformIfExistsDismantle("SlashWidth", slashWidth);
		setUniformIfExistsDismantle("RandA", r);
		setUniformIfExistsDismantle("RandB", g);
		setUniformIfExistsDismantle("RandC", b);
		return true;
	}

	public static boolean beginFrameCaptureHollowPurple(float timeSeconds, float intensity, float radius, float distortionStrength) {
		if (HOLLOW_PURPLE_SHADER == null) {
			System.err.println("[JJK Strongest] Cannot capture frame - Hollow Purple shader is null!");
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return false;
		com.mojang.blaze3d.pipeline.RenderTarget main = mc.getMainRenderTarget();
		int w = main.width;
		int h = main.height;
		if (w <= 0 || h <= 0)
			return false;
		ensureSceneCopyPurple(w, h);
		copyMainToSceneCopy(main, PURPLE_SCENE_COPY, w, h);
		try {
			HOLLOW_PURPLE_SHADER.setSampler("SceneSampler", PURPLE_SCENE_COPY.getColorTextureId());
		} catch (Exception ignored) {
		}
		setUniformIfExistsPurple("Time", timeSeconds);
		setUniformIfExistsPurple("Intensity", intensity);
		setUniformIfExistsPurple("Radius", radius);
		setUniformIfExistsPurple("DistortStrength", distortionStrength);
		return true;
	}

	public static boolean beginPurpleChargeEffect(float timeSeconds, float chargeProgress) {
		if (PURPLE_CHARGE_SHADER == null)
			return false;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return false;
		com.mojang.blaze3d.pipeline.RenderTarget main = mc.getMainRenderTarget();
		int w = main.width;
		int h = main.height;
		if (w <= 0 || h <= 0)
			return false;
		ensureSceneCopyPurple(w, h);
		copyMainToSceneCopy(main, PURPLE_SCENE_COPY, w, h);
		try {
			PURPLE_CHARGE_SHADER.setSampler("SceneSampler", PURPLE_SCENE_COPY.getColorTextureId());
		} catch (Exception ignored) {
		}
		setUniformIfExistsPurpleCharge("Time", timeSeconds);
		setUniformIfExistsPurpleCharge("ChargeProgress", chargeProgress);
		setUniformIfExistsPurpleCharge("RedPosX", 0.35f);
		setUniformIfExistsPurpleCharge("RedPosY", 0.5f);
		setUniformIfExistsPurpleCharge("BluePosX", 0.65f);
		setUniformIfExistsPurpleCharge("BluePosY", 0.5f);
		return true;
	}

	public static boolean beginRedOrbEffect(float timeSeconds, float chargeProgress) {
		if (RED_ORB_SHADER == null)
			return false;
		setUniformIfExistsRedOrb("Time", timeSeconds);
		setUniformIfExistsRedOrb("ChargeProgress", chargeProgress);
		return true;
	}

	public static boolean beginBlueOrbEffect(float timeSeconds, float chargeProgress) {
		if (BLUE_ORB_SHADER == null)
			return false;
		setUniformIfExistsBlueOrb("Time", timeSeconds);
		setUniformIfExistsBlueOrb("ChargeProgress", chargeProgress);
		return true;
	}

	public static boolean beginFlameArrowEffect(float timeSeconds, float chargeProgress) {
		if (FLAME_ARROW_SHADER == null)
			return false;
		setUniformIfExistsFlameArrow("Time", timeSeconds);
		setUniformIfExistsFlameArrow("ChargeProgress", chargeProgress);
		return true;
	}

	private static void setUniformIfExistsFlameArrow(String name, float... values) {
		var uniform = FLAME_ARROW_SHADER.getUniform(name);
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

	private static void setUniformIfExistsDismantle(String name, float... values) {
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

	private static void setUniformIfExistsPurple(String name, float... values) {
		var uniform = HOLLOW_PURPLE_SHADER.getUniform(name);
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

	private static void setUniformIfExistsPurpleCharge(String name, float... values) {
		var uniform = PURPLE_CHARGE_SHADER.getUniform(name);
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

	private static void setUniformIfExistsRedOrb(String name, float... values) {
		var uniform = RED_ORB_SHADER.getUniform(name);
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

	private static void setUniformIfExistsBlueOrb(String name, float... values) {
		var uniform = BLUE_ORB_SHADER.getUniform(name);
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

	private static void ensureSceneCopyPurple(int w, int h) {
		if (PURPLE_SCENE_COPY == null || w != purpleLastW || h != purpleLastH) {
			purpleLastW = w;
			purpleLastH = h;
			if (PURPLE_SCENE_COPY != null) {
				try {
					PURPLE_SCENE_COPY.destroyBuffers();
				} catch (Exception ignored) {
				}
			}
			PURPLE_SCENE_COPY = new TextureTarget(w, h, false, Minecraft.ON_OSX);
			try {
				PURPLE_SCENE_COPY.setFilterMode(org.lwjgl.opengl.GL11.GL_LINEAR);
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
