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
