package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

/**
 * How intact a closed barrier is, direction by direction.
 *
 * <p>One grid produces both ways a barrier fails, which is the point of it. A rival open
 * domain presses on the whole surface at once, so every cell runs down together and the
 * shell shatters as a piece. Somebody hammering one spot drives a single patch to zero
 * long before the rest, and that patch becomes a hole. Same numbers, same storage — only
 * the distribution differs.
 *
 * <p>Cells are a latitude/longitude grid over directions from the centre, which matches
 * both the sphere mesh's UV layout and the way the fragment shader rebuilds a direction,
 * so the grid can be handed to the shader as a {@code 32 x 16} texture and the indexing
 * agrees on both sides.
 *
 * <p>Integrity is kept as floats because pressure arrives in fractions of a point per
 * tick; it is quantised to bytes only when sent to clients.
 */
public final class DomainShell {

	public static final int LON_CELLS = 32;
	public static final int LAT_CELLS = 16;
	public static final int CELLS = LON_CELLS * LAT_CELLS;
	public static final float FULL = 255.0f;

	private final float[] integrity = new float[CELLS];
	private final short[] hold = new short[CELLS];
	/** How this particular barrier heals and how well it shrugs off rival pressure. */
	private final DomainShellProfile profile;
	private int version;
	private int breaches;
	private boolean dirty = true;

	public DomainShell(DomainShellProfile profile) {
		this.profile = profile;
		Arrays.fill(integrity, FULL);
	}

	// ---- indexing -----------------------------------------------------------

	/**
	 * Cell index for a direction from the domain centre.
	 *
	 * <p>Latitude bands by <em>angle</em>, not by height. Banding on {@code y} directly
	 * would disagree with the sphere mesh, which steps theta uniformly, and the two would
	 * drift apart toward the poles — putting the cracks somewhere other than where the
	 * damage was.
	 */
	public static int cellFor(double dx, double dy, double dz) {
		double length = Math.sqrt(dx * dx + dz * dz + dy * dy);
		if (length <= 1.0E-6)
			return 0;
		double ny = Math.max(-1.0, Math.min(1.0, dy / length));
		int lat = (int) (Math.acos(ny) / Math.PI * LAT_CELLS);
		if (lat >= LAT_CELLS)
			lat = LAT_CELLS - 1;
		// Matches the mesh's u = atan2(z, x) / 2pi exactly. An offset here would put every
		// crack half a turn away from the damage that caused it.
		double angle = Math.atan2(dz, dx) / (Math.PI * 2.0);
		if (angle < 0.0)
			angle += 1.0;
		int lon = (int) (angle * LON_CELLS);
		if (lon >= LON_CELLS)
			lon = LON_CELLS - 1;
		if (lon < 0)
			lon = 0;
		return lat * LON_CELLS + lon;
	}

	public static int cellFor(Vec3 direction) {
		return cellFor(direction.x, direction.y, direction.z);
	}

	// ---- damage -------------------------------------------------------------

	/**
	 * Even wear across the whole surface, from a rival open domain pressing on it.
	 *
	 * <p>Deliberately uniform rather than derived from where slashes actually land: the
	 * shrine only ever spawns slashes at or above its own centre height, so damage taken
	 * purely from impacts would eat the top of the dome and never touch the bottom.
	 */
	public void applyPressure(float perCell) {
		float resisted = perCell / Math.max(0.01f, profile.pressureResistance());
		if (resisted <= 0.0f)
			return;
		for (int i = 0; i < CELLS; i++)
			hurt(i, resisted);
		dirty = true;
	}

	/**
	 * Wear concentrated on the face pointing one way, for a barrier being pressed on by
	 * another barrier.
	 *
	 * <p>Deliberately not the even wear {@link #applyPressure} does. An open domain has no
	 * surface, so all it can do is lean on a closed one from every side at once; two closed
	 * domains meet along a real contact plane, and the shell should give way <em>there</em>
	 * and collapse inward from that side. That is what the clash looks like in the source
	 * material and it is what makes it read differently from a shrine grinding a dome down
	 * everywhere at the same rate.
	 *
	 * @param coneCos cosine of the half-angle of the affected face; 0.5 is 60&deg;
	 */
	public void applyFacePressure(Vec3 direction, float amount, double coneCos) {
		if (amount <= 0.0f)
			return;
		double length = direction.length();
		if (length <= 1.0E-6)
			return;
		double nx = direction.x / length;
		double ny = direction.y / length;
		double nz = direction.z / length;
		double span = 1.0 - coneCos;
		if (span <= 1.0E-6)
			return;
		boolean touched = false;
		for (int i = 0; i < CELLS; i++) {
			double align = CELL_DIRECTIONS[i * 3] * nx + CELL_DIRECTIONS[i * 3 + 1] * ny + CELL_DIRECTIONS[i * 3 + 2] * nz;
			if (align <= coneCos)
				continue;
			// Squared so the middle of the face takes far more than its edge, which is what
			// opens one hole rather than thinning a whole hemisphere at once.
			float falloff = (float) ((align - coneCos) / span);
			hurt(i, amount * falloff * falloff);
			touched = true;
		}
		if (touched)
			dirty = true;
	}

	/**
	 * Unit direction of a cell's centre — the inverse of {@link #cellFor}.
	 *
	 * <p>Built once because the face-pressure loop runs over all 512 cells every tick a
	 * clash is live, and allocating a vector per cell for that would be 512 objects a tick
	 * per domain for numbers that never change.
	 */
	private static final double[] CELL_DIRECTIONS = buildCellDirections();

