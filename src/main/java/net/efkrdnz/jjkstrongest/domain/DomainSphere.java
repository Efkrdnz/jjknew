package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The one description of a domain's shape.
 *
 * <p>Collision, the terrain carve, the renderer, the fog and every "is this inside
 * a domain" query read this and nothing else. Before it there were eight unrelated
 * hard-coded radii (25.2, 28.5, 29, 30, 35, 44, 72, 100) spread across gameplay and
 * render code, so the thing you could see was never quite the thing you could touch.
 *
 * <p>The shape is a sphere cut by a flat floor plane. That is what the old block
 * shell actually built — solid below the plane, hollow dome above it — and it matters:
 * a bare sphere would leave the player standing on the inside of a bowl, sliding to
 * the middle.
 */
public record DomainSphere(Vec3 center, double radius, double floorY, DomainPhase phase, float progress) {

	/**
	 * The reach of an open domain: a plain ball with no floor cut, since an open domain
	 * has no interior to stand in — it just covers a volume.
	 */
	public static DomainSphere openField(Vec3 center, double radius) {
		return openField(center, radius, DomainPhase.ACTIVE, 1.0f);
	}

	/**
	 * An open field that knows where it is in its life.
	 *
	 * <p>The single-argument form reported {@code ACTIVE} unconditionally, which was true
	 * only because open domains had no phases. They do now, and code that asks a domain
	 * what phase it is in has to get the same answer whichever kind it is holding.
	 */
	public static DomainSphere openField(Vec3 center, double radius, DomainPhase phase, float progress) {
		return new DomainSphere(center, radius, Double.NEGATIVE_INFINITY, phase, progress);
	}

	public boolean contains(double x, double y, double z) {
		if (y < floorY)
			return false;
		return center.distanceToSqr(x, y, z) <= radius * radius;
	}

	public boolean contains(Vec3 point) {
		return contains(point.x, point.y, point.z);
	}

	/** Ignores the floor plane. Used by the carve, which only ever looks above it anyway. */
	public boolean withinRadius(double x, double y, double z) {
		return center.distanceToSqr(x, y, z) <= radius * radius;
	}

	/** Bounding box of the whole sphere, for broad-phase entity lookups. */
	public AABB bounds() {
		return new AABB(center.x - radius, center.y - radius, center.z - radius, center.x + radius, center.y + radius, center.z + radius);
	}

	/** The highest point of the dome. */
	public double topY() {
		return center.y + radius;
	}

	/**
	 * Clamps a movement so it cannot cross this shell.
	 *
	 * <p>Lives here rather than in {@link DomainCollision} because it is sphere geometry
	 * and nothing else — no entity, no level, no registry. That keeps it reachable from
	 * {@code tools/geometry-harness}, which matters: this is the most behaviourally
	 * load-bearing maths in the engine and the environment cannot build the mod to test it
	 * any other way.
	 *
	 * @param pos       the mover's current position
	 * @param halfWidth half its bounding-box width
	 * @param movement  the movement already resolved against blocks
	 * @param shell     per-direction integrity, or null when the barrier is unbroken
	 */
	public Vec3 clampMovement(Vec3 pos, double halfWidth, Vec3 movement, DomainShell shell) {
		final double epsilon = 1.0E-4;
		Vec3 out = movement;
		Vec3 next = pos.add(out);

		// A cell driven to zero is a hole, and a hole is a way through. This is the whole
		// payoff of tracking integrity per direction rather than as one number.
		if (shell != null) {
			Vec3 outward = next.subtract(center);
			if (shell.isOpenTowards(outward.x, outward.y, outward.z))
				return out;
		}

		if (contains(pos.x, pos.y, pos.z)) {
			// Inside: keep them off the floor plane and inside the dome.
			if (next.y < floorY) {
				out = new Vec3(out.x, floorY - pos.y, out.z);
				next = pos.add(out);
			}
			double limit = Math.max(0.25, radius - halfWidth);
			Vec3 rel = next.subtract(center);
			double dist = rel.length();
			if (dist > limit && dist > epsilon) {
				Vec3 normal = rel.scale(1.0 / dist);
				double outward = out.dot(normal);
				if (outward > 0.0)
					out = out.subtract(normal.scale(outward));
			}
		} else {
			// Outside: the shell is solid from this side too, so a domain is a sealed room
			// rather than a bag you can only leave. Below the floor plane the terrain is
			// untouched, so ordinary block collision already covers it.
			double limit = radius + halfWidth;
			Vec3 rel = next.subtract(center);
			double dist = rel.length();
			if (dist < limit && dist > epsilon && next.y >= floorY) {
				Vec3 normal = rel.scale(1.0 / dist);
				double inward = out.dot(normal);
				if (inward < 0.0)
					out = out.subtract(normal.scale(inward));
			}
		}
		return out;
	}

	/**
	 * The floor plane on its own, bounded to a horizontal footprint.
	 *
	 * <p>Used while a domain is collapsing. The shell is shrinking away by then, so it
	 * cannot hold anyone up, but the ground it carved out is still being put back — and
	 * with the whole sphere hollowed that is a thirty-block hole to fall into. The plane
	 * keeps standing until the domain finally goes, and the footprint bound stops it
	 * becoming an invisible floor stretching across the world.
	 */
	public Vec3 clampFloorWithin(Vec3 pos, Vec3 movement, double horizontalRadius) {
		Vec3 next = pos.add(movement);
		if (next.y >= floorY)
			return movement;
		double dx = next.x - center.x;
		double dz = next.z - center.z;
		if (dx * dx + dz * dz > horizontalRadius * horizontalRadius)
			return movement;
		return new Vec3(movement.x, floorY - pos.y, movement.z);
	}

	public boolean isUsable() {
		return radius > 0.0;
	}
}
