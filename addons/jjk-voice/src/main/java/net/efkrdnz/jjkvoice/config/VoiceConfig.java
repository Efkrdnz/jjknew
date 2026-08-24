package net.efkrdnz.jjkvoice.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.neoforged.fml.loading.FMLPaths;

import net.efkrdnz.jjkvoice.JjkVoiceMod;
import net.efkrdnz.jjkvoice.compat.JjkBridge;

/**
 * Player-owned recognition settings, stored as plain JSON so they can be edited
 * by hand without opening the game.
 *
 * <p>The shape differs from a single-skill voice addon: JJK Strongest has many
 * techniques, so this maps each host-mod command key to every phrase that should
 * fire it. Recognition here is acoustic rather than linguistic, so "dismantle"
 * and "kaisen" are simply two different things to listen for -- each is enrolled
 * separately and either one firing runs the same technique. That is what makes
 * near-misses recoverable: add whatever you actually say out loud.
 *
 * <p>Enrolling every phrase at once is a long sit, so partial enrollment is a
 * first-class state. A command with no voiceprint is skipped during matching
 * rather than treated as an error, which lets a player teach the System the three
 * techniques they actually use and ignore the rest.
 */
public final class VoiceConfig {
	public static final String MODE_VOICEPRINT = "voiceprint";
	public static final String MODE_SHOUT = "shout";

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile VoiceConfig instance;

	/**
	 * {@code voiceprint} matches against recordings you enrolled. {@code shout}
	 * fires the {@link #shoutCommand} on any loud vocalisation, which is useful for
	 * confirming the microphone path works before enrolling anything.
	 */
	public String mode = MODE_VOICEPRINT;

	/**
	 * Host-mod command key to the phrases that should fire it.
	 *
	 * <p>Keys must be ones the host mod understands; unknown keys are dropped on
	 * load rather than silently kept, so a typo shows up as a missing command in
	 * {@code /jjkvoice status} instead of a phrase that never works.
	 */
	public Map<String, List<String>> commands = defaultCommands();

	/** Which command {@code shout} mode fires. Only used in that mode. */
	public String shoutCommand = "domain_expansion";

	/**
	 * How far past your own natural variation a clip may drift and still count.
	 * Enrollment measures how much your repeats of a phrase differ from each
	 * other; this multiplies that spread to get the accept threshold. Raise it if
	 * the game keeps refusing you, lower it if unrelated words trigger a technique.
	 */
	public double thresholdMultiplier = 1.35D;

	/** Hard ceiling, so a wildly inconsistent enrollment cannot accept everything. */
	public double absoluteMaxDistance = 60.0D;

	/** Recordings taken per phrase during enrollment. More is steadier, and slower. */
	public int enrollmentSamples = 3;

	/** Clips outside this range are discarded before any matching work happens. */
	public double minSpeechSeconds = 0.25D;
	public double maxSpeechSeconds = 3.0D;

	/** Loudness a clip must reach in {@code shout} mode, as normalised RMS. */
	public double shoutRmsThreshold = 0.06D;

	/** Print the matched phrase and its distance to chat. Useful while tuning. */
	public boolean announceMatches = true;

	/**
	 * Extra chant phrases per ability, on top of the ability's own phrases.
	 *
	 * <p>Empty by default, and that is usually enough: saying an ability's name
	 * while it is <em>not</em> active selects it, and saying it again once it
	 * <em>is</em> active chants it. Nothing extra to enroll. Put a real incantation
	 * here when you would rather charge with different words than you select with.
	 */
	public Map<String, List<String>> chants = new LinkedHashMap<>();

	/**
	 * How far past a phrase's accept threshold still counts as a chant.
	 *
	 * <p>Chanting is the safe place to be generous. A near miss here only charges
	 * slightly, where a near miss on firing would waste a cooldown -- so chants use
	 * this looser band and are credited at {@link #nearChantCredit} of the time
	 * spoken, while actions keep the tight threshold.
	 */
	public double chantNearMultiplier = 1.75D;

	/** Share of the spoken time a near-miss chant is worth. */
	public double nearChantCredit = 0.5D;

	/** Ceiling on ticks one chant can grant, so a held key cannot be out-charged. */
	public int maxChantTicks = 60;

