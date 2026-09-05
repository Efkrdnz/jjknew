package net.efkrdnz.jjkstrongest.domain;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * Stops projectiles at the domain shell.
 *
 * <p>The analytic clamp in {@link DomainCollision} rides on {@code Entity#collide},
 * which most projectiles never reach: arrows drive themselves by writing position
 * directly, and this mod's own technique entities set {@code noPhysics}, which makes
 * {@code move()} skip collision resolution altogether. The old block shell stopped
 * them anyway because it was made of real blocks, so without this the change would
 * quietly let arrows through a wall that stops people.
 *
 * <p>Detection is a crossing test between the previous tick's position and this one's,
 * so a fast arrow cannot step over the shell between ticks.
 */
@EventBusSubscriber(modid = "jjk_strongest")
public final class DomainProjectileContainment {

	private static final TagKey<EntityType<?>> TECHNIQUE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique"));

	private DomainProjectileContainment() {
	}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		if (DomainRegistry.activeCount == 0)
			return;
		Entity entity = event.getEntity();
		if (!(entity instanceof Projectile))
			return;
		if (entity.getType().is(TECHNIQUE))
			return;
		if (entity.level().isClientSide())
			return;

		for (DomainSource domain : DomainRegistry.closedIn(entity.level())) {
			if (!domain.isAlive())
				continue;
			DomainSphere sphere = domain.volume();
			if (!sphere.isUsable() || !sphere.phase().isSealed())
				continue;

			boolean wasInside = sphere.contains(entity.xOld, entity.yOld, entity.zOld);
			boolean isInside = sphere.contains(entity.getX(), entity.getY(), entity.getZ());
			if (wasInside == isInside)
				continue;

			// Crossed the shell this tick — put it back on the surface and kill it.
			Vec3 from = new Vec3(entity.xOld, entity.yOld, entity.zOld);
			Vec3 rel = from.subtract(sphere.center());
			double dist = rel.length();
			if (dist > 1.0E-4) {
				double surface = Math.min(dist, Math.max(0.0, sphere.radius() - 0.1));
				Vec3 landing = sphere.center().add(rel.scale(surface / dist));
				entity.setPos(landing.x, Math.max(landing.y, sphere.floorY()), landing.z);
			} else {
				entity.setPos(from.x, from.y, from.z);
			}
			entity.setDeltaMovement(Vec3.ZERO);
			return;
		}
	}
}
