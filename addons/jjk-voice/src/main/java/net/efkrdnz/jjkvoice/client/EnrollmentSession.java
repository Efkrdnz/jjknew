package net.efkrdnz.jjkvoice.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import net.efkrdnz.jjkvoice.config.VoiceConfig;
import net.efkrdnz.jjkvoice.recognize.VoicePrintStore;

/**
 * Guided voice enrollment: teaching the mod how this player speaks.
 *
 * <p>Matching is speaker-dependent by design, which is why enrollment exists at
 * all. It is also why accuracy is good for one player and one phrase without any
 * model: the reference is the player's own voice, not an average of everyone's.
 *
 * <p>Repeats matter. Two recordings of the same phrase reveal how much the player
 * naturally varies, and that spread is what calibrates the accept threshold.
 *
 * <p>A session takes a plain phrase list rather than a technique, so the caller
 * decides the scope -- one technique's phrases, or every configured phrase. With
 * nineteen techniques available, enrolling one at a time is the normal path.
 */
public final class EnrollmentSession {
	private static EnrollmentSession active;

	private final List<String> phrases;
	private final int samplesPerPhrase;
	private final Map<String, List<float[][]>> collected = new LinkedHashMap<>();
	private int phraseIndex;

	private EnrollmentSession(List<String> phrases, int samplesPerPhrase) {
		this.phrases = phrases;
		this.samplesPerPhrase = samplesPerPhrase;
		for (String phrase : phrases)
			collected.put(phrase, new ArrayList<>());
	}

	public static boolean isActive() {
		return active != null;
	}

	public static EnrollmentSession active() {
		return active;
	}

	/** @return false when there is nothing to enroll */
	public static boolean start(List<String> phrases) {
		VoiceConfig config = VoiceConfig.get();
		List<String> cleaned = new ArrayList<>();
		for (String phrase : phrases) {
			String normalised = VoiceConfig.normalisePhrase(phrase);
			if (!normalised.isEmpty() && !cleaned.contains(normalised))
				cleaned.add(normalised);
		}
		if (cleaned.isEmpty())
			return false;

		active = new EnrollmentSession(cleaned, config.enrollmentSamples);
		say(Component.translatable("message.jjkvoice.enroll.start",
				cleaned.size(), cleaned.size() * config.enrollmentSamples)
				.withStyle(ChatFormatting.LIGHT_PURPLE));
		active.prompt();
		return true;
	}

	public static void cancel() {
		if (active == null)
			return;
		active = null;
		say(Component.translatable("message.jjkvoice.enroll.cancelled").withStyle(ChatFormatting.GRAY));
	}

	public String currentPhrase() {
		return phraseIndex < phrases.size() ? phrases.get(phraseIndex) : "";
	}

	/**
	 * Feeds one captured clip into the session.
	 *
	 * <p>Takes already-extracted features rather than raw audio so the caller can
	 * do the expensive part off the client thread.
	 */
	public void accept(float[][] frames) {
		String phrase = currentPhrase();
		if (phrase.isEmpty() || frames == null)
			return;

		// Frames advance every 10 ms, so the count is the clip's speech duration.
		double seconds = frames.length * 0.010D;
		VoiceConfig config = VoiceConfig.get();

		if (frames.length == 0 || seconds < config.minSpeechSeconds) {
			say(Component.translatable("message.jjkvoice.enroll.rejected_short")
					.withStyle(ChatFormatting.RED));
			prompt();
			return;
		}
		if (seconds > config.maxSpeechSeconds) {
			say(Component.translatable("message.jjkvoice.enroll.rejected_long")
					.withStyle(ChatFormatting.RED));
			prompt();
			return;
		}

		List<float[][]> samples = collected.get(phrase);
		samples.add(frames);
		say(Component.translatable("message.jjkvoice.enroll.accepted", samples.size(), samplesPerPhrase)
				.withStyle(ChatFormatting.GREEN));

		if (samples.size() < samplesPerPhrase) {
			prompt();
			return;
		}

		VoicePrintStore.enroll(phrase, samples);
		double threshold = VoicePrintStore.find(phrase).map(print -> print.threshold).orElse(0.0D);
		String command = VoiceConfig.get().commandFor(phrase);
		say(Component.translatable("message.jjkvoice.enroll.phrase_done", phrase,
				command.isEmpty() ? "?" : command, String.format("%.2f", threshold))
				.withStyle(ChatFormatting.AQUA));

		phraseIndex++;
		if (phraseIndex < phrases.size()) {
			prompt();
			return;
		}

		active = null;
		say(Component.translatable("message.jjkvoice.enroll.complete").withStyle(ChatFormatting.LIGHT_PURPLE));
	}

	private void prompt() {
		String phrase = currentPhrase();
		if (phrase.isEmpty())
			return;
		int done = collected.get(phrase).size();
		say(Component.translatable("message.jjkvoice.enroll.prompt",
				Component.literal(phrase).withStyle(ChatFormatting.GOLD),
				done + 1, samplesPerPhrase).withStyle(ChatFormatting.YELLOW));
	}

	private static void say(Component message) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null)
			minecraft.player.displayClientMessage(message, false);
	}

	/** Exposed so the tick handler can size its status text without recomputing. */
	public int samplesPerPhrase() {
		return samplesPerPhrase;
	}
}
