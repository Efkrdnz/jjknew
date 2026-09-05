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
		return new DomainSphere(center, radius, Double.NEGATIVE_INFINITY, DomainPhase.ACTIVE, 1.0f);
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

	public boolean isUsable() {
		return radius > 0.0;
	}
}
