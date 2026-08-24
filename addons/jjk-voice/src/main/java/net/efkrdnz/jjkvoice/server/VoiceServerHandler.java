package net.efkrdnz.jjkvoice.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.efkrdnz.jjkvoice.compat.JjkBridge;
import net.efkrdnz.jjkvoice.network.VoiceCastPayload;

/**
 * Server side of the voice request.
 *
 * <p>Recognition happens on the client because that is where the microphone is,
 * which means the client is asserting something the server cannot verify. So the
 * server treats the packet as nothing more than a keypress that arrived over the
 * network: allow-list the key, throttle the sender, then hand off to the host
 * mod, which applies every real gameplay check itself.
 *
 * <p>The allow-list is the host mod's own command set rather than a copy kept
 * here. A hardcoded duplicate would drift the first time a technique is added or
 * renamed, and drift in an allow-list fails in the dangerous direction.
 */
public final class VoiceServerHandler {
	/**
	 * Floor on the gap between two accepted requests from one player. Each
	 * technique has its own cost and cooldown, but those live behind sorcerer and
	 * resource checks -- this stops a modified client from burning server time on
	 * the checks themselves.
	 */
	private static final long MINIMUM_INTERVAL_MILLIS = 250L;

	private static final Map<UUID, Long> LAST_REQUEST = new ConcurrentHashMap<>();

	private VoiceServerHandler() {
	}

	public static void handleSpeak(VoiceCastPayload payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player))
				return;

			List<String> keys = new ArrayList<>();
			for (String raw : payload.commandKeys()) {
				String key = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
				if (JjkBridge.commandKeys().contains(key) && !keys.contains(key))
					keys.add(key);
			}
			if (keys.isEmpty())
				return;
			if (isThrottled(player.getUUID()))
				return;

			// The host mod decides what these mean for this player, which of them
			// their technique includes, and which the recital rules out.
			JjkBridge.speak(player, keys, payload.exact(), payload.line(), payload.lines());
		});
	}

	private static boolean isThrottled(UUID playerId) {
		long now = System.currentTimeMillis();
		Long previous = LAST_REQUEST.put(playerId, now);
		if (previous == null)
			return false;
		if (now - previous >= MINIMUM_INTERVAL_MILLIS)
			return false;
		// Keep the earlier stamp so a burst cannot walk the window forward.
		LAST_REQUEST.put(playerId, previous);
		return true;
	}

	public static void forget(UUID playerId) {
		LAST_REQUEST.remove(playerId);
	}
}
