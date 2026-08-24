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
 * <p>No gameplay decision happens here. The host mod decides what a spoken name
 * means, whether the speaker's technique includes it, and what each technique
 * costs. Re-implementing any of that would mean two sources of truth that could
 * disagree, and the copy here would be the one that went stale.
 */
public final class JjkBridge {
	private JjkBridge() {
	}

	/** Command keys the host mod understands. The server allow-lists against this. */
	public static Set<String> commandKeys() {
		return JjkVoiceApi.commandKeys();
	}

	/**
	 * Acts on a spoken name: select, charge, release or cast, as the host mod
	 * decides from state only it can see.
	 *
	 * @return what actually happened, for telling the speaker
	 */
	public static JjkVoiceApi.Spoken speak(ServerPlayer player, String commandKey, boolean exact, int line, int lines) {
		return JjkVoiceApi.speak(player, commandKey, exact, line, lines);
	}

	/** True when this key selects an ability rather than firing one. */
	public static boolean isSelection(String commandKey) {
		return JjkVoiceApi.movesetKeys().contains(JjkVoiceApi.normalise(commandKey));
	}

	/** True when this ability charges by holding, and so can be chanted. */
	public static boolean isChantable(String movesetKey) {
		return JjkVoiceApi.chantableMovesets().contains(JjkVoiceApi.normalise(movesetKey));
	}

	/** Every ability that can take an incantation, whoever the speaker is. */
	public static Set<String> chantableMovesets() {
		return JjkVoiceApi.chantableMovesets();
	}

	/**
	 * The player's technique, readable on the client because the host mod syncs
	 * its player variables.
	 */
	public static String sorcerer(Player player) {
		if (player == null)
			return "";
		String sorcerer = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES).sorcerer;
		return sorcerer == null ? "" : JjkVoiceApi.normalise(sorcerer);
	}

	/** The ability the player currently has active. */
	public static String currentMoveset(Player player) {
		if (player == null)
			return "";
		String moveset = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES).current_moveset;
		return moveset == null ? "" : JjkVoiceApi.normalise(moveset);
	}

	/**
	 * What this player is entitled to say.
	 *
	 * <p>Used to narrow what the recogniser will even compare against, so another
	 * sorcerer's techniques are not merely refused but never heard. The server
	 * checks the same thing again; this only keeps a Gojo player's "dismantle"
	 * from being the closest match to something they did say.
	 */
	public static Set<String> allowedKeys(Player player) {
		return JjkVoiceApi.commandKeysFor(sorcerer(player));
	}

	/** Whether this player's technique includes an ability. */
	public static boolean owns(Player player, String movesetKey) {
		return JjkVoiceApi.owns(sorcerer(player), movesetKey);
	}
}
