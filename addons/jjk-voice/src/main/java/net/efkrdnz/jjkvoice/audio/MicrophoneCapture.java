package net.efkrdnz.jjkvoice.audio;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe bridge between Simple Voice Chat's audio thread and the client thread.
 *
 * <p>Simple Voice Chat delivers microphone frames on its own capture thread. The
 * recognizer runs on the Minecraft client thread. This class is the only shared
 * state between the two, so all mutation happens under one lock and the finished
 * clip is handed over through a single atomic reference.
 */
public final class MicrophoneCapture {
	public static final int SAMPLE_RATE = 48_000;

	/** A command phrase is short. Anything longer is the player holding the key. */
	public static final int MAX_SECONDS = 5;
	private static final int MAX_SAMPLES = SAMPLE_RATE * MAX_SECONDS;

	private static final AtomicBoolean ARMED = new AtomicBoolean();
	private static final AtomicReference<CapturedAudio> COMPLETED = new AtomicReference<>();
	private static final Object BUFFER_LOCK = new Object();
	private static final short[] BUFFER = new short[MAX_SAMPLES];

	private static int sampleCount;
	private static boolean receiving;
	private static boolean truncated;

	private MicrophoneCapture() {
	}

	public static void arm() {
		synchronized (BUFFER_LOCK) {
			sampleCount = 0;
			receiving = false;
			truncated = false;
		}
		COMPLETED.set(null);
		ARMED.set(true);
	}

	public static void disarm() {
		ARMED.set(false);
		synchronized (BUFFER_LOCK) {
			if (!receiving || sampleCount == 0)
				return;
			COMPLETED.set(new CapturedAudio(Arrays.copyOf(BUFFER, sampleCount), SAMPLE_RATE, truncated));
			receiving = false;
			sampleCount = 0;
			truncated = false;
		}
	}

	public static boolean isArmed() {
		return ARMED.get();
	}

	public static boolean isReceiving() {
		synchronized (BUFFER_LOCK) {
			return receiving;
		}
	}

	/** Called from Simple Voice Chat's capture thread. */
	public static void accept(short[] samples) {
		if (!ARMED.get() || samples == null || samples.length == 0)
			return;

		synchronized (BUFFER_LOCK) {
			receiving = true;
			int writable = Math.min(samples.length, MAX_SAMPLES - sampleCount);
			if (writable > 0) {
				System.arraycopy(samples, 0, BUFFER, sampleCount, writable);
				sampleCount += writable;
			}
			if (writable < samples.length)
				truncated = true;
		}
	}

	public static CapturedAudio pollCompleted() {
		return COMPLETED.getAndSet(null);
	}

	public record CapturedAudio(short[] samples, int sampleRate, boolean truncated) {
		public double durationSeconds() {
			return (double) samples.length / sampleRate;
		}
	}
}
