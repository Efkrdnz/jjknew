package net.efkrdnz.jjkvoice.recognize;

import java.util.List;

import net.efkrdnz.jjkvoice.audio.MicrophoneCapture;
import net.efkrdnz.jjkvoice.audio.PcmResampler;
import net.efkrdnz.jjkvoice.config.VoiceConfig;

/**
 * Decides whether a captured clip is one of the player's enrolled phrases, and
 * which technique that phrase is bound to.
 *
 * <p>Everything runs locally and deterministically: resample, trim, featurise,
 * then compare against each enrolled print with {@link DtwMatcher}. The closest
 * print wins if it falls inside the threshold that phrase calibrated during
 * enrollment.
 *
 * <p>Unlike a single-skill voice addon this searches across every configured
 * technique at once, so the winning phrase also decides what happens. Commands
 * with nothing enrolled are skipped rather than failed, which is what makes
 * partial enrollment usable.
 */
public final class PhraseRecognizer {
	public static final int TARGET_SAMPLE_RATE = 16_000;

	private PhraseRecognizer() {
	}

	public enum Outcome {
		MATCHED,
		TOO_SHORT,
		TOO_LONG,
		TOO_QUIET,
		NOT_ENROLLED,
		NO_MATCH
	}

	/**
	 * @param commandKey the host-mod command to run, or empty when nothing matched
	 * @param phrase     the winning phrase, for feedback
	 * @param distance   the best distance seen, for tuning feedback
	 */
	public record Result(Outcome outcome, String commandKey, String phrase, double distance, double threshold) {
		public boolean matched() {
			return outcome == Outcome.MATCHED;
		}

		static Result rejected(Outcome outcome) {
			return new Result(outcome, "", "", Double.MAX_VALUE, 0.0D);
		}
	}

	/** Shared preprocessing, so enrollment and recognition featurise identically. */
	public static float[][] features(MicrophoneCapture.CapturedAudio audio) {
		if (audio == null)
			return new float[0][];
		short[] resampled = PcmResampler.resample(audio.samples(), audio.sampleRate(), TARGET_SAMPLE_RATE);
		double[] normalised = PcmResampler.toNormalized(resampled);
		double[] trimmed = PcmResampler.trimSilence(normalised, TARGET_SAMPLE_RATE);
		return MfccExtractor.extract(trimmed, TARGET_SAMPLE_RATE);
	}

	/** Seconds of actual speech left after silence trimming. */
	public static double speechSeconds(MicrophoneCapture.CapturedAudio audio) {
		if (audio == null)
			return 0.0D;
		short[] resampled = PcmResampler.resample(audio.samples(), audio.sampleRate(), TARGET_SAMPLE_RATE);
		double[] normalised = PcmResampler.toNormalized(resampled);
		return (double) PcmResampler.trimSilence(normalised, TARGET_SAMPLE_RATE).length / TARGET_SAMPLE_RATE;
	}

	/** How well a clip matched the chant it was measured against. */
	public enum ChantQuality {
		NONE,
		NEAR,
		EXACT
	}

	/**
	 * @param seconds the spoken length, which is what the charge is measured in
	 */
	public record ChantResult(ChantQuality quality, String phrase, double seconds, double distance, double threshold) {
		public boolean charged() {
			return quality != ChantQuality.NONE;
		}
	}

