package net.efkrdnz.jjkstrongest.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.procedures.VCTexeProcedure;

/**
 * The public entry point for driving the mod by name.
 *
 * <p>This exists so companion mods have one seam to hold onto instead of calling
 * into {@code procedures}. It is deliberately thin: the underlying code already
 * checks which sorcerer the player is, and each technique applies its own costs
 * and cooldowns. Re-checking any of that here would create a second source of
 * truth that could disagree.
 *
 * <p>There are two kinds of command, because the mod works in two steps.
 *
 * <ul>
 * <li><b>Actions</b> ({@link #actionKeys()}) do something immediately —
 *     opening a domain, throwing a Dismantle, an Inumaki Cursed Speech word.
 * <li><b>Selections</b> ({@link #movesetKeys()}) switch which ability is active,
 *     exactly as picking it in the radial menu does. The technique keybinds then
 *     act on that selection.
 * </ul>
 *
 * <p>The split is not arbitrary. Several abilities are charge-and-release —
 * Hollow Purple needs {@code charge_purple >= 3} and the charging effect before
 * it will fire — so there is no honest way to express them as a single action.
 * Selecting is what a player actually does first.
 *
 * <p>Replaces the previous arrangement, where an external program wrote a word
 * into {@code Documents/JJKVoiceCommands/command.txt} and the mod polled that
 * file every five ticks on the server thread.
 */
public final class JjkVoiceApi {
	/**
	 * Commands that do something immediately.
	 *
	 * <p>These are the canonical spellings. {@link VCTexeProcedure} matches with
	 * {@code contains}, so its own aliases ("explode" for burst, "come_here" for
	 * pull, and so on) still work if something passes them — but the aliases are
	 * not listed here, because a few of them overlap. {@code be_crushed} contains
	 * {@code crush} and so reaches the crush branch first; sticking to canonical
	 * keys avoids depending on that ordering.
	 */
	private static final Set<String> ACTION_KEYS = Set.of(
			// Gojo and Sukuna
			"domain_expansion",
			// Sukuna
			"dismantle", "fuga",
			// Inumaki - Cursed Speech
			"dont_move", "die", "blast", "crush", "burst", "sleep", "flee", "rot",
			"twist", "burn", "fall", "spit", "pull", "shrink", "weep", "kneel");

	/**
	 * Abilities that can be made active, mapped to the label shown when they are.
	 *
	 * <p>Mirrors the radial ability menu one-for-one; keeping the same labels means
	 * a spoken selection and a clicked one are indistinguishable to the player.
	 */
	private static final Map<String, String> MOVESETS = buildMovesets();

	private static Map<String, String> buildMovesets() {
		Map<String, String> movesets = new LinkedHashMap<>();
		movesets.put("gojo_blue", "Blue");
		movesets.put("gojo_limitless", "Limitless");
		movesets.put("gojo_red", "Red");
		movesets.put("gojo_purple", "Purple");
		movesets.put("gojo_melee", "Melee");
		movesets.put("sukuna_cleave", "Cleave");
		movesets.put("sukuna_dismantle", "Dismantle");
		movesets.put("sukuna_fuga", "Fuga");
		movesets.put("sukuna_wcs", "World Slash");
		movesets.put("sukuna_shrine", "Shrine");
		movesets.put("sukuna_melee", "Melee");
		movesets.put("all_generic", "Generic");
		movesets.put("yuji_bloodmanipulation", "Blood Manipulation");
		movesets.put("yuji_shrine", "Shrine");
		movesets.put("yuji_divergentfist", "Divergent Fist");
		movesets.put("yuji_melee", "Melee");
		movesets.put("inumaki_assault", "Assault");
		movesets.put("inumaki_control", "Control");
		movesets.put("inumaki_binding", "Binding");
		movesets.put("inumaki_utility", "Utility");
		movesets.put("inumaki_melee", "Melee");
		movesets.put("reverse_cursed_technique", "RCT");
		return Map.copyOf(movesets);
	}

	/**
	 * The chant a moveset starts when its technique key is held.
	 *
	 * <p>Some movesets have more than one -- Dismantle can chant {@code dismantle}
	 * or {@code dis_net}, World Slash steps through {@code wcs1..3} -- so this
	 * lists the one a spoken chant starts from scratch. A chant already running is
	 * always extended as-is rather than replaced, which is how a player who opened
	 * with the keybind can carry on with their voice.
	 */
	private static final Map<String, String> MOVESET_CHANTS = buildMovesetChants();

	private static Map<String, String> buildMovesetChants() {
		Map<String, String> chants = new LinkedHashMap<>();
		chants.put("gojo_blue", "blue");
		chants.put("gojo_red", "red");
		chants.put("gojo_purple", "purple");
		chants.put("gojo_limitless", "teleport");
		chants.put("sukuna_dismantle", "dismantle");
		chants.put("sukuna_cleave", "cleave");
		chants.put("sukuna_fuga", "flame_arrow");
		chants.put("sukuna_wcs", "wcs1");
		return Map.copyOf(chants);
	}