	/**
	 * The starting phrase list, carried over from the phrase map the previous
	 * speech-to-text app used.
	 *
	 * <p>Only the phrasings a player would actually choose to say are kept. That
	 * old map also listed things like "domain expression", "hello purple" and
	 * "this mantle" -- those were not alternatives, they were the transcriber's
	 * mistakes, written down so the wrong transcript still mapped to the right
	 * ability. Nothing here transcribes, so there is no wrong transcript to catch:
	 * matching is acoustic and has no dictionary, which means it cannot confuse one
	 * word for another. Enrolling a misreading would only widen what counts as the
	 * phrase and make false triggers more likely.
	 *
	 * <p>Seven abilities rather than every command the mod exposes, because that is
	 * what the old map covered and every extra phrase costs enrollment time. The
	 * rest are still reachable with {@code /jjkvoice add <command> <phrase>}, and
	 * tab-complete lists them.
	 */
	private static Map<String, List<String>> defaultCommands() {
		Map<String, List<String>> defaults = new LinkedHashMap<>();

		// Immediate actions.
		defaults.put("domain_expansion", new ArrayList<>(List.of("domain expansion", "ryouiki tenkai")));
		defaults.put("dismantle", new ArrayList<>(List.of("dismantle", "kaisen", "slash")));
		defaults.put("fuga", new ArrayList<>(List.of("fuga", "open the furnace", "divine flames", "flame arrow")));

		// Ability selections. The old map called these hollow_purple, reversal_red,
		// lapse_blue and cleave; those are the spoken names, while the keys below are
		// what the mod calls the abilities in its own radial menu.
		defaults.put("gojo_purple", new ArrayList<>(List.of("hollow purple", "imaginary purple", "imaginary technique purple")));
		defaults.put("gojo_red", new ArrayList<>(List.of("reversal red")));
		defaults.put("gojo_blue", new ArrayList<>(List.of("lapse blue")));
		defaults.put("sukuna_cleave", new ArrayList<>(List.of("cleave")));

		// Inumaki's Cursed Speech. One word each, deliberately: they are spoken as
		// single commands and every extra phrase is another enrollment sitting.
		// These fire immediately -- Cursed Speech has no charge state to build.
		defaults.put("dont_move", new ArrayList<>(List.of("don't move")));
		defaults.put("die", new ArrayList<>(List.of("die")));
		defaults.put("blast", new ArrayList<>(List.of("blast")));
		defaults.put("crush", new ArrayList<>(List.of("crush")));
		defaults.put("burst", new ArrayList<>(List.of("burst")));
		defaults.put("sleep", new ArrayList<>(List.of("sleep")));
		defaults.put("flee", new ArrayList<>(List.of("run away")));
		defaults.put("rot", new ArrayList<>(List.of("rot")));
		defaults.put("twist", new ArrayList<>(List.of("twist")));
		defaults.put("burn", new ArrayList<>(List.of("burn")));
		defaults.put("fall", new ArrayList<>(List.of("fall")));
		defaults.put("spit", new ArrayList<>(List.of("spit")));
		defaults.put("pull", new ArrayList<>(List.of("come here")));
		defaults.put("shrink", new ArrayList<>(List.of("shrink")));
		defaults.put("weep", new ArrayList<>(List.of("weep")));
		defaults.put("kneel", new ArrayList<>(List.of("kneel")));

		return defaults;
	}

	public static VoiceConfig get() {
		VoiceConfig current = instance;
		if (current == null) {
			synchronized (VoiceConfig.class) {
				current = instance;
				if (current == null) {
					current = load();
					instance = current;
				}
			}
		}
		return current;
	}

	public static Path path() {
		return FMLPaths.CONFIGDIR.get().resolve("jjkvoice.json");
	}

	public static VoiceConfig reload() {
		VoiceConfig loaded = load();
		instance = loaded;
		return loaded;
	}

	/** Every configured phrase, flattened. Order follows the command order. */
	public List<String> allPhrases() {
		List<String> phrases = new ArrayList<>();
		commands.forEach((key, list) -> {
			if (list != null)
				phrases.addAll(list);
		});
		return phrases;
	}

	/** The command a phrase belongs to, or empty when the phrase is unknown. */
	public String commandFor(String phrase) {
		String needle = normalisePhrase(phrase);
		for (Map.Entry<String, List<String>> entry : commands.entrySet()) {
			if (entry.getValue() != null && entry.getValue().contains(needle))
				return entry.getKey();
		}
		return "";
	}

	public List<String> phrasesFor(String commandKey) {
		List<String> phrases = commands.get(normaliseCommand(commandKey));
		return phrases == null ? List.of() : List.copyOf(phrases);
	}

