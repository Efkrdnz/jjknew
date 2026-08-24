package net.efkrdnz.jjkvoice.recognize;

/**
 * Turns a spoken clip into MFCC frames -- the feature the matcher compares.
 *
 * <p>Raw waveforms cannot be compared directly: saying the same word twice
 * produces two completely different sample sequences. Mel-frequency cepstral
 * coefficients throw away pitch and loudness and keep the shape of the vocal
 * tract, which is what actually distinguishes one word from another.
 *
 * <p>The pipeline is the textbook one: pre-emphasis, 25 ms Hamming frames every
 * 10 ms, power spectrum, 26 mel-spaced triangular filters, log, then a DCT to
 * decorrelate. Cepstral mean normalisation at the end removes the constant
 * colouring of the player's microphone, so an enrolled voiceprint still matches
 * after they switch headsets.
 */
public final class MfccExtractor {
	public static final int COEFFICIENTS = 13;

	private static final double FRAME_SECONDS = 0.025D;
	private static final double STEP_SECONDS = 0.010D;
	private static final int FFT_SIZE = 512;
	private static final int MEL_FILTERS = 26;
	private static final double PRE_EMPHASIS = 0.97D;
	private static final double LOW_FREQUENCY = 300.0D;
	private static final double HIGH_FREQUENCY = 8000.0D;
	private static final double LOG_FLOOR = 1.0e-10D;

	private MfccExtractor() {
	}

	/** @return one row of {@link #COEFFICIENTS} values per frame, or empty if the clip is too short */
	public static float[][] extract(double[] samples, int sampleRate) {
		if (samples == null || sampleRate <= 0)
			return new float[0][];

		int frameLength = (int) Math.round(FRAME_SECONDS * sampleRate);
		int frameStep = (int) Math.round(STEP_SECONDS * sampleRate);
		if (frameLength <= 0 || frameStep <= 0 || samples.length < frameLength)
			return new float[0][];

		double[] emphasised = preEmphasise(samples);
		double[] window = hammingWindow(frameLength);
		double[][] filterbank = melFilterbank(sampleRate);

		int frameCount = 1 + (emphasised.length - frameLength) / frameStep;
		float[][] frames = new float[frameCount][];

		double[] real = new double[FFT_SIZE];
		double[] imaginary = new double[FFT_SIZE];
		double[] power = new double[FFT_SIZE / 2 + 1];
		double[] logEnergies = new double[MEL_FILTERS];

		for (int frame = 0; frame < frameCount; frame++) {
			int offset = frame * frameStep;

			java.util.Arrays.fill(real, 0.0D);
			java.util.Arrays.fill(imaginary, 0.0D);
			for (int i = 0; i < frameLength && i < FFT_SIZE; i++)
				real[i] = emphasised[offset + i] * window[i];

			fft(real, imaginary);

			for (int bin = 0; bin < power.length; bin++)
				power[bin] = (real[bin] * real[bin] + imaginary[bin] * imaginary[bin]) / FFT_SIZE;

			for (int filter = 0; filter < MEL_FILTERS; filter++) {
				double sum = 0.0D;
				double[] weights = filterbank[filter];
				for (int bin = 0; bin < weights.length; bin++)
					sum += power[bin] * weights[bin];
				logEnergies[filter] = Math.log(Math.max(sum, LOG_FLOOR));
			}

			frames[frame] = discreteCosineTransform(logEnergies);
		}

		subtractCepstralMean(frames);
		return frames;
	}

	private static double[] preEmphasise(double[] samples) {
		double[] output = new double[samples.length];
		output[0] = samples[0];
		for (int i = 1; i < samples.length; i++)
			output[i] = samples[i] - PRE_EMPHASIS * samples[i - 1];
		return output;
	}

	private static double[] hammingWindow(int length) {
		double[] window = new double[length];
		for (int i = 0; i < length; i++)
			window[i] = 0.54D - 0.46D * Math.cos(2.0D * Math.PI * i / (length - 1));
		return window;
	}

