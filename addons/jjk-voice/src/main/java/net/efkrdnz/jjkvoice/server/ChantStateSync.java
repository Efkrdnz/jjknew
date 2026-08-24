package net.efkrdnz.jjkvoice.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.efkrdnz.jjkstrongest.api.JjkVoiceApi;

import net.efkrdnz.jjkvoice.JjkVoiceMod;
import net.efkrdnz.jjkvoice.compat.JjkBridge;
import net.efkrdnz.jjkvoice.network.ChantStatePayload;

/**
 * Tells the client how far through an incantation the player is, whenever that
 * changes.
 *
 * <p>Polled rather than pushed from where the recital is edited, because it is
 * edited in the host mod, which must not know this addon exists. Reading three
 * fields per player per tick is cheaper than that coupling would be, and it
 * catches every path without having to list them -- a line landing, a recital
 * lapsing on its timer, a charge spent by firing.
 *
 * <p>Only differences are sent. A player standing still with nothing being
 * recited costs one comparison a tick and no traffic at all.
 */
@EventBusSubscriber(modid = JjkVoiceMod.MOD_ID)
public final class ChantStateSync {
	private static final Map<UUID, ChantStatePayload> LAST_SENT = new ConcurrentHashMap<>();

	private ChantStateSync() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		JjkVoiceApi.Recital recital = JjkBridge.recital(player);
		ChantStatePayload state = new ChantStatePayload(recital.candidates(), recital.recited(), recital.tier());
		if (state.matches(LAST_SENT.get(player.getUUID())))
			return;

		LAST_SENT.put(player.getUUID(), state);
		PacketDistributor.sendToPlayer(player, state);
	}

	@SubscribeEvent
	public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		LAST_SENT.remove(event.getEntity().getUUID());
		VoiceServerHandler.forget(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		// Forgotten rather than resent: a fresh client already draws the resting list,
		// and the next tick corrects it if a recital somehow outlived the session.
		LAST_SENT.remove(event.getEntity().getUUID());
	}
}
