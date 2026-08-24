package net.efkrdnz.jjkstrongest.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.procedures.Technique1OnKeyPressProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique1OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique3OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique3OnKeypressProcedure;
import net.efkrdnz.jjkstrongest.procedures.VCTexeProcedure;

/**
 * The public entry point for driving the mod by name.
 *
 * <p>This exists so companion mods have one seam to hold onto instead of calling
 * into {@code procedures}. Each technique still applies its own costs and
 * cooldowns where it always did; what this adds is the decision of <em>which</em>
 * thing a spoken name means, and the check that the speaker is entitled to it at
 * all.
 *
 * <p>{@link #speak} is the whole interface. A name does one of four things
 * depending on what is already true of the player, which is what lets an
 * incantation and a technique name be spoken in sequence and mean "charge this,
 * now fire it":
 *
 * <ul>
 * <li><b>An action</b> ({@link #actionKeys()}) happens immediately — a domain, a
 *     Dismantle, an Inumaki Cursed Speech word.
 * <li><b>An ability not currently selected</b> becomes the selection, exactly as
 *     picking it in the radial menu does.
 * <li><b>An ability already selected</b> starts charging it, as holding its
 *     technique key does.
 * <li><b>An ability already being chanted</b> releases it, as letting that key up
 *     does — so the technique comes out at whatever tier the chanting reached.
 * </ul>
 *
 * <p>An <b>incantation</b> is the charging step on its own, and arrives a line at
 * a time: each line charges a tier, and reciting one to its end carries the
 * technique to full output, which is what reciting the whole thing is for. It
 * selects its ability first if needed, since it names it unambiguously.
 *
 * <p>Everything is gated on the speaker's own technique. Saying another
 * sorcerer's ability is not merely ineffective, it is unrecognised — it will not
 * select, charge, or fire.
 *
 * <p>Replaces the previous arrangement, where an external program wrote a word
 * into {@code Documents/JJKVoiceCommands/command.txt} and the mod polled that
 * file every five ticks on the server thread.
 */
public final class JjkVoiceApi {
	/**
	 * Commands that do something immediately, mapped to who may use them.
	 *
	 * <p>These are the canonical spellings. {@link VCTexeProcedure} matches with
	 * {@code contains}, so its own aliases ("explode" for burst, "come_here" for
	 * pull, and so on) still work if something passes them — but the aliases are
	 * not listed here, because a few of them overlap. {@code be_crushed} contains
	 * {@code crush} and so reaches the crush branch first; sticking to canonical
	 * keys avoids depending on that ordering.
	 */
	private static final Map<String, Set<String>> ACTIONS = buildActions();

	private static Map<String, Set<String>> buildActions() {
		Map<String, Set<String>> actions = new LinkedHashMap<>();
		actions.put("domain_expansion", Set.of("gojo", "sukuna"));
		actions.put("dismantle", Set.of("sukuna"));
		actions.put("fuga", Set.of("sukuna"));
		for (String word : List.of("dont_move", "die", "blast", "crush", "burst", "sleep", "flee", "rot",
				"twist", "burn", "fall", "spit", "pull", "shrink", "weep", "kneel"))
			actions.put(word, Set.of("inumaki"));
		return Map.copyOf(actions);
	}

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

	/** Abilities nobody is excluded from, whatever their technique. */
	private static final Set<String> UNIVERSAL = Set.of("all_generic", "reverse_cursed_technique");

	/** The technique key an ability charges on. They are not interchangeable. */
	private enum TechniqueKey {
		ONE {
			@Override
			void press(ServerPlayer player) {
				Technique1OnKeyPressProcedure.execute(player);
			}

			@Override
			void release(ServerPlayer player) {
				Technique1OnKeyPressedProcedure.execute(player);
			}
		},
		THREE {
			@Override
			void press(ServerPlayer player) {
				Technique3OnKeypressProcedure.execute(player);
			}

			@Override
			void release(ServerPlayer player) {
				Technique3OnKeyPressedProcedure.execute(player.level(), player.getX(), player.getY(), player.getZ(), player);
			}
		};

		abstract void press(ServerPlayer player);

		abstract void release(ServerPlayer player);
	}

	/**
	 * An ability a chant can charge, and how to drive it.
	 *
	 * @param chant the state the mod's own press handler sets
	 * @param key   which technique key charges it
	 * @param tiers the ChantCounter values that raise output, in order
	 */
	private record Chantable(String chant, TechniqueKey key, int[] tiers) {
	}

