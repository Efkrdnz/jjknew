package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

import javax.annotation.Nullable;

import com.mojang.blaze3d.shaders.FogShape;

@EventBusSubscriber(value = Dist.CLIENT)
public class InformationOverloadBlindnessProcedure {
	public static ViewportEvent.RenderFog provider = null;

	public static void setDistance(float start, float end) {
		provider.setNearPlaneDistance(start);
		provider.setFarPlaneDistance(end);
		if (!provider.isCanceled()) {
			provider.setCanceled(true);
		}
	}

	public static void setShape(FogShape shape) {
		provider.setFogShape(shape);
		if (!provider.isCanceled()) {
			provider.setCanceled(true);
		}
	}

	// Runs after the domain's own fog, which establishes the base atmosphere, so this
	// tightens what is already there instead of racing it. Neither handler used to declare
	// a priority, and whichever ran last simply won.
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void renderFog(ViewportEvent.RenderFog event) {
		provider = event;
		if (provider.getMode() == FogRenderer.FogMode.FOG_TERRAIN) {
			ClientLevel level = Minecraft.getInstance().level;
			Entity entity = provider.getCamera().getEntity();
			if (level != null && entity != null) {
				Vec3 pos = entity.getPosition((float) provider.getPartialTick());
				execute(provider, entity);
			}
		}
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if (!(entity instanceof LivingEntity living) || !living.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD))
			return;

		// This used to be setDistance(1, 1) — total whiteout a block from your face, which
		// deleted the domain from view for everyone it was cast on. The domain is the point;
		// being overloaded should make it harder to read, not replace it with a black card.
		// So the envelope still reaches the barrier and the gradient across it is savage:
		// the shell stays visible, heavily veiled, and everything between is washed out.
		// The disorientation proper lives in the screen overlay, where it can be loud
		// without hiding anything.
		double radius = 20.0;
		if (Minecraft.getInstance().level != null) {
			net.efkrdnz.jjkstrongest.domain.DomainSphere sphere = net.efkrdnz.jjkstrongest.domain.DomainRegistry.sphereAt(Minecraft.getInstance().level, entity.getX(), entity.getEyeY(), entity.getZ());
			if (sphere != null)
				radius = sphere.radius();
		}
		setDistance((float) (radius * 0.05), (float) (radius * 1.35));
		setShape(FogShape.SPHERE);
	}
}