	/** Persistent-data keys backing a spoken chant. */
	public static final String HOLD_TICKS = "jjkvoice_hold";
	public static final String HOLD_OWNED = "jjkvoice_hold_owned";

	private JjkVoiceApi() {
	}

	/** Movesets that can be chanted, i.e. that start a chant when held. */
	public static Set<String> chantableMovesets() {
		return MOVESET_CHANTS.keySet();
	}

	/**
	 * Charges the player's current ability as if they had held its technique key
	 * for {@code holdTicks} ticks.
	 *
	 * <p>Nothing about the charge curve is reimplemented here. Holding works by
	 * leaving {@code chanting} set while ChantOnTickProcedure advances ChantCounter
	 * one per tick and trips the ability's own thresholds; this simply supplies
	 * that held state from a spoken chant instead of a key, so the multipliers, the
	 * tier thresholds and the tier sounds are identical because they are the same
	 * code running.
	 *
	 * <p>A chant already in progress is extended rather than restarted, so chanting
	 * repeatedly accumulates exactly as holding longer would.
	 *
	 * @param holdTicks ticks of hold to grant, normally the spoken duration
	 * @return the chant state now running, or empty when the ability cannot chant
	 */
	public static String chant(ServerPlayer player, int holdTicks) {
		if (player == null || holdTicks <= 0)
			return "";

		String active = player.getPersistentData().getString("chanting");
		if (active == null || active.isEmpty()) {
			JjkStrongestModVariables.PlayerVariables variables = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
			String chant = MOVESET_CHANTS.get(normalise(variables.current_moveset));
			if (chant == null)
				return "";
			player.getPersistentData().putString("chanting", chant);
			// Marked as ours so the drain below only ever ends a chant that a voice
			// started. A player holding the key keeps their own chant.
			player.getPersistentData().putBoolean(HOLD_OWNED, true);
			active = chant;
		}

		double pending = player.getPersistentData().getDouble(HOLD_TICKS);
		player.getPersistentData().putDouble(HOLD_TICKS, pending + holdTicks);
		return active;
	}

	/** Commands that fire a technique immediately. */
	public static Set<String> actionKeys() {
		return ACTION_KEYS;
	}

	/** Abilities that can be selected, i.e. made the active moveset. */
	public static Set<String> movesetKeys() {
		return MOVESETS.keySet();
	}

	/** Everything {@link #run} accepts, for building an allow-list against. */
	public static Set<String> commandKeys() {
		java.util.Set<String> all = new java.util.LinkedHashSet<>(ACTION_KEYS);
		all.addAll(MOVESETS.keySet());
		return Set.copyOf(all);
	}

	public static boolean isCommandKey(String key) {
		String normalised = normalise(key);
		return ACTION_KEYS.contains(normalised) || MOVESETS.containsKey(normalised);
	}

	public static String normalise(String key) {
		return key == null ? "" : key.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
	}

	/**
	 * Runs whatever {@code key} names — an action or an ability selection.
	 *
	 * <p>Server side only. Whether anything actually happens is up to the mod: a
	 * Gojo player saying "dismantle" is simply ignored, exactly as it is when the
	 * key arrives from any other source.
	 *
	 * @return false when the player or key was unusable; true once the request was
	 *         handed on, which is not a promise that a technique fired
	 */
	public static boolean run(ServerPlayer player, String key) {
		if (player == null)
			return false;
		String command = normalise(key);
		if (MOVESETS.containsKey(command))
			return selectMoveset(player, command);
		if (ACTION_KEYS.contains(command))
			return cast(player, command);
		return false;
	}

	/** Fires an action. Does nothing for a moveset key; use {@link #run}. */
	public static boolean cast(ServerPlayer player, String key) {
		if (player == null)
			return false;
		String command = normalise(key);
		if (!ACTION_KEYS.contains(command))
			return false;
		VCTexeProcedure.execute(player.level(), player.getX(), player.getY(), player.getZ(), player, command);
		return true;
	}

	/**
	 * Makes an ability the active one, as the radial menu does.
	 *
	 * <p>Deliberately identical to the menu path, down to the confirmation label,
	 * so a spoken selection behaves exactly like a clicked one. The technique
	 * keybinds and every charge requirement then apply unchanged.
	 */
	public static boolean selectMoveset(ServerPlayer player, String moveset) {
		if (player == null)
			return false;
		String key = normalise(moveset);
		String label = MOVESETS.get(key);
		if (label == null)
			return false;

		JjkStrongestModVariables.PlayerVariables variables = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
		variables.current_moveset = key;
		variables.syncPlayerVariables(player);
		player.displayClientMessage(Component.literal("Selected: " + label), true);
		return true;
	}

	/** Convenience for callers that want a stable order, e.g. for a config file. */
	public static List<String> sortedCommandKeys() {
		return commandKeys().stream().sorted().toList();
	}
}