	/**
	 * The abilities that can actually be charged by chanting.
	 *
	 * <p>Deliberately short. An ability belongs here only if ChantOnTickProcedure
	 * has a branch that climbs ChantCounter for its state. Limitless
	 * ({@code teleport}) and Fuga ({@code flame_arrow}) have none, so their counter
	 * is reset every tick and there is no charge to build. Cleave is excluded for
	 * the opposite reason: its state runs CleaveHoldTickProcedure, which
	 * <em>performs</em> the technique after fifteen ticks rather than charging it,
	 * so chanting Cleave would cast it instead of powering it up.
	 *
	 * <p>The tier values mirror that procedure's thresholds rather than deriving
	 * anything. One chant advances the counter to the next of them, which is what
	 * makes a chant worth exactly one tier whether the ability's ladder is short
	 * (Blue, 10/20/30) or long (Purple, 70/90/110/130).
	 */
	private static final Map<String, Chantable> CHANTABLE = buildChantable();

	private static Map<String, Chantable> buildChantable() {
		Map<String, Chantable> chantable = new LinkedHashMap<>();
		chantable.put("gojo_blue", new Chantable("blue", TechniqueKey.THREE, new int[] {10, 20, 30}));
		chantable.put("gojo_red", new Chantable("red", TechniqueKey.THREE, new int[] {10, 20, 40}));
		chantable.put("gojo_purple", new Chantable("purple", TechniqueKey.THREE, new int[] {70, 90, 110, 130}));
		chantable.put("sukuna_dismantle", new Chantable("dismantle", TechniqueKey.ONE, new int[] {10, 20, 30}));
		chantable.put("sukuna_wcs", new Chantable("wcs1", TechniqueKey.ONE, new int[] {20, 40, 60}));
		return Map.copyOf(chantable);
	}

	/** Spoken command that releases a chant, as letting the technique key up does. */
	public static final String RELEASE = "release";

	/** Ticks an unreleased chant is held before it lapses. */
	private static final int EXPIRY_TICKS = 200;

	/** Persistent-data keys backing a spoken chant. */
	public static final String HOLD_TICKS = "jjkvoice_hold";
	public static final String HOLD_STATE = "jjkvoice_hold_state";

	/** How far through an incantation the player has recited, and for what. */
	private static final String INCANT_ABILITY = "jjkvoice_incant";
	private static final String INCANT_LINE = "jjkvoice_incant_line";

	private JjkVoiceApi() {
	}

	public static String normalise(String key) {
		return key == null ? "" : key.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
	}

	/** Commands that fire a technique immediately. */
	public static Set<String> actionKeys() {
		return ACTIONS.keySet();
	}

	/** Abilities that can be selected, i.e. made the active moveset. */
	public static Set<String> movesetKeys() {
		return MOVESETS.keySet();
	}

	/** Movesets that can be charged by chanting, i.e. that accept an incantation. */
	public static Set<String> chantableMovesets() {
		return CHANTABLE.keySet();
	}

	/** Everything {@link #speak} accepts, for building an allow-list against. */
	public static Set<String> commandKeys() {
		Set<String> all = new LinkedHashSet<>(ACTIONS.keySet());
		all.addAll(MOVESETS.keySet());
		all.add(RELEASE);
		return Set.copyOf(all);
	}

	public static boolean isCommandKey(String key) {
		String normalised = normalise(key);
		return RELEASE.equals(normalised) || ACTIONS.containsKey(normalised) || MOVESETS.containsKey(normalised);
	}

	/**
	 * Whether a sorcerer's own technique includes this ability.
	 *
	 * <p>Read off the naming rather than a second table, because the moveset IDs
	 * already carry it — {@code gojo_purple} is Gojo's by construction. A duplicate
	 * table would be one more thing to update when an ability is added, and would
	 * fail in the permissive direction whenever someone forgot.
	 */
	public static boolean owns(String sorcerer, String moveset) {
		String who = normalise(sorcerer);
		String what = normalise(moveset);
		if (!MOVESETS.containsKey(what))
			return false;
		return UNIVERSAL.contains(what) || (!who.isEmpty() && what.startsWith(who + "_"));
	}

	/** Whether this sorcerer may use an immediate action. */
	public static boolean ownsAction(String sorcerer, String action) {
		Set<String> allowed = ACTIONS.get(normalise(action));
		return allowed != null && allowed.contains(normalise(sorcerer));
	}

	/** Every ability and action this sorcerer is entitled to speak. */
	public static Set<String> commandKeysFor(String sorcerer) {
		Set<String> allowed = new LinkedHashSet<>();
		for (Map.Entry<String, Set<String>> action : ACTIONS.entrySet())
			if (action.getValue().contains(normalise(sorcerer)))
				allowed.add(action.getKey());
		for (String moveset : MOVESETS.keySet())
			if (owns(sorcerer, moveset))
				allowed.add(moveset);
		allowed.add(RELEASE);
		return Set.copyOf(allowed);
	}

