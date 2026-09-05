package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.world.phys.Vec3;

/**
 * How intact the shell is, direction by direction.
 *
 * <p>Inert for now — nothing damages a shell yet, and every cell stays at full
 * strength. It is here because it is the seam all four kinds of breakage share: a
 * melee hit knocks down the cells around the impact, a rival domain grinds down the
 * cells facing the contact lens, Mahoraga's adaptation shatters cells in a spreading
 * pattern instead of deleting the domain outright, and regeneration walks them back up
 * unless something keeps hitting them.
 *
 * <p>Cells are a latitude/longitude grid over directions from the centre, which lines
 * up with both the sphere mesh's UV layout and the way the fragment shader already
 * rebuilds a direction — so the same indexing works in Java and in GLSL, and the grid
 * can be handed to the shader as a small {@code 32 x 16} texture.
 *
 * <p>Once cells can actually be knocked out, {@link #isOpenTowards} is the one call
 * {@link DomainCollision} needs in order to let people through a hole.
 */
public final class DomainShell {

	public static final int LON_CELLS = 32;
	public static final int LAT_CELLS = 16;
	public static final int CELLS = LON_CELLS * LAT_CELLS;
	public static final byte INTACT = (byte) 255;

	private final byte[] integrity = new byte[CELLS];
	private int version;

	public DomainShell() {
		java.util.Arrays.fill(integrity, INTACT);
	}

	/** Cell index for a direction from the domain centre. */
	public static int cellFor(double dx, double dy, double dz) {
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length <= 1.0E-6)
			return 0;
		double ny = dy / length;
		int lat = (int) ((1.0 - ny) * 0.5 * LAT_CELLS);
		if (lat >= LAT_CELLS)
			lat = LAT_CELLS - 1;
		double angle = Math.atan2(dz, dx) / (Math.PI * 2.0) + 0.5;
		int lon = (int) (angle * LON_CELLS);
		if (lon >= LON_CELLS)
			lon = LON_CELLS - 1;
		return lat * LON_CELLS + lon;
	}

	public int cellFor(DomainSphere sphere, Vec3 worldPoint) {
		Vec3 rel = worldPoint.subtract(sphere.center());
		return cellFor(rel.x, rel.y, rel.z);
	}

	public byte integrityAt(int cell) {
		return integrity[Math.floorMod(cell, CELLS)];
	}

	/** Whether the shell has been breached in this direction. */
	public boolean isOpenTowards(double dx, double dy, double dz) {
		return integrityAt(cellFor(dx, dy, dz)) == 0;
	}

	public byte[] snapshot() {
		return integrity.clone();
	}

	public int version() {
		return version;
	}

	/** Bumped whenever the grid changes, so clients can drop stale updates. */
	public void markChanged() {
		version++;
	}
}
