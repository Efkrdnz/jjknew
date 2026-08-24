package net.efkrdnz.jjkvoice.recognize;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.efkrdnz.jjkvoice.audio.MicrophoneCapture;
import net.efkrdnz.jjkvoice.audio.PcmResampler;
import net.efkrdnz.jjkvoice.config.VoiceConfig;

/**
 * Decides whether a captured clip is one of the player's enrolled phrases, and
 * which technique that phrase belongs to.
 *
 * <p>Everything runs locally and deterministically: resample, trim, featurise,
 * then compare against each enrolled print with {@link DtwMatcher}. The closest
 * print wins if it falls inside the threshold that phrase calibrated during
 * enrollment.
 *
 * <p>Unlike a single-skill voice addon this searches across every technique at
 * once, so the winning phrase also decides what happens. Commands with nothing
 * enrolled are skipped rather than failed, which is what makes partial
 * enrollment usable.
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
	 * What the recogniser is allowed to hear.
	 *
	 * <p>Narrowed to the speaker's own technique before it gets here. That is not
	 * only a permission check: leaving another sorcerer's phrases in the search
	 * lets them win, so a Gojo player saying "purple" could lose to an enrolled
	 * "fuga" they can never use. Removing them makes the remaining field the only
	 * thing competing.
	 *
	 * @param allowed      command keys to search, from the host mod
	 * @param incantations ability to the incantations that charge it
	 * @param loose        keys where a near miss still counts, because charging one
	 *                     step too little is recoverable where a wrong cast is not
	 */
	public record Vocabulary(Set<String> allowed, Map<String, List<String>> incantations, Set<String> loose) {
	}

	/**
	 * @param commandKey  the host-mod command spoken, or empty when nothing matched
	 * @param phrase      the winning phrase, for feedback
	 * @param exact       inside the calibrated threshold, rather than merely near
	 * @param incantation matched an incantation rather than the ability's name
	 * @param line        which line of that incantation, or -1 when not one
	 */
	public record Result(Outcome outcome, String commandKey, String phrase, boolean exact, boolean incantation,
			int line, double distance, double threshold) {
		public boolean matched() {
			return outcome == Outcome.MATCHED;
		}

		static Result rejected(Outcome outcome) {
			return new Result(outcome, "", "", false, false, -1, Double.MAX_VALUE, 0.0D);
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

	public static Result recognise(MicrophoneCapture.CapturedAudio audio, Vocabulary vocabulary) {
		VoiceConfig config = VoiceConfig.get();
		if (audio == null)
			return Result.rejected(Outcome.TOO_SHORT);

		short[] resampled = PcmResampler.resample(audio.samples(), audio.sampleRate(), TARGET_SAMPLE_RATE);
		double[] normalised = PcmResampler.toNormalized(resampled);
		double[] speech = PcmResampler.trimSilence(normalised, TARGET_SAMPLE_RATE);
		double seconds = (double) speech.length / TARGET_SAMPLE_RATE;

		if (seconds < config.minSpeechSeconds)
			return Result.rejected(Outcome.TOO_SHORT);
		// A recited line is allowed to run longer than a shouted word, but only if a
		// line is what it turns out to be -- checked once there is a winner.
		boolean recitable = !vocabulary.incantations().isEmpty();
		if (seconds > (recitable ? config.maxIncantationSeconds : config.maxSpeechSeconds))
			return Result.rejected(Outcome.TOO_LONG);

		if (VoiceConfig.MODE_SHOUT.equals(config.mode)) {
			double loudness = PcmResampler.rootMeanSquare(speech);
			return loudness >= config.shoutRmsThreshold
					? new Result(Outcome.MATCHED, config.shoutCommand, "(shout)", true, false, -1, loudness,
							config.shoutRmsThreshold)
					: Result.rejected(Outcome.TOO_QUIET);
		}

		float[][] spoken = MfccExtractor.extract(speech, TARGET_SAMPLE_RATE);
		if (spoken.length == 0)
			return Result.rejected(Outcome.TOO_SHORT);

		Best best = new Best();
		for (String commandKey : vocabulary.allowed())
			best.consider(spoken, commandKey, config.phrasesFor(commandKey), false);
		// Incantations are searched under the ability they charge, so a match
		// already carries which ability it was for -- there is nothing to look up.
		for (Map.Entry<String, List<String>> entry : vocabulary.incantations().entrySet())
			best.consider(spoken, entry.getKey(), entry.getValue(), true);

		if (!best.anyEnrolled)
			return Result.rejected(Outcome.NOT_ENROLLED);
		if (!best.incantation && seconds > config.maxSpeechSeconds)
			// Long, but not a recited line. The extra room was not for this.
			return Result.rejected(Outcome.TOO_LONG);
		if (best.distance <= best.threshold)
			return new Result(Outcome.MATCHED, best.commandKey, best.phrase, true, best.incantation,
					best.line, best.distance, best.threshold);
		// A near miss is only worth taking where being one step under-charged is
		// recoverable. Casting on a near miss would spend a cooldown on a guess.
		if ((best.incantation || vocabulary.loose().contains(best.commandKey))
				&& best.distance <= best.threshold * config.chantNearMultiplier)
			return new Result(Outcome.MATCHED, best.commandKey, best.phrase, false, best.incantation,
					best.line, best.distance, best.threshold);
		return new Result(Outcome.NO_MATCH, "", best.phrase, false, false, -1, best.distance, best.threshold);
	}

	/** Running best across every phrase searched, whatever bucket it came from. */
	private static final class Best {
		private String commandKey = "";
		private String phrase = "";
		private boolean incantation;
		private int line = -1;
		private double distance = Double.MAX_VALUE;
		private double threshold;
		private boolean anyEnrolled;

		private void consider(float[][] spoken, String commandKey, List<String> phrases, boolean incantation) {
			if (phrases == null)
				return;
			for (int index = 0; index < phrases.size(); index++) {
				String candidate = phrases.get(index);
				VoicePrintStore.PhrasePrint print = VoicePrintStore.find(candidate).orElse(null);
				if (print == null || print.templates == null || print.templates.isEmpty())
					continue;
				anyEnrolled = true;

				// Closest template wins: enrollment repeats are alternatives, not an average.
				for (float[][] template : print.templates) {
					double measured = DtwMatcher.distance(spoken, template);
					if (measured >= distance)
						continue;
					distance = measured;
					threshold = print.threshold;
					phrase = print.phrase;
					this.commandKey = commandKey;
					this.incantation = incantation;
					this.line = incantation ? index : -1;
				}
			}
		}
	}
}
