package net.efkrdnz.jjkstrongest.entity;

import net.minecraft.world.level.Level;

import net.efkrdnz.jjkstrongest.JjkStrongestMod;
import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.procedures.Technique1OnKeyPressProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique1OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique3OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique3OnKeypressProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique4OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique4OnKeyReleasedProcedure;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Every ability a bot can be told to use, and how to actually make it happen.
 *
 * <p>Deliberately routed through the same {@code Technique*} procedures a key press goes
 * to, rather than calling the underlying ability procedures directly the way the mod's
 * NPC AI does. Those procedures <em>are</em> the dispatch — the sorcerer/moveset ladder,
 * the charge gates, the cooldown writes, the arm animations — and reimplementing that for
 * a bot would mean the bot testing a different code path from the one players use, which
 * is the one thing a debug tool must never do. Every one of them already takes
 * {@code Entity}; only the transport layer above them ever wanted a {@code Player}.
 *
 * <p>The naming in those procedures is a trap and is worth stating once: {@code OnKeyPress}
 * is the press and {@code OnKeyPressed} is the <em>release</em> — except on key 4, where
 * {@code OnKeyPressed} is the press and {@code OnKeyReleased} is the release.
 */
public final class DebugBotAbilities {

	/**
	 * @param moveset    what {@code current_moveset} has to be for the dispatch to reach it
	 * @param key        which technique key carries it
	 * @param holdTicks  ticks to hold between press and release; 0 fires on the press alone.
	 *                   For charged techniques this is the top tier plus a little, so
	 *                   {@code use} gives a fully wound-up cast in one command.
	 */
	public record Ability(String moveset, int key, int holdTicks, String description) {
	}

	private static final Map<String, Ability> ABILITIES = build();

	private static Map<String, Ability> build() {
		Map<String, Ability> out = new LinkedHashMap<>();
		// Gojo
		out.put("domain", new Ability("gojo_limitless", 4, 2, "Unlimited Void"));
		out.put("blue", new Ability("gojo_blue", 3, 34, "Lapse Blue, fully charged"));
		out.put("red", new Ability("gojo_red", 3, 44, "Reversal Red, fully charged"));
		out.put("purple", new Ability("gojo_purple", 3, 134, "Hollow Purple, fully charged"));
		out.put("teleport", new Ability("gojo_limitless", 3, 1, "Teleport blink"));
		out.put("infinity", new Ability("gojo_limitless", 1, 0, "Toggle Infinity"));
		// Sukuna
		out.put("shrine", new Ability("sukuna_shrine", 4, 2, "Malevolent Shrine"));
		out.put("dismantle", new Ability("sukuna_dismantle", 1, 34, "Dismantle, fully charged"));
		out.put("cleave", new Ability("sukuna_cleave", 1, 20, "Cleave"));
		out.put("fuga", new Ability("sukuna_fuga", 3, 6, "Fuga / flame arrow"));
		// Shared
		out.put("melee", new Ability("gojo_melee", 1, 0, "Melee jab"));
		return Collections.unmodifiableMap(out);
	}

	private DebugBotAbilities() {
	}

	public static java.util.Set<String> names() {
		return ABILITIES.keySet();
	}

	public static Ability get(String name) {
		return name == null ? null : ABILITIES.get(name.toLowerCase(Locale.ROOT));
	}

	/**
	 * Points the bot's variables at an ability and runs the press, then queues the release.
	 *
	 * @return false if the name is not one we know
	 */
	public static boolean use(DebugBotEntity bot, String name) {
		Ability ability = get(name);
		if (ability == null)
			return false;
		String moveset = ability.moveset();
		// "melee" is per-character, and the only difference is the prefix.
		if (moveset.endsWith("_melee") && !bot.getCharacter().isEmpty())
			moveset = bot.getCharacter() + "_melee";
		select(bot, moveset);
		press(bot, ability.key());
		if (ability.holdTicks() <= 0) {
			release(bot, ability.key());
			return true;
		}
		JjkStrongestMod.queueServerWork(ability.holdTicks(), () -> {
			if (bot.isAlive())
				release(bot, ability.key());
		});
		return true;
	}

	/** Starts a charge and leaves it running, so it can be watched or released by hand. */
	public static boolean beginChant(DebugBotEntity bot, String name) {
		Ability ability = get(name);
		if (ability == null)
			return false;
		select(bot, ability.moveset());
		press(bot, ability.key());
		return true;
	}

	/** Lets go of whatever is being held. Harmless if nothing is. */
	public static void releaseHeld(DebugBotEntity bot) {
		Ability ability = get(currentAbilityName(bot));
		release(bot, ability == null ? 3 : ability.key());
	}

	private static String currentAbilityName(DebugBotEntity bot) {
		String moveset = bot.getData(JjkStrongestModVariables.PLAYER_VARIABLES).current_moveset;
		for (Map.Entry<String, Ability> entry : ABILITIES.entrySet())
			if (entry.getValue().moveset().equals(moveset))
				return entry.getKey();
		return null;
	}

	public static void select(DebugBotEntity bot, String moveset) {
		JjkStrongestModVariables.PlayerVariables variables = bot.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
		variables.current_moveset = moveset;
		variables.syncPlayerVariables(bot);
	}

	/**
	 * Gives the bot a character and the resources its techniques check for.
	 *
	 * <p>The charge top-up is not a cheat, it is the difference between working and
	 * silently doing nothing: Blue and Red refuse to even start a chant below one charge
	 * and Purple below three, and the refusal is silent, so a bot without them looks
	 * broken rather than out of resources.
	 */
	public static void become(DebugBotEntity bot, String character) {
		bot.setCharacter(character);
		JjkStrongestModVariables.PlayerVariables variables = bot.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
		variables.sorcerer = character;
		variables.charge_blue = 3;
		variables.charge_red = 3;
		variables.charge_purple = 3;
		// World Cutting Slash checks for a completed three-line chant, and its other route
		// is a creative-mode test that returns false for anything that is not a player.
		variables.wcs_chant_progress = 3;
		variables.syncPlayerVariables(bot);
	}

	private static void press(DebugBotEntity bot, int key) {
		switch (key) {
			// OnKeyPress is the press on keys 1 and 3; on key 4 the press is OnKeyPressed.
			case 1 -> Technique1OnKeyPressProcedure.execute(bot);
			case 3 -> Technique3OnKeypressProcedure.execute(bot);
			case 4 -> Technique4OnKeyPressedProcedure.execute(bot);
			default -> {
			}
		}
	}

	private static void release(DebugBotEntity bot, int key) {
		Level level = bot.level();
		if (level == null)
			return;
		double x = bot.getX();
		double y = bot.getY();
		double z = bot.getZ();
		switch (key) {
			case 1 -> Technique1OnKeyPressedProcedure.execute(bot);
			case 3 -> Technique3OnKeyPressedProcedure.execute(level, x, y, z, bot);
			case 4 -> Technique4OnKeyReleasedProcedure.execute(level, x, y, z, bot);
			default -> {
			}
		}
	}
}