	private static double[] buildCellDirections() {
		double[] out = new double[CELLS * 3];
		for (int cell = 0; cell < CELLS; cell++) {
			int lat = cell / LON_CELLS;
			int lon = cell % LON_CELLS;
			// Cell centres, so the direction round-trips back through cellFor to itself.
			double theta = (lat + 0.5) / LAT_CELLS * Math.PI;
			double phi = (lon + 0.5) / LON_CELLS * Math.PI * 2.0;
			double sinTheta = Math.sin(theta);
			out[cell * 3] = sinTheta * Math.cos(phi);
			out[cell * 3 + 1] = Math.cos(theta);
			out[cell * 3 + 2] = sinTheta * Math.sin(phi);
		}
		return out;
	}

	/** Unit direction of a cell's centre, as a vector. For tests and debug readouts. */
	public static Vec3 directionOf(int cell) {
		int i = Math.floorMod(cell, CELLS) * 3;
		return new Vec3(CELL_DIRECTIONS[i], CELL_DIRECTIONS[i + 1], CELL_DIRECTIONS[i + 2]);
	}

	/** A single stopped projectile or slash. Small — mostly so the hit is visible. */
	public void applyImpact(Vec3 direction, float amount) {
		hurt(cellFor(direction), amount);
		dirty = true;
	}

	/**
	 * A deliberate blow, spread over neighbouring cells so repeated hits on one spot open
	 * a patch rather than a pinhole.
	 */
	public void applyStrike(Vec3 direction, float amount, int spreadRings) {
		int centre = cellFor(direction);
		int lat = centre / LON_CELLS;
		int lon = centre % LON_CELLS;
		for (int dLat = -spreadRings; dLat <= spreadRings; dLat++) {
			int l = lat + dLat;
			if (l < 0 || l >= LAT_CELLS)
				continue;
			for (int dLon = -spreadRings; dLon <= spreadRings; dLon++) {
				int rings = Math.max(Math.abs(dLat), Math.abs(dLon));
				// full strength at the centre, halving each ring out
				float falloff = 1.0f / (1 << rings);
				hurt(l * LON_CELLS + Math.floorMod(lon + dLon, LON_CELLS), amount * falloff);
			}
		}
		dirty = true;
	}

	private void hurt(int cell, float amount) {
		float before = integrity[cell];
		if (before <= 0.0f)
			return;
		float after = Math.max(0.0f, before - amount);
		integrity[cell] = after;
		hold[cell] = (short) profile.regenHoldTicks();
		if (after <= 0.0f)
			breaches++;
	}

	/** Heals cells nothing has touched recently. Sustained pressure outruns this easily. */
	public void tickRegen() {
		boolean changed = false;
		for (int i = 0; i < CELLS; i++) {
			if (hold[i] > 0) {
				hold[i]--;
				continue;
			}
			if (integrity[i] >= FULL)
				continue;
			boolean wasBreached = integrity[i] <= 0.0f;
			integrity[i] = Math.min(FULL, integrity[i] + profile.regenPerTick());
			if (wasBreached && integrity[i] > 0.0f)
				breaches--;
			changed = true;
		}
		if (changed)
			dirty = true;
	}

	// ---- queries ------------------------------------------------------------

	/** Mean integrity across the surface, 0..1. This is the barrier's health. */
	public float totalIntegrity() {
		float sum = 0.0f;
		for (int i = 0; i < CELLS; i++)
			sum += integrity[i];
		return sum / (CELLS * FULL);
	}

	/** How many cells have been driven to zero — each one is a hole. */
	public int breachCount() {
		return breaches;
	}

	/** True once every cell is gone: the shell shatters as a piece. */
	public boolean isShattered() {
		return breaches >= CELLS;
	}

	/** Whether the shell has been holed in this direction. */
	public boolean isOpenTowards(double dx, double dy, double dz) {
		return integrity[cellFor(dx, dy, dz)] <= 0.0f;
	}

	public float integrityAt(int cell) {
		return integrity[Math.floorMod(cell, CELLS)];
	}

	// ---- sync ---------------------------------------------------------------

	public boolean isDirty() {
		return dirty;
	}

	/** Quantised copy for the wire: one byte per cell. Pure — call {@link #markSynced} after sending. */
	public byte[] snapshot() {
		byte[] out = new byte[CELLS];
		for (int i = 0; i < CELLS; i++)
			out[i] = (byte) Math.round(Math.max(0.0f, Math.min(FULL, integrity[i])));
		return out;
	}

	/** Clears the dirty flag and bumps the version, once a snapshot has actually gone out. */
	public void markSynced() {
		dirty = false;
		version++;
	}

	public int version() {
		return version;
	}

	/** Overwrites the grid from a received snapshot. Client side. */
	public void applyCells(byte[] cells) {
		if (cells.length != CELLS)
			return;
		breaches = 0;
		for (int i = 0; i < CELLS; i++) {
			integrity[i] = cells[i] & 0xFF;
			if (integrity[i] <= 0.0f)
				breaches++;
		}
		dirty = false;
	}

	// ---- persistence --------------------------------------------------------

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putByteArray("cells", snapshot());
		return tag;
	}

	public void load(CompoundTag tag) {
		byte[] cells = tag.getByteArray("cells");
		if (cells.length != CELLS)
			return;
		breaches = 0;
		for (int i = 0; i < CELLS; i++) {
			integrity[i] = cells[i] & 0xFF;
			hold[i] = 0;
			if (integrity[i] <= 0.0f)
				breaches++;
		}
		dirty = true;
	}
}
