package net.efkrdnz.jjkvoice.client.hud;

import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.efkrdnz.jjkvoice.network.ChantStatePayload;

/**
 * The last thing the server said about the player's recital.
 *
 * <p>The single writer for recital state on the client. The overlay reads it and
 * draws exactly that; it never advances anything itself, because a line it
 * believed landed may have been refused at the other end and a locally-kept count
 * would quietly disagree with the technique the player is actually holding.
 */
@OnlyIn(Dist.CLIENT)
public final class ChantHudState {
	private static volatile ChantStatePayload current = ChantStatePayload.NONE;

	private ChantHudState() {
	}

	public static void accept(ChantStatePayload payload, IPayloadContext context) {
		context.enqueueWork(() -> current = payload == null ? ChantStatePayload.NONE : payload);
	}

	/** Abilities the running recital could still be for; empty when none is. */
	public static List<String> candidates() {
		return current.candidates();
	}

	public static int recited() {
		return current.recited();
	}

	public static int tier() {
		return current.tier();
	}

	public static boolean running() {
		return current.running();
	}

	/** Called on disconnect, so a recital cannot outlive the world it was in. */
	public static void clear() {
		current = ChantStatePayload.NONE;
	}
}
