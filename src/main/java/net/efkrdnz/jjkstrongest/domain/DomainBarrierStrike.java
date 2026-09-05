package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Hitting a barrier by hand.
 *
 * <p>The counterpart to the even pressure a rival domain applies. Because a blow lands in
 * one direction rather than across the whole surface, it drives a single patch to zero
 * long before the rest — so a barrier that a shrine would have shattered whole instead
 * opens a hole where somebody kept hitting it. Same grid, same numbers; only the
 * distribution differs, and the distribution is the mechanic.
 *
 * <p>Entirely server-side. The client says nothing but "I swung"; where that swing landed
 * is worked out here from the player's own eye and look vector, so nobody can nominate a
 * cell to destroy.
 */
public final class DomainBarrierStrike {

	/** Damage to the struck cell. Roughly ten clean hits open one. */
	private static final float STRIKE_DAMAGE = 26.0f;
	/** Cells either side that share in it, halving per ring. */
	private static final int STRIKE_SPREAD = 2;
	/** Minimum ticks between one player's strikes, so a macro cannot melt a shell. */
	private static final int COOLDOWN_TICKS = 10;
	private static final double REACH = 4.5;

	private DomainBarrierStrike() {
	}

	/**
	 * Resolves a swing against any barrier in front of the player.
	 *
	 * @return true if a barrier was struck
	 */
	public static boolean trySwing(ServerLevel level, Player player) {
		long now = level.getGameTime();
		long last = player.getPersistentData().getLong("lastBarrierStrike");
		if (now - last < COOLDOWN_TICKS)
			return false;

		Vec3 eye = player.getEyePosition(1.0f);
		Vec3 look = player.getLookAngle();

		for (DomainSource domain : DomainRegistry.closedIn(level)) {
			if (!domain.isAlive())
				continue;
			DomainSphere sphere = domain.volume();
			if (!sphere.isUsable() || !sphere.phase().isSealed())
				continue;
			double t = raySphere(eye, look, sphere, REACH);
			if (Double.isNaN(t))
				continue;
			DomainShell shell = domain.shell();
			if (shell == null)
				continue;
			Vec3 hit = eye.add(look.scale(t));
			Vec3 outward = hit.subtract(sphere.center());
			if (shell.isOpenTowards(outward.x, outward.y, outward.z))
				continue;
			shell.applyStrike(outward, STRIKE_DAMAGE, STRIKE_SPREAD);
			player.getPersistentData().putLong("lastBarrierStrike", now);
			return true;
		}
		return false;
	}

	/**
	 * First intersection of a ray with a sphere within {@code maxDistance}, or NaN.
	 *
	 * <p>Handles both directions: standing outside and hitting the near face, and standing
	 * inside and hitting the far one.
	 */
	private static double raySphere(Vec3 origin, Vec3 direction, DomainSphere sphere, double maxDistance) {
		Vec3 m = origin.subtract(sphere.center());
		double b = m.dot(direction);
		double c = m.dot(m) - sphere.radius() * sphere.radius();
		double disc = b * b - c;
		if (disc <= 0.0)
			return Double.NaN;
		double root = Math.sqrt(disc);
		double t = -b - root;
		if (t < 0.0)
			t = -b + root;
		if (t < 0.0 || t > maxDistance)
			return Double.NaN;
		return t;
	}
}