	/**
	 * Measures a clip against the phrases that chant the active ability.
	 *
	 * <p>Two bands rather than one. Inside the calibrated threshold is an exact
	 * chant; out to {@code chantNearMultiplier} of it is a near one, credited at a
	 * fraction of the time spoken. Being generous is safe here in a way it is not
	 * for firing: the worst a wrong near-match can do is charge an ability the
	 * player already has selected, where a wrong action would spend a cooldown.
	 */
	public static ChantResult recogniseChant(MicrophoneCapture.CapturedAudio audio, List<String> chantPhrases) {
		VoiceConfig config = VoiceConfig.get();
		if (audio == null || chantPhrases == null || chantPhrases.isEmpty())
			return new ChantResult(ChantQuality.NONE, "", 0.0D, Double.MAX_VALUE, 0.0D);

		short[] resampled = PcmResampler.resample(audio.samples(), audio.sampleRate(), TARGET_SAMPLE_RATE);
		double[] normalised = PcmResampler.toNormalized(resampled);
		double[] speech = PcmResampler.trimSilence(normalised, TARGET_SAMPLE_RATE);
		double seconds = (double) speech.length / TARGET_SAMPLE_RATE;
		if (seconds < config.minSpeechSeconds || seconds > config.maxSpeechSeconds)
			return new ChantResult(ChantQuality.NONE, "", seconds, Double.MAX_VALUE, 0.0D);

		float[][] spoken = MfccExtractor.extract(speech, TARGET_SAMPLE_RATE);
		if (spoken.length == 0)
			return new ChantResult(ChantQuality.NONE, "", seconds, Double.MAX_VALUE, 0.0D);

		String best = "";
		double bestDistance = Double.MAX_VALUE;
		double bestThreshold = 0.0D;
		for (String phrase : chantPhrases) {
			VoicePrintStore.PhrasePrint print = VoicePrintStore.find(phrase).orElse(null);
			if (print == null || print.templates == null || print.templates.isEmpty())
				continue;
			for (float[][] template : print.templates) {
				double distance = DtwMatcher.distance(spoken, template);
				if (distance < bestDistance) {
					bestDistance = distance;
					best = print.phrase;
					bestThreshold = print.threshold;
				}
			}
		}

		if (bestThreshold <= 0.0D)
			return new ChantResult(ChantQuality.NONE, "", seconds, bestDistance, 0.0D);
		if (bestDistance <= bestThreshold)
			return new ChantResult(ChantQuality.EXACT, best, seconds, bestDistance, bestThreshold);
		if (bestDistance <= bestThreshold * config.chantNearMultiplier)
			return new ChantResult(ChantQuality.NEAR, best, seconds, bestDistance, bestThreshold);
		return new ChantResult(ChantQuality.NONE, best, seconds, bestDistance, bestThreshold);
	}

	public static Result recognise(MicrophoneCapture.CapturedAudio audio) {
		VoiceConfig config = VoiceConfig.get();
		if (audio == null)
			return Result.rejected(Outcome.TOO_SHORT);

		short[] resampled = PcmResampler.resample(audio.samples(), audio.sampleRate(), TARGET_SAMPLE_RATE);
		double[] normalised = PcmResampler.toNormalized(resampled);
		double[] speech = PcmResampler.trimSilence(normalised, TARGET_SAMPLE_RATE);
		double seconds = (double) speech.length / TARGET_SAMPLE_RATE;

		if (seconds < config.minSpeechSeconds)
			return Result.rejected(Outcome.TOO_SHORT);
		if (seconds > config.maxSpeechSeconds)
			return Result.rejected(Outcome.TOO_LONG);

		if (VoiceConfig.MODE_SHOUT.equals(config.mode)) {
			double loudness = PcmResampler.rootMeanSquare(speech);
			return loudness >= config.shoutRmsThreshold
					? new Result(Outcome.MATCHED, config.shoutCommand, "(shout)", loudness, config.shoutRmsThreshold)
					: Result.rejected(Outcome.TOO_QUIET);
		}

		float[][] spoken = MfccExtractor.extract(speech, TARGET_SAMPLE_RATE);
		if (spoken.length == 0)
			return Result.rejected(Outcome.TOO_SHORT);

		String bestCommand = "";
		String bestPhrase = "";
		double bestDistance = Double.MAX_VALUE;
		double bestThreshold = 0.0D;
		boolean anyEnrolled = false;

		for (String commandKey : config.commands.keySet()) {
			List<String> phrases = config.phrasesFor(commandKey);
			for (String phrase : phrases) {
				VoicePrintStore.PhrasePrint print = VoicePrintStore.find(phrase).orElse(null);
				if (print == null || print.templates == null || print.templates.isEmpty())
					continue;
				anyEnrolled = true;

				// Closest template wins: enrollment repeats are alternatives, not an average.
				for (float[][] template : print.templates) {
					double distance = DtwMatcher.distance(spoken, template);
					if (distance < bestDistance) {
						bestDistance = distance;
						bestCommand = commandKey;
						bestPhrase = print.phrase;
						bestThreshold = print.threshold;
					}
				}
			}
		}

		if (!anyEnrolled)
			return Result.rejected(Outcome.NOT_ENROLLED);
		if (bestDistance > bestThreshold)
			return new Result(Outcome.NO_MATCH, "", bestPhrase, bestDistance, bestThreshold);
		return new Result(Outcome.MATCHED, bestCommand, bestPhrase, bestDistance, bestThreshold);
	}
}
