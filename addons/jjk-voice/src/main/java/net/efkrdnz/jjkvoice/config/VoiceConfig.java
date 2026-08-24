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
import java.util.Set;

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

	/**
	 * The same ceiling for a line of an incantation, which is allowed to run longer
	 * because it is a recited line rather than a shouted word. A clip past
	 * {@link #maxSpeechSeconds} is only kept if an incantation line is what it
	 * turned out to match.
	 */
	public double maxIncantationSeconds = 6.0D;

	/** Loudness a clip must reach in {@code shout} mode, as normalised RMS. */
	public double shoutRmsThreshold = 0.06D;

	/** Print the matched phrase and its distance to chat. Useful while tuning. */
	public boolean announceMatches = true;

	/** Show the on-screen list of lines you can say next. */
	public boolean hudEnabled = true;

	/**
	 * Where that list sits, as fractions of the screen rather than pixels, so it
	 * stays put across a resolution or GUI-scale change. Set by dragging it in
	 * {@code /jjkvoice hud}.
	 */
	public double hudX = 0.012D;
	public double hudY = 0.62D;

	/**
	 * Which shape this file was written in.
	 *
	 * <p>Only reason it exists: chanting changed what a phrase <em>means</em>, so
	 * an untouched older file is not merely missing entries, it is wrong. Filling
	 * gaps alone would leave "dismantle" bound to the immediate slash and the
	 * Dismantle stance permanently unchantable, because one phrase cannot mean both
	 * "do it now" and "wind it up".
	 */
	public int configVersion;

	/** Bumped when an upgrade needs more than new keys being filled in. */
	private static final int CURRENT_VERSION = 4;

	/**
	 * The incantations that charge an ability, keyed by the ability they charge.
	 *
	 * <p>Separate from {@link #commands} because they mean something different.
	 * An ability's name selects it, charges it a step, or lets it go, depending on
	 * where you already are. An incantation only ever charges.
	 *
	 * <p><b>These are lines, in order, not alternatives.</b> You say them one at a
	 * time and each one charges a tier; getting to the end of the list carries the
	 * technique to full output, which is what reciting the whole thing is for. That
	 * is also the only shape that works acoustically -- a whole incantation in one
	 * breath runs past {@link #maxSpeechSeconds}, and the recogniser matches a fixed
	 * utterance rather than listening continuously.
	 *
	 * <p>Two incantations may share a line -- Blue and Red both open on "phase" --
	 * and it is enrolled once, since the two sound identical and no amount of
	 * bookkeeping would tell them apart. A shared line is read as belonging to the
	 * ability you have selected, so select before you recite when the opening is
	 * one of these. A line must still not collide with a {@link #commands} phrase,
	 * where it would be ambiguous with no way to resolve it.
	 *
	 * <p>The entries below are a starting point, not scripture. Put whatever you
	 * actually say here, split however you actually pause; the recogniser has no
	 * dictionary and does not care whether it is canon.
	 */
	public Map<String, List<String>> chants = defaultChants();

	private static Map<String, List<String>> defaultChants() {
		Map<String, List<String>> chants = new LinkedHashMap<>();
		chants.put("gojo_blue", new ArrayList<>(List.of("phase", "twilight", "eyes of wisdom")));
		chants.put("gojo_red", new ArrayList<>(List.of("phase", "paramita", "pillars of light")));
		chants.put("gojo_purple", new ArrayList<>(List.of("nine ropes", "polarized light",
				"crow and declaration", "between front and back")));
		chants.put("sukuna_dismantle", new ArrayList<>(List.of("dragon scales", "repulsion", "twin meteor")));
		return chants;
	}

	/**
	 * How far past a phrase's accept threshold still counts as a chant.
	 *
	 * <p>Chanting is the safe place to be generous. A near miss here is worth half
	 * a tier, so two of them add up to one and the words are never simply wasted;
	 * a near miss on firing would spend a cooldown on a guess. Ability names and
	 * incantations get this looser band, immediate actions keep the tight one.
	 */
	public double chantNearMultiplier = 1.75D;

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
	 * <p>You never face the whole list. Only the commands your own technique
	 * includes are searched or offered, so this is five or so phrases for Gojo and
	 * seventeen for Inumaki, not all of them. Anything left out is still reachable
	 * with {@code /jjkvoice add <command> <phrase>}, and tab-complete lists them.
	 */
	private static Map<String, List<String>> defaultCommands() {
		Map<String, List<String>> defaults = new LinkedHashMap<>();

		// Immediate techniques. Cursed Speech takes effect the moment it is spoken
		// and Sukuna's slash is meant to be spammable, so these stay single words.
		defaults.put("domain_expansion", new ArrayList<>(List.of("domain expansion", "ryouiki tenkai")));
		// Dismantle is thrown in whichever shape you name, spending the same chant
		// as power, area or duration. All three are projectiles.
		defaults.put("dismantle", new ArrayList<>(List.of("dismantle", "kaisen")));
		defaults.put("dismantle_barrage", new ArrayList<>(List.of("dismantle barrage")));
		defaults.put("dismantle_net", new ArrayList<>(List.of("dismantle net")));
		defaults.put("fuga", new ArrayList<>(List.of("fuga", "open the furnace", "divine flames")));
		defaults.put("release", new ArrayList<>(List.of("release")));

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

		// Ability names. Saying one selects it, says it again to charge, and again
		// to let it go -- so these must not collide with the words above. "kaisen"
		// slashes; "dismantle" is the stance you charge.
		defaults.put("gojo_blue", new ArrayList<>(List.of("lapse blue")));
		defaults.put("gojo_red", new ArrayList<>(List.of("reversal red")));
		defaults.put("gojo_purple", new ArrayList<>(List.of("hollow purple")));
		defaults.put("sukuna_cleave", new ArrayList<>(List.of("cleave")));

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
			// Stamped here rather than in the field, so that a file written before
			// versioning -- which names no version at all -- reads as the oldest
			// rather than as whatever the current one happens to be.
			defaults.configVersion = CURRENT_VERSION;
			defaults.save();
			return defaults;
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			VoiceConfig loaded = GSON.fromJson(reader, VoiceConfig.class);
			if (loaded == null)
				return new VoiceConfig();
			loaded.migrate();
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
	/**
	 * Brings an older file up to date without discarding anything chosen in it.
	 *
	 * <p>Missing keys are filled from the defaults on every load, so a technique
	 * added later simply appears. Removing an entry you did not want is therefore
	 * not a way to keep it gone -- clear its phrase list instead, which survives.
	 */
	private void migrate() {
		if (commands == null)
			commands = new LinkedHashMap<>();
		if (chants == null)
			chants = new LinkedHashMap<>();

		if (configVersion < 4) {
			// Incantations and the Dismantle phrases have both been reshaped. Only
			// drop an entry that still matches something this addon shipped, so
			// anything written by hand is left in whatever shape its owner wanted;
			// the defaults are filled back in below.
			dropUntouched(chants, SUPERSEDED_CHANTS);
			dropUntouched(commands, SUPERSEDED_COMMANDS);
			// "imaginary purple" was briefly a line of Purple's incantation.
			List<String> purple = commands.get("gojo_purple");
			if (purple != null)
				purple.removeIf(phrase -> "imaginary purple".equals(normalisePhrase(phrase)));
		}

		if (configVersion < 2) {
			// "dismantle" now selects and charges the stance; the immediate slash
			// answers to "kaisen" and "slash", which that entry already carried.
			List<String> slash = commands.get("dismantle");
			if (slash != null) {
				slash.removeIf(phrase -> "dismantle".equals(normalisePhrase(phrase)));
				if (slash.isEmpty())
					commands.remove("dismantle");
			}
		}

		for (Map.Entry<String, List<String>> entry : defaultCommands().entrySet())
			commands.putIfAbsent(entry.getKey(), new ArrayList<>(entry.getValue()));
		for (Map.Entry<String, List<String>> entry : defaultChants().entrySet())
			chants.putIfAbsent(entry.getKey(), new ArrayList<>(entry.getValue()));

		if (configVersion < CURRENT_VERSION) {
			configVersion = CURRENT_VERSION;
			save();
		}
	}

	/** What {@link #defaultChants} used to hold, so an untouched copy can be replaced. */
	private static final Map<String, List<List<String>>> SUPERSEDED_CHANTS = Map.of(
			"gojo_blue", List.of(
					List.of("cursed technique lapse blue"),
					List.of("cursed technique lapse", "maximum output blue")),
			"gojo_red", List.of(
					List.of("phase paramita pillars of light", "cursed technique reversal red"),
					List.of("phase paramita", "pillars of light")),
			"gojo_purple", List.of(
					List.of("imaginary technique hollow purple"),
					List.of("imaginary technique", "imaginary purple")),
			"sukuna_dismantle", List.of(
					List.of("cursed technique dismantle")),
			"sukuna_wcs", List.of(
					List.of("world dismantling slash")));

	/** Command phrases that moved elsewhere, same rule: replace only if untouched. */
	private static final Map<String, List<List<String>>> SUPERSEDED_COMMANDS = Map.of(
			"dismantle", List.of(List.of("kaisen", "slash")),
			"sukuna_dismantle", List.of(List.of("dismantle")),
			"sukuna_wcs", List.of(List.of("world slash")));

	private void dropUntouched(Map<String, List<String>> current, Map<String, List<List<String>>> shipped) {
		for (Map.Entry<String, List<List<String>>> entry : shipped.entrySet()) {
			List<String> stored = current.get(entry.getKey());
			if (stored == null)
				continue;
			for (List<String> previous : entry.getValue())
				if (normalisedEquals(stored, previous)) {
					current.remove(entry.getKey());
					break;
				}
		}
	}

	private boolean normalisedEquals(List<String> stored, List<String> reference) {
		if (stored.size() != reference.size())
			return false;
		for (int i = 0; i < stored.size(); i++)
			if (!reference.get(i).equals(normalisePhrase(stored.get(i))))
				return false;
		return true;
	}

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
		maxIncantationSeconds = clamp(maxIncantationSeconds, maxSpeechSeconds, 12.0D);
		shoutRmsThreshold = clamp(shoutRmsThreshold, 0.001D, 1.0D);

		if (chants == null)
			chants = new LinkedHashMap<>();
		// An incantation for something that cannot be charged is dead weight the
		// recogniser would still compare against, so it goes the same way an unknown
		// command key does. World Slash used to be here.
		Map<String, List<String>> cleanedChants = new LinkedHashMap<>();
		chants.forEach((rawKey, rawLines) -> {
			String key = normaliseCommand(rawKey);
			if (key.isEmpty() || !JjkBridge.isChantable(key) || rawLines == null)
				return;
			List<String> lines = new ArrayList<>();
			for (String rawLine : rawLines) {
				String line = normalisePhrase(rawLine);
				// Repeating a line inside one incantation would make its position
				// ambiguous, and position is what the recital counts.
				if (!line.isEmpty() && !lines.contains(line))
					lines.add(line);
			}
			if (!lines.isEmpty())
				cleanedChants.put(key, lines);
		});
		chants = cleanedChants;
		chantNearMultiplier = clamp(chantNearMultiplier, 1.0D, 4.0D);
		hudX = clamp(hudX, 0.0D, 1.0D);
		hudY = clamp(hudY, 0.0D, 1.0D);
	}

	/**
	 * Everything that counts as chanting {@code moveset}: its own selection phrases
	 * plus any extra incantations configured for it.
	 */
	/**
	 * Every ability whose incantation has this exact line in this exact position.
	 *
	 * <p>Usually one. Blue and Red both open on "Phase", though, and the client has
	 * no business choosing between them -- they are the same sound. All of them go
	 * to the server, which narrows against the recital already running.
	 */
	public List<String> abilitiesWithLine(String phrase, int index, Set<String> allowed) {
		String wanted = normalisePhrase(phrase);
		List<String> sharing = new ArrayList<>();
		if (wanted.isEmpty() || index < 0)
			return sharing;
		for (Map.Entry<String, List<String>> entry : chants.entrySet()) {
			if (allowed != null && !allowed.contains(entry.getKey()))
				continue;
			List<String> lines = entry.getValue();
			if (lines != null && index < lines.size() && wanted.equals(normalisePhrase(lines.get(index))))
				sharing.add(entry.getKey());
		}
		return sharing;
	}

	/** Everything worth enrolling for one command: what selects it, and what chants it. */
	public List<String> allPhrasesFor(String command) {
		List<String> phrases = new ArrayList<>(phrasesFor(command));
		for (String line : incantationsFor(command))
			if (!phrases.contains(line))
				phrases.add(line);
		return phrases;
	}

	/** The incantation lines enrolled for one ability, in the order they are said. */
	public List<String> incantationsFor(String moveset) {
		List<String> phrases = chants.get(normaliseCommand(moveset));
		return phrases == null ? List.of() : List.copyOf(phrases);
	}

	/**
	 * The incantations for every ability the speaker is entitled to, ready for the
	 * recogniser. Abilities they cannot use are left out entirely rather than
	 * searched and then refused.
	 */
	public Map<String, List<String>> incantationsFor(Set<String> allowed, String preferred) {
		Map<String, List<String>> narrowed = new LinkedHashMap<>();
		// Selected ability first. Two incantations may share a line, and a shared
		// line gives the same distance under either -- the recogniser keeps the
		// first it saw, so order is the whole tie-break.
		put(narrowed, allowed, normaliseCommand(preferred));
		for (String moveset : chants.keySet())
			put(narrowed, allowed, moveset);
		return narrowed;
	}

	private void put(Map<String, List<String>> into, Set<String> allowed, String moveset) {
		if (moveset.isEmpty() || into.containsKey(moveset))
			return;
		if (allowed != null && !allowed.contains(moveset))
			return;
		List<String> lines = chants.get(moveset);
		if (lines != null && !lines.isEmpty())
			into.put(moveset, List.copyOf(lines));
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
