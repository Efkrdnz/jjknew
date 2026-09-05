package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.List;

/**
 * Makes the domain shell solid without building it out of blocks.
 *
 * <p>The old barrier was a voxelized sphere of {@code domain_barrier}: stair-stepped,
 * ~13 million position checks to raise, and a six-figure NBT blob to put back. The
 * shell here is the analytic sphere itself. {@code EntityCollideMixin} feeds every
 * movement through {@link #clamp} after vanilla has resolved it against blocks, so
 * the two compose instead of fighting — and because it runs inside the same call on
 * the client and the server, both sides reach the same answer and the player never
 * gets snapped back.
 *
 * <p>The maths is deliberately a slide, not a stop: the component of the motion
 * pointing through the shell is removed and the rest is kept, so running at the wall
 * carries you along it rather than gluing you to it.
 */
public final class DomainCollision {

	/** Entities that are themselves techniques pass through; that includes the domain anchor. */
	private static final TagKey<EntityType<?>> TECHNIQUE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique"));

	private static final double EPSILON = 1.0E-4;

	private DomainCollision() {
	}

	/**
	 * Adjusts a resolved movement vector so it cannot cross a domain shell.
	 *
	 * @param entity   the entity being moved
	 * @param movement the movement vanilla already resolved against blocks
	 * @return the same vector when nothing applies, otherwise a clamped one
	 */
	public static Vec3 clamp(Entity entity, Vec3 movement) {
		if (DomainRegistry.activeCount == 0)
			return movement;
		if (entity == null || isExempt(entity))
			return movement;
		Level level = entity.level();
		if (level == null)
			return movement;
		List<DomainUVEntity> domains = DomainRegistry.voidsIn(level);
		if (domains.isEmpty())
			return movement;

		Vec3 result = movement;
		for (DomainUVEntity domain : domains) {
			if (!domain.isAlive())
				continue;
			DomainSphere sphere = domain.sphere();
			if (!sphere.isUsable())
				continue;
			// Only a shell at full size is solid. While it is still growing — or
			// shrinking on the way out — the radius sweeps through every value down to
			// zero, and clamping against that would squeeze everyone inside into the
			// centre point and then let them go.
			if (!sphere.phase().isSealed())
				continue;
			result = clampAgainst(entity, result, sphere, domain.shell());
		}
		return result;
	}

	private static boolean isExempt(Entity entity) {
		if (entity.isSpectator())
			return true;
		if (entity instanceof Player player && player.isCreative())
			return true;
		return entity.getType().is(TECHNIQUE);
	}

	private static Vec3 clampAgainst(Entity entity, Vec3 movement, DomainSphere sphere, DomainShell shell) {
		return sphere.clampMovement(entity.position(), entity.getBbWidth() * 0.5, movement, shell);
	}
}