	/**
	 * What speaking a name did, so the speaker can be told which of the four it
	 * was rather than inferring it from what happened on screen.
	 */
	public enum Spoken {
		/** Not this sorcerer's to say, or not a command at all. */
		UNRECOGNISED,
		/** An immediate technique fired. */
		CAST,
		/** The ability became the active one. */
		SELECTED,
		/** The ability gained charge. */
		CHARGED,
		/** A charged ability was let go. */
		RELEASED
	}

	/**
	 * Acts on a spoken name.
	 *
	 * <p>Server side only. The decision of what a name means lives here rather than
	 * in the caller because it depends on state the caller cannot see — which
	 * ability is selected, and whether one is mid-chant — and because a client that
	 * decided for itself could ask for a release it had not earned.
	 *
	 * @param exact whether the name was heard cleanly; a near miss still charges,
	 *              at half a tier, so it is not simply wasted
	 * @param line  which line of the ability's incantation this was, or negative
	 *              when the ability's own name was spoken instead
	 * @param lines how many lines that incantation has
	 */
	public static Spoken speak(ServerPlayer player, String key, boolean exact, int line, int lines) {
		if (player == null)
			return Spoken.UNRECOGNISED;
		String command = normalise(key);
		boolean incantation = line >= 0;
		JjkStrongestModVariables.PlayerVariables variables = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
		String sorcerer = normalise(variables.sorcerer);

		if (RELEASE.equals(command))
			return release(player) ? Spoken.RELEASED : Spoken.UNRECOGNISED;

		if (MOVESETS.containsKey(command)) {
			if (!owns(sorcerer, command))
				return Spoken.UNRECOGNISED;

			// An incantation names its ability unambiguously, so reciting one for
			// something not yet selected selects it rather than being thrown away.
			if (!command.equals(normalise(variables.current_moveset))) {
				// Switching ability drops any charge being built, the same way it is
				// not possible to hold Red's charge while reaching for Blue.
				cancel(player);
				if (!incantation)
					return selectMoveset(player, command) ? Spoken.SELECTED : Spoken.UNRECOGNISED;
				if (!selectMoveset(player, command))
					return Spoken.UNRECOGNISED;
			}

			Chantable ability = CHANTABLE.get(command);
			if (ability == null)
				// Selectable but not chargeable; saying its name again is a no-op
				// rather than a second selection message.
				return Spoken.SELECTED;

			// Already chanting this one: the name is the cue to let it go.
			if (!incantation && ability.chant().equals(player.getPersistentData().getString("chanting")))
				return release(player) ? Spoken.RELEASED : Spoken.UNRECOGNISED;

			// Only reciting an incantation to its end tops the technique out. Any
			// single line is worth a tier, so starting in the middle gains nothing
			// that saying the ability's name would not.
			boolean completed = incantation && advanceRecital(player, command, line, lines);
			return chant(player, exact, completed).isEmpty() ? Spoken.UNRECOGNISED : Spoken.CHARGED;
		}

		if (ACTIONS.containsKey(command)) {
			if (!ownsAction(sorcerer, command))
				return Spoken.UNRECOGNISED;
			VCTexeProcedure.execute(player.level(), player.getX(), player.getY(), player.getZ(), player, command);
			return Spoken.CAST;
		}
		return Spoken.UNRECOGNISED;
	}

	/**
	 * Records one line of an incantation and reports whether that finished it.
	 *
	 * <p>Lines count only when spoken in order. Being out of order is not punished
	 * -- the line still charges its tier -- it simply does not advance the recital,
	 * so the only way to full output is to actually recite the thing.
	 */
	private static boolean advanceRecital(ServerPlayer player, String ability, int line, int lines) {
		CompoundTag data = player.getPersistentData();
		int recited = ability.equals(data.getString(INCANT_ABILITY)) ? data.getInt(INCANT_LINE) : 0;
		data.putString(INCANT_ABILITY, ability);

		if (line != recited) {
			data.putInt(INCANT_LINE, recited);
			return false;
		}
		recited = line + 1;
		if (lines > 0 && recited >= lines) {
			data.putInt(INCANT_LINE, 0);
			return true;
		}
		data.putInt(INCANT_LINE, recited);
		return false;
	}