	/** Triangular filters spaced evenly on the mel scale, expressed as FFT-bin weights. */
	private static double[][] melFilterbank(int sampleRate) {
		double lowMel = toMel(LOW_FREQUENCY);
		double highMel = toMel(Math.min(HIGH_FREQUENCY, sampleRate / 2.0D));
		int binCount = FFT_SIZE / 2 + 1;

		int[] points = new int[MEL_FILTERS + 2];
		for (int i = 0; i < points.length; i++) {
			double mel = lowMel + (highMel - lowMel) * i / (MEL_FILTERS + 1);
			double hertz = fromMel(mel);
			points[i] = (int) Math.floor((FFT_SIZE + 1) * hertz / sampleRate);
			points[i] = Math.max(0, Math.min(binCount - 1, points[i]));
		}

		double[][] filterbank = new double[MEL_FILTERS][binCount];
		for (int filter = 0; filter < MEL_FILTERS; filter++) {
			int left = points[filter];
			int centre = points[filter + 1];
			int right = points[filter + 2];
			for (int bin = left; bin < centre; bin++)
				if (centre > left)
					filterbank[filter][bin] = (double) (bin - left) / (centre - left);
			for (int bin = centre; bin < right; bin++)
				if (right > centre)
					filterbank[filter][bin] = (double) (right - bin) / (right - centre);
		}
		return filterbank;
	}

	private static float[] discreteCosineTransform(double[] logEnergies) {
		float[] coefficients = new float[COEFFICIENTS];
		int n = logEnergies.length;
		for (int k = 0; k < COEFFICIENTS; k++) {
			double sum = 0.0D;
			for (int i = 0; i < n; i++)
				sum += logEnergies[i] * Math.cos(Math.PI * k * (i + 0.5D) / n);
			coefficients[k] = (float) (sum * Math.sqrt(2.0D / n));
		}
		return coefficients;
	}

	/**
	 * Removes the per-coefficient average across the clip. Anything constant for
	 * the whole utterance is the channel -- the mic, the room -- not the word.
	 */
	private static void subtractCepstralMean(float[][] frames) {
		if (frames.length == 0)
			return;
		for (int coefficient = 0; coefficient < COEFFICIENTS; coefficient++) {
			double sum = 0.0D;
			for (float[] frame : frames)
				sum += frame[coefficient];
			float mean = (float) (sum / frames.length);
			for (float[] frame : frames)
				frame[coefficient] -= mean;
		}
	}

	private static double toMel(double hertz) {
		return 2595.0D * Math.log10(1.0D + hertz / 700.0D);
	}

	private static double fromMel(double mel) {
		return 700.0D * (Math.pow(10.0D, mel / 2595.0D) - 1.0D);
	}

	/** In-place iterative radix-2 Cooley-Tukey transform. Length must be a power of two. */
	private static void fft(double[] real, double[] imaginary) {
		int n = real.length;

		for (int i = 1, j = 0; i < n; i++) {
			int bit = n >> 1;
			for (; (j & bit) != 0; bit >>= 1)
				j ^= bit;
			j ^= bit;
			if (i < j) {
				double swapReal = real[i];
				real[i] = real[j];
				real[j] = swapReal;
				double swapImaginary = imaginary[i];
				imaginary[i] = imaginary[j];
				imaginary[j] = swapImaginary;
			}
		}

		for (int length = 2; length <= n; length <<= 1) {
			double angle = -2.0D * Math.PI / length;
			double stepReal = Math.cos(angle);
			double stepImaginary = Math.sin(angle);
			for (int start = 0; start < n; start += length) {
				double twiddleReal = 1.0D;
				double twiddleImaginary = 0.0D;
				for (int k = 0; k < length / 2; k++) {
					int a = start + k;
					int b = a + length / 2;
					double productReal = real[b] * twiddleReal - imaginary[b] * twiddleImaginary;
					double productImaginary = real[b] * twiddleImaginary + imaginary[b] * twiddleReal;
					real[b] = real[a] - productReal;
					imaginary[b] = imaginary[a] - productImaginary;
					real[a] += productReal;
					imaginary[a] += productImaginary;
					double nextReal = twiddleReal * stepReal - twiddleImaginary * stepImaginary;
					twiddleImaginary = twiddleReal * stepImaginary + twiddleImaginary * stepReal;
					twiddleReal = nextReal;
				}
			}
		}
	}
}
