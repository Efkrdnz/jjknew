package net.efkrdnz.jjkstrongest.api;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

import net.efkrdnz.jjkstrongest.procedures.VCTexeProcedure;

/**
 * The public entry point for driving a technique by name.
 *
 * <p>This exists so companion mods have one seam to hold onto instead of calling
 * into {@code procedures}. It is deliberately thin: {@link VCTexeProcedure}
 * already checks which sorcerer the player is and dispatches accordingly, and
 * each technique applies its own costs and cooldowns. Re-checking any of that
 * here would create a second source of truth that could disagree.
 *
 * <p>Replaces the previous arrangement, where an external program wrote a word
 * into {@code Documents/JJKVoiceCommands/command.txt} and the mod polled that
 * file every five ticks on the server thread.
 */
public final class JjkVoiceApi {
	/**
	 * Every command key a caller may pass to {@link #cast}.
	 *
	 * <p>These are the canonical spellings. {@link VCTexeProcedure} matches with
	 * {@code contains}, so its own aliases ("explode" for burst, "come_here" for
	 * pull, and so on) still work if something passes them — but the aliases are
	 * not listed here, because a few of them overlap. {@code be_crushed} contains
	 * {@code crush} and so reaches the crush branch first; sticking to canonical
	 * keys avoids depending on that ordering.
	 */
	private static final Set<String> COMMAND_KEYS = Set.of(
			// Gojo and Sukuna
			"domain_expansion",
			// Sukuna
			"dismantle", "fuga",
			// Inumaki - Cursed Speech
			"dont_move", "die", "blast", "crush", "burst", "sleep", "flee", "rot",
			"twist", "burn", "fall", "spit", "pull", "shrink", "weep", "kneel");

	private JjkVoiceApi() {
	}

	/** Command keys this mod understands, for building an allow-list against. */
	public static Set<String> commandKeys() {
		return COMMAND_KEYS;
	}

	public static boolean isCommandKey(String key) {
		return key != null && COMMAND_KEYS.contains(normalise(key));
	}

	public static String normalise(String key) {
		return key == null ? "" : key.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
	}

	/**
	 * Runs the technique bound to {@code key} for {@code player}.
	 *
	 * <p>Server side only. Whether anything actually happens is up to the mod: a
	 * Gojo player saying "dismantle" is simply ignored, exactly as it is when the
	 * key arrives from any other source.
	 *
	 * @return false when the player or key was unusable; true once the request was
	 *         handed to the dispatcher, which is not a promise that a technique fired
	 */
	public static boolean cast(ServerPlayer player, String key) {
		if (player == null)
			return false;
		String command = normalise(key);
		if (!COMMAND_KEYS.contains(command))
			return false;
		VCTexeProcedure.execute(player.level(), player.getX(), player.getY(), player.getZ(), player, command);
		return true;
	}

	/** Convenience for callers that want a stable order, e.g. for a config file. */
	public static List<String> sortedCommandKeys() {
		return COMMAND_KEYS.stream().sorted().toList();
	}
}