	/**
	 * Adds a phrase to a command.
	 *
	 * @return false when the command is unknown to the host mod, or the phrase is
	 *         empty or already bound somewhere
	 */
	public boolean addPhrase(String commandKey, String phrase) {
		String command = normaliseCommand(commandKey);
		String cleaned = normalisePhrase(phrase);
		if (command.isEmpty() || cleaned.isEmpty())
			return false;
		if (!JjkBridge.commandKeys().contains(command))
			return false;
		if (!commandFor(cleaned).isEmpty())
			return false;
		commands.computeIfAbsent(command, key -> new ArrayList<>()).add(cleaned);
		save();
		return true;
	}

	private static VoiceConfig load() {
		Path path = path();
		if (!Files.isRegularFile(path)) {
			VoiceConfig defaults = new VoiceConfig();
			defaults.save();
			return defaults;
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			VoiceConfig loaded = GSON.fromJson(reader, VoiceConfig.class);
			if (loaded == null)
				return new VoiceConfig();
			loaded.sanitise();
			return loaded;
		} catch (IOException | JsonSyntaxException exception) {
			JjkVoiceMod.LOGGER.error("Could not read {}; falling back to defaults", path, exception);
			return new VoiceConfig();
		}
	}

	public void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			JjkVoiceMod.LOGGER.error("Could not write {}", path, exception);
		}
	}

	/** Repairs values a hand-edited file could otherwise put out of range. */
	private void sanitise() {
		if (commands == null || commands.isEmpty())
			commands = defaultCommands();

		Map<String, List<String>> cleanedCommands = new LinkedHashMap<>();
		List<String> seenPhrases = new ArrayList<>();
		commands.forEach((rawKey, rawPhrases) -> {
			String key = normaliseCommand(rawKey);
			// Dropping unknown keys rather than keeping them means a typo surfaces in
			// /jjkvoice status as a missing command, instead of an enrolled phrase
			// that silently never fires.
			if (key.isEmpty() || !JjkBridge.commandKeys().contains(key) || rawPhrases == null)
				return;
			List<String> phrases = new ArrayList<>();
			for (String rawPhrase : rawPhrases) {
				String phrase = normalisePhrase(rawPhrase);
				// One phrase cannot mean two techniques; first binding wins.
				if (!phrase.isEmpty() && !seenPhrases.contains(phrase)) {
					phrases.add(phrase);
					seenPhrases.add(phrase);
				}
			}
			if (!phrases.isEmpty())
				cleanedCommands.put(key, phrases);
		});
		// A file that survived sanitising with nothing usable left is treated the
		// same as a missing one, rather than leaving the player with no phrases.
		commands = cleanedCommands.isEmpty() ? defaultCommands() : cleanedCommands;

		if (!MODE_SHOUT.equals(mode))
			mode = MODE_VOICEPRINT;
		shoutCommand = normaliseCommand(shoutCommand);
		if (!JjkBridge.commandKeys().contains(shoutCommand))
			shoutCommand = commands.keySet().iterator().next();

		thresholdMultiplier = clamp(thresholdMultiplier, 1.0D, 4.0D);
		absoluteMaxDistance = clamp(absoluteMaxDistance, 1.0D, 500.0D);
		enrollmentSamples = (int) clamp(enrollmentSamples, 2, 10);
		minSpeechSeconds = clamp(minSpeechSeconds, 0.05D, 2.0D);
		maxSpeechSeconds = clamp(maxSpeechSeconds, minSpeechSeconds + 0.1D, 5.0D);
		shoutRmsThreshold = clamp(shoutRmsThreshold, 0.001D, 1.0D);

		if (chants == null)
			chants = new LinkedHashMap<>();
		chantNearMultiplier = clamp(chantNearMultiplier, 1.0D, 4.0D);
		nearChantCredit = clamp(nearChantCredit, 0.0D, 1.0D);
		maxChantTicks = (int) clamp(maxChantTicks, 5, 200);
	}

	/**
	 * Everything that counts as chanting {@code moveset}: its own selection phrases
	 * plus any extra incantations configured for it.
	 */
	public List<String> chantPhrasesFor(String moveset) {
		String key = normaliseCommand(moveset);
		List<String> phrases = new ArrayList<>(phrasesFor(key));
		List<String> extra = chants.get(key);
		if (extra != null) {
			for (String phrase : extra) {
				String cleaned = normalisePhrase(phrase);
				if (!cleaned.isEmpty() && !phrases.contains(cleaned))
					phrases.add(cleaned);
			}
		}
		return phrases;
	}

	public static String normalisePhrase(String phrase) {
		return phrase == null ? "" : phrase.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	public static String normaliseCommand(String key) {
		return key == null ? "" : key.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
	}

	private static double clamp(double value, double min, double max) {
		if (Double.isNaN(value))
			return min;
		return Math.max(min, Math.min(max, value));
	}
}
