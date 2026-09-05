package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;

import com.mojang.blaze3d.shaders.FogShape;

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

	/** Pulls the fog in to the shell so the outside world is not visible past it. */
	@SubscribeEvent
	public static void onRenderFog(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN)
			return;
		if (event.getCamera().getFluidInCamera() != FogType.NONE)
			return;
		DomainSphere sphere = sphereAroundCamera();
		if (sphere == null)
			return;
		event.setFogShape(FogShape.SPHERE);
		event.setNearPlaneDistance((float) (sphere.radius() * 0.55));
		event.setFarPlaneDistance((float) (sphere.radius() * 1.05));
		if (!event.isCanceled())
			event.setCanceled(true);
	}

	/** Tints the fog toward the void's own palette instead of the overworld sky. */
	@SubscribeEvent
	public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
		if (sphereAroundCamera() == null)
			return;
		event.setRed(0.03f);
		event.setGreen(0.04f);
		event.setBlue(0.11f);
	}
}
