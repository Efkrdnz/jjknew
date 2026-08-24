package net.efkrdnz.jjkvoice.recognize;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.neoforged.fml.loading.FMLPaths;

import net.efkrdnz.jjkvoice.JjkVoiceMod;
import net.efkrdnz.jjkvoice.config.VoiceConfig;

/**
 * The player's enrolled voiceprints, kept on disk beside the game.
 *
 * <p>Nothing here ever leaves the machine. A print is a handful of MFCC matrices
 * from the player's own microphone plus the accept threshold measured from them,
 * which is not reversible into audio and is useless to anyone else.
 */
public final class VoicePrintStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type PRINT_MAP = new TypeToken<LinkedHashMap<String, PhrasePrint>>() {
	}.getType();

	private static final Map<String, PhrasePrint> PRINTS = new LinkedHashMap<>();
	private static boolean loaded;

	private VoicePrintStore() {
	}

	public static Path path() {
		return FMLPaths.GAMEDIR.get().resolve("jjkvoice").resolve("voiceprints.json");
	}

	public static synchronized Optional<PhrasePrint> find(String phrase) {
		ensureLoaded();
		return Optional.ofNullable(PRINTS.get(VoiceConfig.normalisePhrase(phrase)));
	}

	public static synchronized List<PhrasePrint> all() {
		ensureLoaded();
		return new ArrayList<>(PRINTS.values());
	}

	public static synchronized boolean isEnrolled(String phrase) {
		return find(phrase).isPresent();
	}

	/** Replaces any previous print for this phrase and recalculates its threshold. */
	public static synchronized void enroll(String phrase, List<float[][]> templates) {
		ensureLoaded();
		String key = VoiceConfig.normalisePhrase(phrase);
		if (key.isEmpty() || templates == null || templates.size() < 2)
			return;

		VoiceConfig config = VoiceConfig.get();
		PhrasePrint print = new PhrasePrint();
		print.phrase = key;
		print.templates = new ArrayList<>(templates);
		print.threshold = calibrate(templates, config.thresholdMultiplier, config.absoluteMaxDistance);
		PRINTS.put(key, print);
		save();
	}

	public static synchronized boolean forget(String phrase) {
		ensureLoaded();
		boolean removed = PRINTS.remove(VoiceConfig.normalisePhrase(phrase)) != null;
		if (removed)
			save();
		return removed;
	}

	public static synchronized void forgetAll() {
		ensureLoaded();
		PRINTS.clear();
		save();
	}

	/**
	 * Derives the accept threshold from how much the player's own repeats differ.
	 *
	 * <p>A fixed number could never suit every voice, microphone, and room. The
	 * widest gap between two recordings of the same phrase is exactly that
	 * speaker's natural variation, so the threshold is that gap with a margin --
	 * self-tuning per phrase, and tight for players who enroll consistently.
	 */
	private static double calibrate(List<float[][]> templates, double multiplier, double ceiling) {
		double widest = 0.0D;
		for (int i = 0; i < templates.size(); i++) {
			for (int j = i + 1; j < templates.size(); j++) {
				double distance = DtwMatcher.distance(templates.get(i), templates.get(j));
				if (distance != Double.MAX_VALUE)
					widest = Math.max(widest, distance);
			}
		}
		if (widest <= 0.0D)
			return ceiling;
		return Math.min(widest * multiplier, ceiling);
	}

	private static void ensureLoaded() {
		if (loaded)
			return;
		loaded = true;
		Path path = path();
		if (!Files.isRegularFile(path))
			return;
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			Map<String, PhrasePrint> stored = GSON.fromJson(reader, PRINT_MAP);
			if (stored == null)
				return;
			stored.forEach((key, print) -> {
				if (print != null && print.templates != null && !print.templates.isEmpty())
					PRINTS.put(key, print);
			});
			JjkVoiceMod.LOGGER.info("Loaded {} enrolled voiceprint(s)", PRINTS.size());
		} catch (IOException | JsonSyntaxException exception) {
			JjkVoiceMod.LOGGER.error("Could not read {}", path, exception);
		}
	}

	private static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(PRINTS, writer);
			}
		} catch (IOException exception) {
			JjkVoiceMod.LOGGER.error("Could not write {}", path, exception);
		}
	}

	/** One phrase the player taught the System, and what counts as saying it. */
	public static final class PhrasePrint {
		public String phrase;
		public List<float[][]> templates;
		public double threshold;

		public int sampleCount() {
			return templates == null ? 0 : templates.size();
		}
	}
}
