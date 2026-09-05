package net.efkrdnz.jjkstrongest.domain;

/**
 * The ripples on one domain's floor: a fixed ring of the last sixteen, packed for the shader.
 *
 * <p>No Minecraft imports on purpose, so {@code tools/geometry-harness} can run it. The
 * client tracker decides <em>when</em> a ripple happens; this only remembers where and how
 * hard, and turns that into the {@code RippleData} uniform the floor reads.
 *
 * <p>Layout, per slot {@code i}: {@code [4i] dx, [4i+1] dz, [4i+2] birth seconds,
 * [4i+3] strength}. Positions are relative to the sphere centre in blocks, and birth is on
 * the same clock as the shader's {@code Time} — the domain entity's own tick count over
 * twenty — so the shader can take {@code Time - birth} and get an age with no conversion.
 */
public final class RippleField {

	public static final int CAPACITY = 16;
	public static final int FLOATS = CAPACITY * 4;
	/** A ripple is finished after this: by then the shader's own decay has it under 3%. */
	public static final int LIFETIME_TICKS = 80;

	private final double[] dx = new double[CAPACITY];
	private final double[] dz = new double[CAPACITY];
	private final int[] birth = new int[CAPACITY];
	private final float[] strength = new float[CAPACITY];
	private int next;

	/** Adds a ripple, taking the slot of the oldest one when the ring is full. */
	public void emit(double centreDx, double centreDz, int birthTick, float amount) {
		if (amount <= 0.0f)
			return;
		int slot = next;
		next = (next + 1) % CAPACITY;
		dx[slot] = centreDx;
		dz[slot] = centreDz;
		birth[slot] = birthTick;
		strength[slot] = amount;
	}

	/** Forgets ripples older than {@link #LIFETIME_TICKS}. */
	public void prune(int nowTick) {
		for (int i = 0; i < CAPACITY; i++)
			if (strength[i] > 0.0f && nowTick - birth[i] > LIFETIME_TICKS)
				strength[i] = 0.0f;
	}

	/** Live ripples at this tick. */
	public int liveCount(int nowTick) {
		int n = 0;
		for (int i = 0; i < CAPACITY; i++)
			if (isLive(i, nowTick))
				n++;
		return n;
	}

	/**
	 * Writes the uniform. Returns how many slots carry a live ripple, so a caller can skip
	 * the upload on a still sea.
	 *
	 * <p>A ripple born in the future — the tracker and the renderer read the entity clock
	 * at different moments — goes out with zero strength rather than a negative age the
	 * shader would have to guard against.
	 */
	public int pack(float[] out, int nowTick) {
		if (out.length < FLOATS)
			throw new IllegalArgumentException("need " + FLOATS + " floats, got " + out.length);
		int live = 0;
		for (int i = 0; i < CAPACITY; i++) {
			int o = i * 4;
			boolean ok = isLive(i, nowTick);
			out[o] = (float) dx[i];
			out[o + 1] = (float) dz[i];
			out[o + 2] = birth[i] / 20.0f;
			out[o + 3] = ok ? strength[i] : 0.0f;
			if (ok)
				live++;
		}
		return live;
	}

	private boolean isLive(int i, int nowTick) {
		return strength[i] > 0.0f && nowTick >= birth[i] && nowTick - birth[i] <= LIFETIME_TICKS;
	}
}
