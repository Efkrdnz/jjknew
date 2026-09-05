package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;

import com.mojang.blaze3d.shaders.FogShape;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;

/**
 * What the inside of a domain looks like beyond the shell itself.
 *
 * <p>Two things have to be handled here because the block barrier is gone. It was
 * {@code lightLevel(s -> 15)}, so it lit the whole interior; without it the carved
 * space is an air pocket at sky-light zero and every entity in there would render
 * pitch black. And it was opaque, so it hid the world outside; an analytic shell needs
 * fog to do that job instead.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public final class DomainAtmosphereRenderer {

	private DomainAtmosphereRenderer() {
	}

	private static DomainSphere sphereAroundCamera() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (mc.level == null || player == null)
			return null;
		return DomainRegistry.sphereAt(mc.level, player.getX(), player.getEyeY(), player.getZ());
	}

	/**
	 * Pulls the fog in to the shell so the outside world is not visible past it.
	 *
	 * <p>Runs early on purpose. Three handlers in this mod write these same two events —
	 * this one and Information Overload's pair — and none of them used to declare a
	 * priority, so which one won inside a domain was undefined. The domain establishes the
	 * base atmosphere; Information Overload composites over it afterwards.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRenderFog(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN)
			return;
		if (event.getCamera().getFluidInCamera() != FogType.NONE)
			return;
		DomainSphere sphere = sphereAroundCamera();
		if (sphere == null)
			return;
		float hold = presence(sphere);
		if (hold <= 0.02f)
			return;
		event.setFogShape(FogShape.SPHERE);
		// Opened out as the domain breaks, rather than snapped off. The shell used to shrink
		// during collapse and take the fog's reference radius down with it; now that it holds
		// at full size so the shards fly from where the wall was, the fog has to be told
		// explicitly to let go — otherwise it stays clamped at 0.55R through the collapse and
		// the whole terrain-restore tail, and then vanishes in one frame.
		float near = (float) (sphere.radius() * 0.55);
		float far = (float) (sphere.radius() * 1.05);
		float open = 1.0f + (1.0f - hold) * 12.0f;
		event.setNearPlaneDistance(near * open);
		event.setFarPlaneDistance(far * open);
		if (!event.isCanceled())
			event.setCanceled(true);
	}

	/**
	 * How much of the domain's atmosphere is still in force, 0..1.
	 *
	 * <p>One during ordinary life, easing to nothing across the collapse.
	 */
	private static float presence(DomainSphere sphere) {
		if (sphere.phase() != DomainPhase.COLLAPSING)
			return 1.0f;
		return Math.max(0.0f, 1.0f - sphere.progress());
	}

	/** Tints the fog toward the void's own palette instead of the overworld sky. */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
		DomainSphere sphere = sphereAroundCamera();
		if (sphere == null)
			return;
		float hold = presence(sphere);
		if (hold <= 0.02f)
			return;
		// Blended toward whatever the sky was going to be, so the colour lets go on the same
		// beat as the distance rather than popping back at the end of the collapse.
		event.setRed(event.getRed() + (0.03f - event.getRed()) * hold);
		event.setGreen(event.getGreen() + (0.04f - event.getGreen()) * hold);
		event.setBlue(event.getBlue() + (0.11f - event.getBlue()) * hold);
	}
}
