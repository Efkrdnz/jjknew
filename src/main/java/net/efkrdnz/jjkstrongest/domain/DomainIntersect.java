package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.world.phys.Vec3;

/**
 * Sphere-against-sphere geometry, for when two domains meet.
 *
 * <p>Nothing calls this yet. It exists because the clash today is a centre-to-centre
 * distance check with a hard-coded 130-block threshold — a Malevolent Shrine 129
 * blocks from a 30-block dome counts as "clashing" even though the two never touch.
 * The merge mechanic needs the real thing: whether the spheres actually overlap, the
 * plane where they meet, and how deep the overlap runs.
 *
 * <p>The contact plane is the radical plane, perpendicular to the line of centres.
 * Pushing it toward the weaker domain is what "the stronger domain drives the contact
 * surface into the weaker one" becomes in practice.
 */
public final class DomainIntersect {

	/** Where two domains meet: the contact disc, and how far they overlap. */
	public record Lens(Vec3 planePoint, Vec3 planeNormal, double discRadius, double depth) {
	}

	private DomainIntersect() {
	}

	public static boolean intersects(DomainSphere a, DomainSphere b) {
		double sum = a.radius() + b.radius();
		return a.center().distanceToSqr(b.center()) < sum * sum;
	}

	/** How far the two spheres overlap along the line of centres; zero when apart. */
	public static double overlapDepth(DomainSphere a, DomainSphere b) {
		double distance = a.center().distanceTo(b.center());
		return Math.max(0.0, a.radius() + b.radius() - distance);
	}

	/**
	 * The contact geometry, or null when the spheres are apart or one wholly contains
	 * the other (in which case there is no lens to contest).
	 */
	public static Lens intersect(DomainSphere a, DomainSphere b) {
		Vec3 between = b.center().subtract(a.center());
		double distance = between.length();
		if (distance <= 1.0E-4)
			return null;
		double ra = a.radius();
		double rb = b.radius();
		if (distance >= ra + rb)
			return null;
		if (distance <= Math.abs(ra - rb))
			return null;

		Vec3 normal = between.scale(1.0 / distance);
		// distance from a's centre to the radical plane
		double offset = (distance * distance + ra * ra - rb * rb) / (2.0 * distance);
		double discRadiusSq = ra * ra - offset * offset;
		double discRadius = discRadiusSq > 0.0 ? Math.sqrt(discRadiusSq) : 0.0;
		return new Lens(a.center().add(normal.scale(offset)), normal, discRadius, ra + rb - distance);
	}

	/** Whether a point sits in the contested region shared by both domains. */
	public static boolean inLens(DomainSphere a, DomainSphere b, double x, double y, double z) {
		return a.contains(x, y, z) && b.contains(x, y, z);
	}
}