	/**
	 * Charges the player's current ability, as holding its technique key does.
	 *
	 * <p>Neither the charge nor the setup is reimplemented. Starting a chant runs
	 * the mod's own press handler, so the charge cost, the sorcerer check, the base
	 * output, the charge animation and the charging effect all apply exactly as
	 * they do from the keyboard — and when that handler refuses (no Blue charges,
	 * fewer than three Purple) its decision stands and nothing happens. Only then
	 * is ChantCounter advanced, and only onto the ability's own thresholds, so the
	 * multipliers and tier sounds are the same code running.
	 *
	 * <p>The chant then stays up until it is released or lapses, so the charge can
	 * be spent rather than evaporating the moment the words stop.
	 *
	 * @param exact whether the chant was heard cleanly; a near miss is worth half a
	 *              tier, so two of them add up to one and neither is wasted
	 * @param full  whether this completed an incantation, which tops the ability out
	 * @return the chant now running, or empty when the ability cannot be chanted
	 */
	public static String chant(ServerPlayer player, boolean exact, boolean full) {
		if (player == null)
			return "";
		JjkStrongestModVariables.PlayerVariables variables = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
		String moveset = normalise(variables.current_moveset);
		if (!owns(normalise(variables.sorcerer), moveset))
			return "";
		Chantable ability = CHANTABLE.get(moveset);
		if (ability == null)
			return "";

		String active = player.getPersistentData().getString("chanting");
		if (active == null || active.isEmpty()) {
			ability.key().press(player);
			active = player.getPersistentData().getString("chanting");
			if (active == null || !active.equals(ability.chant()))
				return "";
			player.getPersistentData().putString(HOLD_STATE, active);
		} else if (!active.equals(ability.chant())) {
			// Mid-chant on something else. Whatever started it owns it.
			return "";
		}

		advance(player, ability, exact, full);
		player.getPersistentData().putInt(HOLD_TICKS, EXPIRY_TICKS);
		return active;
	}

	/**
	 * Moves ChantCounter up the ability's thresholds.
	 *
	 * <p>ChantOnTickProcedure increments the counter and then tests it with
	 * {@code ==}, so the counter is parked one short and the following tick lands
	 * exactly on the threshold, which is what plays the tier sound and sets the
	 * multiplier. Assigning the threshold itself would step straight over it.
	 *
	 * <p>A clean incantation goes to the top, because that is what reciting one
	 * means. Anything else is worth a tier, and a near miss half of one, so two
	 * mishearings add up rather than each being wasted.
	 */
	private static void advance(ServerPlayer player, Chantable ability, boolean exact, boolean full) {
		int[] tiers = ability.tiers();
		if (full && exact) {
			player.getPersistentData().putDouble("ChantCounter", tiers[tiers.length - 1] - 1);
			return;
		}

		double counter = player.getPersistentData().getDouble("ChantCounter");
		int next = 0;
		int previous = 0;
		for (int tier : tiers) {
			// One past the counter, because the pending tick already claims that one.
			if (tier > counter + 1) {
				next = tier;
				break;
			}
			previous = tier;
		}
		if (next == 0)
			// Already at full output; further chanting cannot add to it.
			return;

		double target = next - 1;
		if (exact) {
			player.getPersistentData().putDouble("ChantCounter", target);
			return;
		}
		// Half the gap between this tier and the last, so two near chants are worth
		// one clean one instead of creeping ever closer without arriving.
		double half = (next - previous) / 2.0D;
		player.getPersistentData().putDouble("ChantCounter", Math.min(target, counter + half));
	}

	/** Drops a chant this started, without firing it. */
	private static void cancel(ServerPlayer player) {
		String owned = player.getPersistentData().getString(HOLD_STATE);
		if (owned == null || owned.isEmpty())
			return;
		if (owned.equals(player.getPersistentData().getString("chanting")))
			// Clearing it is enough: ChantOnTick zeroes the counter and the
			// multiplier on the next tick by itself.
			player.getPersistentData().putString("chanting", "");
		player.getPersistentData().putInt(HOLD_TICKS, 0);
		player.getPersistentData().putString(HOLD_STATE, "");
		forgetRecital(player);
	}

	/** Drops any part-recited incantation, so the next one starts from its opening. */
	private static void forgetRecital(ServerPlayer player) {
		player.getPersistentData().putString(INCANT_ABILITY, "");
		player.getPersistentData().putInt(INCANT_LINE, 0);
	}

	/**
	 * Releases a running chant, as letting the technique key up does.
	 *
	 * <p>Routed through the mod's own release handler, so the technique comes out
	 * at whatever tier was reached and every cost and cooldown is applied there.
	 *
	 * @return false when nothing chargeable was being chanted
	 */
	public static boolean release(ServerPlayer player) {
		if (player == null)
			return false;
		String active = player.getPersistentData().getString("chanting");
		if (active == null || active.isEmpty())
			return false;
		for (Chantable ability : CHANTABLE.values()) {
			if (!ability.chant().equals(active))
				continue;
			ability.key().release(player);
			player.getPersistentData().putInt(HOLD_TICKS, 0);
			player.getPersistentData().putString(HOLD_STATE, "");
			forgetRecital(player);
			return true;
		}
		return false;
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
		if (!owns(normalise(variables.sorcerer), key))
			return false;
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
