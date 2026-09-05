package net.efkrdnz.jjkstrongest.client;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.client.JjkShaderManager;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;

@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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
	public static ShaderInstance BLUE_VORTEX_SHADER;
	public static RenderType BLUE_VORTEX_RENDER_TYPE;
	public static ShaderInstance FLAME_ARROW_SHADER;
	public static RenderType FLAME_ARROW_RENDER_TYPE;
	public static ShaderInstance FLAME_ARROW_EXPLOSION_SHADER;
	public static RenderType FLAME_ARROW_EXPLOSION_RENDER_TYPE;
	public static ShaderInstance IMAGINARY_PURPLE_SHADER;
	public static RenderType IMAGINARY_PURPLE_RENDER_TYPE;
	public static ShaderInstance UV_INTERIOR_SHADER;
	public static RenderType UV_INTERIOR_RENDER_TYPE;
	/**
	 * The same shader, colour only.
	 *
	 * <p>The interior writes depth, which is what makes it occlude the world behind it. A
	 * dome fading out during a collapse must not do that — at alpha 0.3 it would still punch
	 * an opaque hole in everything behind it.
	 */
	public static RenderType UV_INTERIOR_COLLAPSE_RENDER_TYPE;
	/** The sea: same shader as the dome, with Surface = 1. Writes depth, so it hides the pit. */
	public static RenderType UV_FLOOR_RENDER_TYPE;
	public static ShaderInstance UV_SHARDS_SHADER;
	public static RenderType UV_SHARDS_RENDER_TYPE;
	public static ShaderInstance IMAGINARY_PURPLE_PROJECTILE_SHADER;
	public static RenderType IMAGINARY_PURPLE_PROJECTILE_RENDER_TYPE;
	public static ShaderInstance INFORMATION_OVERLOAD_OVERLAY_SHADER;
	public static RenderType INFORMATION_OVERLOAD_OVERLAY_RENDER_TYPE;
	public static ShaderInstance FUGA_DOMAIN_EXPLOSION_SHADER;
	public static RenderType FUGA_DOMAIN_EXPLOSION_RENDER_TYPE;
	public static ShaderInstance HOLLOW_NUKE_SHADER;
	public static RenderType HOLLOW_NUKE_RENDER_TYPE;
	public static ShaderInstance LAPSE_BLUE_LIQUID_SHADER;
	public static RenderType LAPSE_BLUE_LIQUID_RENDER_TYPE;

	@net.neoforged.bus.api.SubscribeEvent
	public static void registerShaders(net.neoforged.neoforge.client.event.RegisterShadersEvent event) {
		// dismantle shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Dismantle shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "dismantle_slash"), DefaultVertexFormat.POSITION_TEX), shader -> {
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
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "hollow_purple"), DefaultVertexFormat.POSITION_TEX), shader -> {
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
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "purple_charge"), DefaultVertexFormat.POSITION_TEX), shader -> {
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
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "red_orb"), DefaultVertexFormat.POSITION_TEX), shader -> {
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
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "blue_orb"), DefaultVertexFormat.POSITION_TEX), shader -> {
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
			System.out.println("[JJK Strongest] Attempting to load Blue Vortex shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "blue_vortex"), DefaultVertexFormat.POSITION_TEX), shader -> {
				BLUE_VORTEX_SHADER = shader;
				BLUE_VORTEX_RENDER_TYPE = makeAdditiveRenderType("blue_vortex", () -> BLUE_VORTEX_SHADER);
				System.out.println("[JJK Strongest] ✓ Blue Vortex shader loaded successfully!");
			});
		} catch (Exception e) {
			BLUE_VORTEX_SHADER = null;
			BLUE_VORTEX_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Blue Vortex shader");
			e.printStackTrace();
		}
		try {
			System.out.println("[JJK Strongest] Attempting to load Flame Arrow shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "flame_arrow"), DefaultVertexFormat.POSITION_TEX), shader -> {
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
		try {
			System.out.println("[JJK Strongest] Attempting to load Flame Arrow Explosion shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "flame_arrow_explosion"), DefaultVertexFormat.POSITION_TEX), shader -> {
				FLAME_ARROW_EXPLOSION_SHADER = shader;
				FLAME_ARROW_EXPLOSION_RENDER_TYPE = makeRenderType("flame_arrow_explosion", () -> FLAME_ARROW_EXPLOSION_SHADER);
				System.out.println("[JJK Strongest] ✓ Flame Arrow Explosion shader loaded successfully!");
			});
		} catch (Exception e) {
			FLAME_ARROW_EXPLOSION_SHADER = null;
			FLAME_ARROW_EXPLOSION_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Flame Arrow Explosion shader");
			e.printStackTrace();
		}
		try {
			System.out.println("[JJK Strongest] Attempting to load Imaginary Purple shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "imaginary_purple"), DefaultVertexFormat.POSITION_TEX), shader -> {
				IMAGINARY_PURPLE_SHADER = shader;
				IMAGINARY_PURPLE_RENDER_TYPE = makeRenderType("imaginary_purple", () -> IMAGINARY_PURPLE_SHADER);
				System.out.println("[JJK Strongest] ✓ Imaginary Purple shader loaded successfully!");
			});
		} catch (Exception e) {
			IMAGINARY_PURPLE_SHADER = null;
			IMAGINARY_PURPLE_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Imaginary Purple shader");
			e.printStackTrace();
		}
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "uv_interior"), DefaultVertexFormat.POSITION_TEX), shader -> {
				UV_INTERIOR_SHADER = shader;
				UV_INTERIOR_RENDER_TYPE = makeDomainSurfaceRenderType("uv_interior", () -> UV_INTERIOR_SHADER, true);
				UV_INTERIOR_COLLAPSE_RENDER_TYPE = makeDomainSurfaceRenderType("uv_interior_collapse", () -> UV_INTERIOR_SHADER, false);
				UV_FLOOR_RENDER_TYPE = makeDomainSurfaceRenderType("uv_floor", () -> UV_INTERIOR_SHADER, true);
			});
		} catch (Exception e) {
			UV_INTERIOR_SHADER = null;
			UV_INTERIOR_RENDER_TYPE = null;
			UV_INTERIOR_COLLAPSE_RENDER_TYPE = null;
			UV_FLOOR_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] \u2717 Failed to load the domain interior shader");
			e.printStackTrace();
		}
		try {
			System.out.println("[JJK Strongest] Attempting to load Imaginary Purple Projectile shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "imaginary_purple_projectile"), DefaultVertexFormat.POSITION_TEX), shader -> {
				IMAGINARY_PURPLE_PROJECTILE_SHADER = shader;
				IMAGINARY_PURPLE_PROJECTILE_RENDER_TYPE = makeRenderType("imaginary_purple_projectile", () -> IMAGINARY_PURPLE_PROJECTILE_SHADER);
				System.out.println("[JJK Strongest] ✓ Imaginary Purple Projectile shader loaded successfully!");
			});
		} catch (Exception e) {
			IMAGINARY_PURPLE_PROJECTILE_SHADER = null;
			IMAGINARY_PURPLE_PROJECTILE_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Imaginary Purple Projectile shader");
			e.printStackTrace();
		}
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "uv_shards"), DefaultVertexFormat.POSITION_TEX), shader -> {
				UV_SHARDS_SHADER = shader;
				// Colour only. The pass is unsorted and every shard is fading, so depth
				// writes would let a near piece permanently occlude a far one's fade.
				UV_SHARDS_RENDER_TYPE = makeDomainSurfaceRenderType("uv_shards", () -> UV_SHARDS_SHADER, false);
			});
		} catch (Exception e) {
			UV_SHARDS_SHADER = null;
			UV_SHARDS_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] \u2717 Failed to load the domain shard shader");
			e.printStackTrace();
		}
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "information_overload_overlay"), DefaultVertexFormat.POSITION_TEX), shader -> {
				INFORMATION_OVERLOAD_OVERLAY_SHADER = shader;
				INFORMATION_OVERLOAD_OVERLAY_RENDER_TYPE = makeRenderType("information_overload_overlay", () -> INFORMATION_OVERLOAD_OVERLAY_SHADER);
			});
		} catch (Exception e) {
			INFORMATION_OVERLOAD_OVERLAY_SHADER = null;
			INFORMATION_OVERLOAD_OVERLAY_RENDER_TYPE = null;
		}
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "fuga_domain_explosion"), DefaultVertexFormat.POSITION_TEX), shader -> {
				FUGA_DOMAIN_EXPLOSION_SHADER = shader;
				FUGA_DOMAIN_EXPLOSION_RENDER_TYPE = makeAdditiveRenderType("fuga_domain_explosion", () -> FUGA_DOMAIN_EXPLOSION_SHADER);
			});
		} catch (Exception e) {
			FUGA_DOMAIN_EXPLOSION_SHADER = null;
			FUGA_DOMAIN_EXPLOSION_RENDER_TYPE = null;
		}
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "hollow_nuke"), DefaultVertexFormat.POSITION_TEX), shader -> {
				HOLLOW_NUKE_SHADER = shader;
				HOLLOW_NUKE_RENDER_TYPE = makeAdditiveRenderType("hollow_nuke", () -> HOLLOW_NUKE_SHADER);
			});
		} catch (Exception e) {
			HOLLOW_NUKE_SHADER = null;
			HOLLOW_NUKE_RENDER_TYPE = null;
		}
		// lapse blue liquid shader
		try {
			System.out.println("[JJK Strongest] Attempting to load Lapse Blue Liquid shader...");
			event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("jjk_strongest", "lapse_blue_liquid"), DefaultVertexFormat.POSITION_TEX), shader -> {
				LAPSE_BLUE_LIQUID_SHADER = shader;
				LAPSE_BLUE_LIQUID_RENDER_TYPE = makeAdditiveRenderType("lapse_blue_liquid", () -> LAPSE_BLUE_LIQUID_SHADER);
				System.out.println("[JJK Strongest] ✓ Lapse Blue Liquid shader loaded successfully!");
			});
		} catch (Exception e) {
			LAPSE_BLUE_LIQUID_SHADER = null;
			LAPSE_BLUE_LIQUID_RENDER_TYPE = null;
			System.err.println("[JJK Strongest] ✗ Failed to load Lapse Blue Liquid shader");
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

	public static boolean beginBlueVortexEffect(float timeSeconds, float intensity) {
		if (BLUE_VORTEX_SHADER == null)
			return false;
		setUniformIfExistsBlueVortex("Time", timeSeconds);
		setUniformIfExistsBlueVortex("Intensity", intensity);
		return true;
	}

	public static boolean beginFlameArrowEffect(float timeSeconds, float chargeProgress) {
		if (FLAME_ARROW_SHADER == null)
			return false;
		setUniformIfExistsFlameArrow("Time", timeSeconds);
		setUniformIfExistsFlameArrow("ChargeProgress", chargeProgress);
		return true;
	}

	public static boolean beginImaginaryPurpleEffect(float timeSeconds, float chargeProgress) {
		if (IMAGINARY_PURPLE_SHADER == null)
			return false;
		setUniformIfExistsImaginaryPurple("Time", timeSeconds);
		setUniformIfExistsImaginaryPurple("ChargeProgress", chargeProgress);
		return true;
	}



	/**
	 * The domain interior: shell, dome, floor and black hole, all from one shader on one
	 * mesh.
	 *
	 * <p>Reports any uniform the shader did not actually take, once. That is not
	 * defensiveness for its own sake: {@link #setUniform} swallows a null lookup, so a name
	 * that disagrees between the {@code .json} and the {@code .fsh} compiles cleanly, sets
	 * nothing, and leaves you staring at a default-valued black hole with no error anywhere
	 * to explain it.
	 *
	 * @param radius       the real radius; the shader multiplies the unit-sphere position
	 *                     by it to get anything measured in blocks
	 * @param camX/Y/Z     camera relative to the sphere centre, in blocks
	 * @param floorY       floor plane relative to the centre
	 * @param inside       1 when the camera is within the shell
	 * @param integrity    whole-barrier integrity, 0..1
	 * @param shellTexture GL id of the per-direction damage grid, or -1 if none yet
	 * @param surface      0 for the dome, 1 for the floor disc — one shader, two surfaces
	 * @param ripples      {@link net.efkrdnz.jjkstrongest.domain.RippleField#FLOATS} floats of
	 *                     packed ripples for the floor, or null for a still sea
	 */
	public static boolean beginUvInterior(float timeSeconds, float seed, float intensity, float radius, float progress, float phase, float camX, float camY, float camZ, float floorY, boolean inside,
			float bhX, float bhY, float bhZ, float bhAngularRadius, float bhDistance, float axisX, float axisY, float axisZ, float discStrength, float integrity, int shellTexture, float surface,
			float[] ripples) {
		if (UV_INTERIOR_SHADER == null)
			return false;
		setUniform(UV_INTERIOR_SHADER, "Surface", surface);
		setUniformArray(UV_INTERIOR_SHADER, "RippleData", ripples == null ? NO_RIPPLES : ripples);
		setUniform(UV_INTERIOR_SHADER, "Time", timeSeconds);
		setUniform(UV_INTERIOR_SHADER, "BrushSeed", seed);
		setUniform(UV_INTERIOR_SHADER, "Intensity", intensity);
		setUniform(UV_INTERIOR_SHADER, "Radius", radius);
		setUniform(UV_INTERIOR_SHADER, "Progress", progress);
		setUniform(UV_INTERIOR_SHADER, "Phase", phase);
		setUniform(UV_INTERIOR_SHADER, "CamOffset", camX, camY, camZ);
		setUniform(UV_INTERIOR_SHADER, "FloorY", floorY);
		setUniform(UV_INTERIOR_SHADER, "Inside", inside ? 1.0f : 0.0f);
		setUniform(UV_INTERIOR_SHADER, "BhDir", bhX, bhY, bhZ);
		setUniform(UV_INTERIOR_SHADER, "BhAng", bhAngularRadius, bhDistance);
		setUniform(UV_INTERIOR_SHADER, "BhAxis", axisX, axisY, axisZ);
		setUniform(UV_INTERIOR_SHADER, "DiscStrength", discStrength);
		setUniform(UV_INTERIOR_SHADER, "Integrity", integrity);
		// Without a grid the sampler reads black, which would mean "totally destroyed" —
		// the exact opposite of the truth. Flag it so the shader ignores the sampler.
		setUniform(UV_INTERIOR_SHADER, "HasShell", shellTexture >= 0 ? 1.0f : 0.0f);
		if (shellTexture >= 0) {
			try {
				UV_INTERIOR_SHADER.setSampler("ShellSampler", shellTexture);
			} catch (Exception ignored) {
			}
		}
		reportMissingUniformsOnce(UV_INTERIOR_SHADER, "uv_interior", "Time", "BrushSeed", "Intensity", "Radius", "Progress", "Phase", "CamOffset", "FloorY", "Inside", "BhDir", "BhAng", "BhAxis",
				"DiscStrength", "Integrity", "HasShell", "Surface", "RippleData");
		return true;
	}

	/** The broken shell, mid-collapse. */
	public static boolean beginUvShards(float timeSeconds, float seed, float intensity, float radius, float progress, float collapseSeconds, float breakX, float breakY, float breakZ, float camX,
			float camY, float camZ, float integrity, int shellTexture) {
		if (UV_SHARDS_SHADER == null)
			return false;
		setUniform(UV_SHARDS_SHADER, "Time", timeSeconds);
		setUniform(UV_SHARDS_SHADER, "BrushSeed", seed);
		setUniform(UV_SHARDS_SHADER, "Intensity", intensity);
		setUniform(UV_SHARDS_SHADER, "Radius", radius);
		setUniform(UV_SHARDS_SHADER, "Progress", progress);
		setUniform(UV_SHARDS_SHADER, "CollapseSeconds", collapseSeconds);
		setUniform(UV_SHARDS_SHADER, "BreakDir", breakX, breakY, breakZ);
		setUniform(UV_SHARDS_SHADER, "CamOffset", camX, camY, camZ);
		setUniform(UV_SHARDS_SHADER, "Integrity", integrity);
		setUniform(UV_SHARDS_SHADER, "HasShell", shellTexture >= 0 ? 1.0f : 0.0f);
		if (shellTexture >= 0) {
			try {
				UV_SHARDS_SHADER.setSampler("ShellSampler", shellTexture);
			} catch (Exception ignored) {
			}
		}
		reportMissingUniformsOnce(UV_SHARDS_SHADER, "uv_shards", "Time", "BrushSeed", "Intensity", "Radius", "Progress", "CollapseSeconds", "BreakDir", "CamOffset", "Integrity", "HasShell");
		return true;
	}

	/**
	 * One shared uniform setter, replacing fifteen copy-pasted ones that differed only in
	 * which {@code ShaderInstance} they read.
	 */
	private static void setUniform(ShaderInstance shader, String name, float... values) {
		if (shader == null)
			return;
		var uniform = shader.getUniform(name);
		if (uniform == null)
			return;
		if (values.length == 1)
			uniform.set(values[0]);
		else if (values.length == 2)
			uniform.set(values[0], values[1]);
		else if (values.length == 3)
			uniform.set(values[0], values[1], values[2]);
		else if (values.length == 4)
			uniform.set(values[0], values[1], values[2], values[3]);
	}

	/** A still sea: every slot zero strength. Shared, never written. */
	private static final float[] NO_RIPPLES = new float[net.efkrdnz.jjkstrongest.domain.RippleField.FLOATS];

	/**
	 * Uploads a whole float array to a {@code uniform float Name[N]}.
	 *
	 * <p>Vanilla's json declares these as type float with a count above four; the parser
	 * accepts a single default value for the lot, and the uniform uploads with
	 * {@code glUniform1fv} over its whole buffer, which is exactly what a GLSL float array
	 * wants. {@code Uniform#set(float[])} refuses an array shorter than the count, so the
	 * caller's array is the contract.
	 */
	private static void setUniformArray(ShaderInstance shader, String name, float[] values) {
		if (shader == null)
			return;
		var uniform = shader.getUniform(name);
		if (uniform == null)
			return;
		uniform.set(values);
	}

	private static final java.util.Set<String> REPORTED_UNIFORMS = new java.util.HashSet<>();

	/**
	 * Says once, in the log, which declared uniforms the linked shader does not have.
	 *
	 * <p>A name that disagrees between the json and the fsh, or one the driver optimised
	 * away because nothing reads it, both come back null and both are silent. Without this
	 * the only symptom is a feature that quietly does not work.
	 */
	private static void reportMissingUniformsOnce(ShaderInstance shader, String label, String... names) {
		if (shader == null || !REPORTED_UNIFORMS.add(label))
			return;
		StringBuilder missing = new StringBuilder();
		for (String name : names) {
			if (shader.getUniform(name) == null)
				missing.append(missing.length() == 0 ? "" : ", ").append(name);
		}
		if (missing.length() > 0)
			System.err.println("[JJK Strongest] " + label + " is not taking these uniforms (name mismatch, or unused and optimised out): " + missing);
	}

	public static boolean beginImaginaryPurpleProjectileEffect(float timeSeconds, float intensity) {
		if (IMAGINARY_PURPLE_PROJECTILE_SHADER == null)
			return false;
		setUniformIfExistsImaginaryPurpleProjectile("Time", timeSeconds);
		setUniformIfExistsImaginaryPurpleProjectile("Intensity", intensity);
		return true;
	}

	public static boolean beginInformationOverloadOverlayEffect(float timeSeconds, float strength) {
		return beginInformationOverloadOverlayEffect(timeSeconds, strength, 1.0f);
	}

	public static boolean beginInformationOverloadOverlayEffect(float timeSeconds, float strength, float alpha) {
		if (INFORMATION_OVERLOAD_OVERLAY_SHADER == null)
			return false;
		setUniformIfExistsInformationOverloadOverlay("Time", timeSeconds);
		setUniformIfExistsInformationOverloadOverlay("Strength", strength);
		setUniformIfExistsInformationOverloadOverlay("Alpha", alpha);
		return true;
	}

	public static boolean beginFugaDomainExplosionEffect(float timeSeconds, float fade, float progress) {
		if (FUGA_DOMAIN_EXPLOSION_SHADER == null)
			return false;
		var u1 = FUGA_DOMAIN_EXPLOSION_SHADER.getUniform("Time");
		if (u1 != null)
			u1.set(timeSeconds);
		var u2 = FUGA_DOMAIN_EXPLOSION_SHADER.getUniform("ChargeProgress");
		if (u2 != null)
			u2.set(fade);
		var u3 = FUGA_DOMAIN_EXPLOSION_SHADER.getUniform("Progress");
		if (u3 != null)
			u3.set(progress);
		return true;
	}

	public static boolean beginHollowNukeEffect(float timeSeconds, float lifeTicks, float seed) {
		if (HOLLOW_NUKE_SHADER == null)
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
			HOLLOW_NUKE_SHADER.setSampler("SceneSampler", PURPLE_SCENE_COPY.getColorTextureId());
		} catch (Exception ignored) {
		}
		var u0 = HOLLOW_NUKE_SHADER.getUniform("OutSize");
		if (u0 != null)
			u0.set((float) w, (float) h);
		var u1 = HOLLOW_NUKE_SHADER.getUniform("Time");
		if (u1 != null)
			u1.set(timeSeconds);
		var u2 = HOLLOW_NUKE_SHADER.getUniform("Life");
		if (u2 != null)
			u2.set(lifeTicks);
		var u3 = HOLLOW_NUKE_SHADER.getUniform("Seed");
		if (u3 != null)
			u3.set(seed);
		return true;
	}

	public static boolean beginLapseBlueLiquidEffect(float timeSeconds, float intensity) {
		if (LAPSE_BLUE_LIQUID_SHADER == null)
			return false;
		var u1 = LAPSE_BLUE_LIQUID_SHADER.getUniform("Time");
		if (u1 != null)
			u1.set(timeSeconds);
		var u2 = LAPSE_BLUE_LIQUID_SHADER.getUniform("Intensity");
		if (u2 != null)
			u2.set(intensity);
		return true;
	}

	private static void setUniformIfExistsInformationOverloadOverlay(String name, float... values) {
		var uniform = INFORMATION_OVERLOAD_OVERLAY_SHADER.getUniform(name);
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

	private static void setUniformIfExistsImaginaryPurpleProjectile(String name, float... values) {
		var uniform = IMAGINARY_PURPLE_PROJECTILE_SHADER.getUniform(name);
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


	private static void setUniformIfExistsImaginaryPurple(String name, float... values) {
		var uniform = IMAGINARY_PURPLE_SHADER.getUniform(name);
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

	public static boolean beginFlameArrowExplosionEffect(float timeSeconds, float intensity) {
		if (FLAME_ARROW_EXPLOSION_SHADER == null)
			return false;
		setUniformIfExistsFlameArrowExplosion("Time", timeSeconds);
		setUniformIfExistsFlameArrowExplosion("ChargeProgress", intensity);
		return true;
	}

	/**
	 * Render type for the domain shell.
	 *
	 * <p>Differs from {@link #makeRenderType} in one decisive way: the write mask
	 * includes depth. The existing factories are colour-only, and the domain renderer
	 * additionally called {@code RenderSystem.disableDepthTest()} around every draw,
	 * so the interior painted over everything already in the buffer and which entities
	 * you could see inside came down to render order. Writing depth makes the shell a
	 * real surface — it occludes the world outside, and anything nearer draws in front
	 * of it.
	 *
	 * <p>Culling stays off deliberately: the mesh is wound inward, so with culling on
	 * the sphere would vanish when viewed from outside, leaving a carved hole in the
	 * ground and nothing above it. The fragment shader branches on {@code gl_FrontFacing}
	 * instead, giving the interior one treatment and the outer shell another.
	 */
	private static RenderType makeDomainSurfaceRenderType(String name, java.util.function.Supplier<ShaderInstance> shaderSup, boolean writeDepth) {
		// 32 x 64 quads is 8192 vertices at 20 bytes each. Sized for that, or the
		// BufferBuilder regrows its buffer every single frame.
		return RenderType.create(name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 262144, false, false,
				RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(shaderSup)).setDepthTestState(new RenderStateShard.DepthTestStateShard("lequal", 515)).setCullState(new RenderStateShard.CullStateShard(false))
						.setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, writeDepth)).setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
							com.mojang.blaze3d.systems.RenderSystem.enableBlend();
							com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
						}, () -> {
							com.mojang.blaze3d.systems.RenderSystem.disableBlend();
							com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
						})).setOutputState(new RenderStateShard.OutputStateShard("main_target", () -> {
						}, () -> {
						})).createCompositeState(true));
	}

	private static RenderType makeAdditiveRenderType(String name, java.util.function.Supplier<ShaderInstance> shaderSup) {
		return RenderType.create(name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(shaderSup)).setDepthTestState(new RenderStateShard.DepthTestStateShard("lequal", 515)).setCullState(new RenderStateShard.CullStateShard(false))
						.setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false)).setTransparencyState(new RenderStateShard.TransparencyStateShard("additive", () -> {
							com.mojang.blaze3d.systems.RenderSystem.enableBlend();
							com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE, org.lwjgl.opengl.GL11.GL_ONE, org.lwjgl.opengl.GL11.GL_ONE);
						}, () -> {
							com.mojang.blaze3d.systems.RenderSystem.disableBlend();
							com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
						})).setOutputState(new RenderStateShard.OutputStateShard("main_target", () -> {
						}, () -> {
						})).createCompositeState(true));
	}

	private static void setUniformIfExistsFlameArrowExplosion(String name, float... values) {
		var uniform = FLAME_ARROW_EXPLOSION_SHADER.getUniform(name);
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

	private static void setUniformIfExistsBlueVortex(String name, float... values) {
		var uniform = BLUE_VORTEX_SHADER.getUniform(name);
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
