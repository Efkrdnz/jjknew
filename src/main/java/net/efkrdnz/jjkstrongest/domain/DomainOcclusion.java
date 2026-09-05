package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.world.phys.Vec3;

/**
 * Clips an open domain's output against a closed domain's shell.
 *
 * <p>This is the rule from {@link DomainBarrierKind} made concrete: nothing an open domain
 * projects may cross a closed barrier. What used to stand in for it was a containment test
 * on a slash's <em>spawn point</em> — so a slash that began outside the sphere and whose
 * twenty-five-block line ran straight through it was drawn clean through the barrier,
 * which is exactly what a barrier is supposed to prevent.
 *
 * <p>Two wrinkles in the geometry, both easy to get wrong. A slash quad is
 * <em>centre</em>-anchored, so shortening one has to move its centre too or it grows out
 * of the other end. And its long axis is <strong>not</strong> its {@code direction}: the
 * renderer maps local +Z to {@code direction} and then scales length along local +X, so
 * {@code direction} is the quad's <em>normal</em> and the blade runs perpendicular to it,
 * spun about it by {@code roll}. Clipping along {@code direction} would cut across an axis
 * the slash does not occupy — which looks almost right, and is entirely wrong.
 */
public final class DomainOcclusion {

	/** Shorter than this and a clipped slash is a visual stub; drop it instead. */
	private static final double MIN_LENGTH = 2.0;
	private static final double EPSILON = 1.0E-6;

	/**
	 * What is left of a slash after the barrier takes its share.
	 *
	 * @param blocked  the barrier stopped it entirely; do not draw it
	 * @param position new centre for the surviving part
	 * @param length   new length for the surviving part
	 * @param impact   where it met the shell, or null if it never touched
	 */
	public record Clip(boolean blocked, Vec3 position, double length, Vec3 impact) {

		static Clip untouched(Vec3 position, double length) {
			return new Clip(false, position, length, null);
		}
	}

	private DomainOcclusion() {
	}

	/**
	 * The blade's long axis in world space.
	 *
	 * <p>Reproduces the renderer's frame: {@code R_y(yaw) · R_x(pitch)} sends local +Z to
	 * {@code direction}, then {@code R_z(roll)} spins the perpendicular plane. Local +X —
	 * the axis length is scaled along — comes out as this.
	 */
	public static Vec3 longAxis(Vec3 direction, float roll) {
		double dx = direction.x, dy = direction.y, dz = direction.z;
		double h = Math.sqrt(dx * dx + dz * dz);
		Vec3 e1;
		Vec3 e2;
		if (h < 1.0E-6) {
			// Pointing straight up or down: the yaw is undefined, so pick any frame.
			e1 = new Vec3(1.0, 0.0, 0.0);
			e2 = new Vec3(0.0, 0.0, dy > 0 ? -1.0 : 1.0);
		} else {
			e1 = new Vec3(dz / h, 0.0, -dx / h);
			e2 = new Vec3(-dx * dy / h, h, -dz * dy / h);
		}
		double c = Math.cos(roll);
		double s = Math.sin(roll);
		return e1.scale(c).add(e2.scale(s));
	}

	/**
	 * Clips a centre-anchored slash against a sphere.
	 *
	 * @param centre    the slash's midpoint
	 * @param direction the quad's normal, as sent on the wire
	 * @param roll      spin about that normal; together with it this fixes the long axis
	 * @param length    total length, half either side of {@code centre}
	 */
	public static Clip clip(Vec3 centre, Vec3 direction, float roll, double length, DomainSphere sphere) {
		Vec3 axis = longAxis(direction, roll);
		double half = length * 0.5;
		Vec3 m = centre.subtract(sphere.center());
		double b = m.dot(axis);
		double c = m.dot(m) - sphere.radius() * sphere.radius();
		double disc = b * b - c;

		// Misses the sphere altogether, or grazes it exactly — nothing to clip.
		if (disc <= EPSILON)
			return Clip.untouched(centre, length);

		double root = Math.sqrt(disc);
		double t0 = -b - root;
		double t1 = -b + root;

		// The crossing happens off the ends of this segment.
		if (t1 <= -half || t0 >= half)
			return Clip.untouched(centre, length);

		double nearOutside = Math.max(0.0, Math.min(t0, half) - (-half));
		double farOutside = Math.max(0.0, half - Math.max(t1, -half));

		// Wholly swallowed by the sphere: it would have been drawn entirely inside.
		if (nearOutside < MIN_LENGTH && farOutside < MIN_LENGTH) {
			Vec3 impact = surfacePoint(centre, axis, sphere, t0, t1, half);
			return new Clip(true, centre, 0.0, impact);
		}

		boolean keepNear = nearOutside >= farOutside;
		double keptStart = keepNear ? -half : Math.max(t1, -half);
		double keptEnd = keepNear ? Math.min(t0, half) : half;
		double keptLength = keptEnd - keptStart;
		double keptMid = (keptStart + keptEnd) * 0.5;

		Vec3 newCentre = centre.add(axis.scale(keptMid));
		Vec3 impact = centre.add(axis.scale(keepNear ? Math.min(t0, half) : Math.max(t1, -half)));
		return new Clip(false, newCentre, keptLength, impact);
	}

	/** Best guess at where a fully-swallowed segment met the surface. */
	private static Vec3 surfacePoint(Vec3 centre, Vec3 axis, DomainSphere sphere, double t0, double t1, double half) {
		double t = Math.abs(t0) <= Math.abs(t1) ? t0 : t1;
		if (t < -half || t > half)
			// Both crossings are outside the segment, so it started and ended inside;
			// the nearest surface point is straight out from the centre.
			return sphere.center().add(centre.subtract(sphere.center()).normalize().scale(sphere.radius()));
		return centre.add(axis.scale(t));
	}
}
