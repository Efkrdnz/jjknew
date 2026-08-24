package net.efkrdnz.jjkvoice.audio;

/**
 * Linear-interpolation resampler and small PCM helpers.
 *
 * <p>Simple Voice Chat hands us 48 kHz mono audio. Every feature stage below
 * expects 16 kHz, which is the standard rate for speech features: it keeps the
 * whole speech band up to 8 kHz while cutting the frame count by two thirds.
 */
public final class PcmResampler {
	private PcmResampler() {
	}

	public static short[] resample(short[] samples, int sourceRate, int targetRate) {
		if (samples == null || samples.length == 0 || sourceRate <= 0 || targetRate <= 0)
			return new short[0];
		if (sourceRate == targetRate)
			return samples.clone();

		double ratio = (double) sourceRate / targetRate;
		int outputLength = (int) Math.floor(samples.length / ratio);
		if (outputLength <= 0)
			return new short[0];

		short[] output = new short[outputLength];
		for (int i = 0; i < outputLength; i++) {
			double position = i * ratio;
			int index = (int) position;
			double fraction = position - index;
			short first = samples[Math.min(index, samples.length - 1)];
			short second = samples[Math.min(index + 1, samples.length - 1)];
			output[i] = (short) Math.round(first + (second - first) * fraction);
		}
		return output;
	}

	/** Converts 16-bit PCM into the -1..1 range the feature stages work in. */
	public static double[] toNormalized(short[] samples) {
		if (samples == null)
			return new double[0];
		double[] output = new double[samples.length];
		for (int i = 0; i < samples.length; i++)
			output[i] = samples[i] / 32768.0D;
		return output;
	}

	/** Root-mean-square amplitude, used for the silence gate and shout mode. */
	public static double rootMeanSquare(double[] samples, int from, int to) {
		if (samples == null || samples.length == 0)
			return 0.0D;
		int start = Math.max(0, from);
		int end = Math.min(samples.length, to);
		if (end <= start)
			return 0.0D;
		double sum = 0.0D;
		for (int i = start; i < end; i++)
			sum += samples[i] * samples[i];
		return Math.sqrt(sum / (end - start));
	}

	public static double rootMeanSquare(double[] samples) {
		return rootMeanSquare(samples, 0, samples == null ? 0 : samples.length);
	}

	/**
	 * Trims leading and trailing silence so a phrase spoken half a second late
	 * still lines up with an enrolled template.
	 *
	 * <p>The threshold is relative to the clip's own peak rather than absolute,
	 * which keeps the trim stable across microphone gain differences.
	 */
	public static double[] trimSilence(double[] samples, int sampleRate) {
		if (samples == null || samples.length == 0)
			return new double[0];

		int window = Math.max(1, sampleRate / 100); // 10 ms
		int windowCount = samples.length / window;
		if (windowCount <= 0)
			return samples.clone();

		double[] energies = new double[windowCount];
		double peak = 0.0D;
		for (int i = 0; i < windowCount; i++) {
			energies[i] = rootMeanSquare(samples, i * window, (i + 1) * window);
			peak = Math.max(peak, energies[i]);
		}
		if (peak <= 0.0D)
			return new double[0];

		double threshold = peak * 0.10D;
		int firstWindow = -1;
		int lastWindow = -1;
		for (int i = 0; i < windowCount; i++) {
			if (energies[i] < threshold)
				continue;
			if (firstWindow < 0)
				firstWindow = i;
			lastWindow = i;
		}
		if (firstWindow < 0)
			return new double[0];

		// Keep a 50 ms cushion so plosives at the edges are not clipped away.
		int cushion = Math.max(1, sampleRate / 20 / window);
		int start = Math.max(0, firstWindow - cushion) * window;
		int end = Math.min(windowCount, lastWindow + 1 + cushion) * window;

		double[] trimmed = new double[end - start];
		System.arraycopy(samples, start, trimmed, 0, trimmed.length);
		return trimmed;
	}
}
