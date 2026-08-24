package net.efkrdnz.jjkvoice.compat;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.efkrdnz.jjkstrongest.api.JjkVoiceApi;
import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

/**
 * The only class in this addon that touches JJK Strongest.
 *
 * <p>Kept to a single seam on purpose. The host mod exposes {@code JjkVoiceApi},
 * so this is a straight delegation; if that API changes shape, this file is the
 * only thing that changes.
 *
 * <p>No validation happens here. The host mod already checks which sorcerer the
 * player is and each technique applies its own costs and cooldowns. Re-implementing
 * any of that would mean two sources of truth that could disagree.
 */
public final class JjkBridge {
	private JjkBridge() {
	}

	/** Command keys the host mod understands. The server allow-lists against this. */
	public static Set<String> commandKeys() {
		return JjkVoiceApi.commandKeys();
	}

	/**
	 * Runs a command key, which the host mod resolves to either an immediate
	 * technique or an ability selection.
	 *
	 * @return true when the host mod accepted the request
	 */
	public static boolean run(ServerPlayer player, String commandKey) {
		return JjkVoiceApi.run(player, commandKey);
	}

	/** True when this key selects an ability rather than firing one. */
	public static boolean isSelection(String commandKey) {
		return JjkVoiceApi.movesetKeys().contains(JjkVoiceApi.normalise(commandKey));
	}

	/** True when this ability charges by holding, and so can be chanted. */
	public static boolean isChantable(String movesetKey) {
		return JjkVoiceApi.chantableMovesets().contains(JjkVoiceApi.normalise(movesetKey));
	}

	/**
	 * The ability the player currently has active.
	 *
	 * <p>Readable on the client because the host mod syncs its player variables,
	 * which is what lets the client tell "select this" from "chant it" without
	 * asking the server first.
	 */
	public static String currentMoveset(Player player) {
		if (player == null)
			return "";
		String moveset = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES).current_moveset;
		return moveset == null ? "" : JjkVoiceApi.normalise(moveset);
	}

	/** Grants ticks of hold, as if the technique key had been held that long. */
	public static void chant(ServerPlayer player, int holdTicks) {
		JjkVoiceApi.chant(player, holdTicks);
	}
}
