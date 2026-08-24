package net.efkrdnz.jjkvoice.recognize;

/**
 * Dynamic time warping between two MFCC sequences.
 *
 * <p>Nobody says a word at the same speed twice. DTW finds the cheapest
 * alignment that stretches one sequence onto the other, so a drawn-out "ariiise"
 * still matches a clipped one. The returned cost is divided by the path length,
 * which makes distances comparable between clips of different durations and lets
 * a single configured threshold apply to every phrase.
 */
public final class DtwMatcher {
	/**
	 * Sakoe-Chiba band, as a fraction of the longer sequence. Alignments are only
	 * allowed to drift this far from the diagonal, which both speeds the search up
	 * and rejects nonsense warps that would otherwise let a long unrelated clip
	 * collapse onto a short template.
	 */
	private static final double BAND_FRACTION = 0.35D;

	private DtwMatcher() {
	}

	/** @return average per-step alignment cost, or {@link Double#MAX_VALUE} if incomparable */
	public static double distance(float[][] first, float[][] second) {
		if (first == null || second == null || first.length == 0 || second.length == 0)
			return Double.MAX_VALUE;

		int n = first.length;
		int m = second.length;
		int band = Math.max(Math.abs(n - m) + 1, (int) Math.ceil(BAND_FRACTION * Math.max(n, m)));

		double[] previous = new double[m + 1];
		double[] current = new double[m + 1];
		java.util.Arrays.fill(previous, Double.MAX_VALUE);
		previous[0] = 0.0D;

		for (int i = 1; i <= n; i++) {
			java.util.Arrays.fill(current, Double.MAX_VALUE);
			int from = Math.max(1, i - band);
			int to = Math.min(m, i + band);
			for (int j = from; j <= to; j++) {
				double best = Math.min(previous[j], Math.min(current[j - 1], previous[j - 1]));
				if (best == Double.MAX_VALUE)
					continue;
				current[j] = best + frameDistance(first[i - 1], second[j - 1]);
			}
			double[] swap = previous;
			previous = current;
			current = swap;
		}

		double total = previous[m];
		if (total == Double.MAX_VALUE)
			return Double.MAX_VALUE;
		return total / (n + m);
	}

	private static double frameDistance(float[] first, float[] second) {
		int length = Math.min(first.length, second.length);
		double sum = 0.0D;
		for (int i = 0; i < length; i++) {
			double delta = first[i] - second[i];
			sum += delta * delta;
		}
		return Math.sqrt(sum);
	}
}
