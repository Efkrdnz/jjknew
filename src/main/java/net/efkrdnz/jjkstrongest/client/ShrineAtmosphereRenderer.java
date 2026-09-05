package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import com.mojang.blaze3d.shaders.FogShape;

/**
 * The air inside Malevolent Shrine: red murk that hides the far side of the field.
 *
 * <p>The Shrine's counterpart to {@link DomainAtmosphereRenderer}, and built to compose
 * with it. One number drives everything the shrine does to the atmosphere — sky, fog
 * distance, fog colour, lightmap — and it is computed here once: the phase ramp (in over
 * the expansion, out over the collapse) times a distance falloff that begins forty blocks
 * outside the field and is complete ten blocks inside it. Approaching a shrine, the world
 * reddens gradually; nothing snaps at a boundary.
 *
 * <p>Inside a closed domain the Void's atmosphere wins, twice over: {@link #presence()}
 * returns zero there, and the Void's fog handler runs first at HIGH priority and cancels
 * the event, so this one is never delivered.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public final class ShrineAtmosphereRenderer {

	/** Blood dusk, the same family as the sky's horizon band. */
	public static final float FOG_R = 0.42f;
	public static final float FOG_G = 0.06f;
	public static final float FOG_B = 0.03f;

	/** Fog planes at full intensity, as fractions of the field radius. */
	private static final double FOG_NEAR = 0.55;
	private static final double FOG_FAR = 1.10;
	/** Where the falloff begins and ends, in blocks outside and inside the field's edge. */
	private static final double FADE_OUTSIDE = 40.0;
	private static final double FADE_INSIDE = 10.0;

	private ShrineAtmosphereRenderer() {
	}

	/** The live shrine nearest a point, or null. Off the registry, never a level scan. */
	public static MalevolentShrineEntity nearest(ClientLevel level, double x, double y, double z) {
		MalevolentShrineEntity best = null;
		double bestSq = Double.MAX_VALUE;
		for (MalevolentShrineEntity shrine : DomainRegistry.shrinesIn(level)) {
			if (!shrine.isAlive())
				continue;
			double distSq = shrine.distanceToSqr(x, y, z);
			if (distSq < bestSq) {
				bestSq = distSq;
				best = shrine;
			}
		}
		return best;
	}

	/** How much of this shrine's atmosphere a point at (x, y, z) is under, 0..1. */
	public static float intensity(MalevolentShrineEntity shrine, double x, double y, double z) {
		float phase;
		DomainPhase p = shrine.phase();
		if (p == DomainPhase.EXPANDING)
			phase = shrine.phaseProgress();
		else if (p == DomainPhase.COLLAPSING)
			phase = 1.0f - shrine.phaseProgress();
		else
			phase = 1.0f;
		if (phase <= 0.0f)
			return 0.0f;
		double radius = MalevolentShrineEntity.FIELD_RADIUS;
		double dist = Math.sqrt(shrine.distanceToSqr(x, y, z));
		float falloff = smoothstep(radius + FADE_OUTSIDE, radius - FADE_INSIDE, dist);
		return phase * falloff;
	}

	/** The shrine intensity at the camera, or zero inside a closed domain. */
	public static float presence() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Entity camera = mc.getCameraEntity();
		if (level == null || camera == null || DomainRegistry.activeCount == 0)
			return 0.0f;
		double x = camera.getX();
		double y = camera.getEyeY();
		double z = camera.getZ();
		if (DomainRegistry.sphereAt(level, x, y, z) != null)
			return 0.0f;
		MalevolentShrineEntity shrine = nearest(level, x, y, z);
		return shrine == null ? 0.0f : intensity(shrine, x, y, z);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onRenderFog(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN)
			return;
		if (event.getCamera().getFluidInCamera() != FogType.NONE)
			return;
		float p = presence();
		if (p <= 0.02f)
			return;
		double radius = MalevolentShrineEntity.FIELD_RADIUS;
		// Lerped from wherever vanilla put the planes, so the murk closes in as you walk
		// into the field rather than arriving all at once.
		event.setNearPlaneDistance(Mth.lerp(p, event.getNearPlaneDistance(), (float) (radius * FOG_NEAR)));
		event.setFarPlaneDistance(Mth.lerp(p, event.getFarPlaneDistance(), (float) (radius * FOG_FAR)));
		if (p > 0.5f)
			event.setFogShape(FogShape.SPHERE);
		if (!event.isCanceled())
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
		float p = presence();
		if (p <= 0.02f)
			return;
		event.setRed(Mth.lerp(p, event.getRed(), FOG_R));
		event.setGreen(Mth.lerp(p, event.getGreen(), FOG_G));
		event.setBlue(Mth.lerp(p, event.getBlue(), FOG_B));
	}

	/** GLSL's smoothstep, with the edges either way round. */
	static float smoothstep(double edge0, double edge1, double x) {
		double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
		return (float) (t * t * (3.0 - 2.0 * t));
	}
}
