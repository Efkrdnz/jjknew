package net.efkrdnz.jjkvoice.network;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.efkrdnz.jjkvoice.JjkVoiceMod;

/**
 * "This is how far through an incantation you are."
 *
 * <p>The only thing the overlay cannot work out for itself. What lines exist and
 * which abilities the player has are both known on the client, but how far a
 * recital has got is not: a line the client believed landed may have been
 * refused, because the ability is not the speaker's or the press was gated on
 * charges they do not have. A client keeping its own count would draw a recital
 * the server is not running.
 *
 * @param candidates abilities the recital could still be for; empty when none is
 *                   running, which is what returns the overlay to its resting list
 * @param recited    lines taken so far
 * @param tier       output actually reached, once one ability has won
 */
public record ChantStatePayload(List<String> candidates, int recited, int tier) implements CustomPacketPayload {
	/** No incantation is longer than this, and nothing shares a line with more. */
	public static final int MAX_CANDIDATES = 8;
	public static final int MAX_KEY_LENGTH = 48;

	public static final ChantStatePayload NONE = new ChantStatePayload(List.of(), 0, 0);

	public static final Type<ChantStatePayload> TYPE = new Type<>(JjkVoiceMod.id("chant_state"));

	public static final StreamCodec<ByteBuf, ChantStatePayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.stringUtf8(MAX_KEY_LENGTH).apply(ByteBufCodecs.list(MAX_CANDIDATES)),
			ChantStatePayload::candidates,
			ByteBufCodecs.VAR_INT, ChantStatePayload::recited,
			ByteBufCodecs.VAR_INT, ChantStatePayload::tier,
			ChantStatePayload::new);

	public ChantStatePayload {
		candidates = List.copyOf(candidates);
	}

	public boolean running() {
		return !candidates.isEmpty();
	}

	/** Whether two states would draw the same thing, so unchanged ticks send nothing. */
	public boolean matches(ChantStatePayload other) {
		return other != null && recited == other.recited && tier == other.tier
				&& new ArrayList<>(candidates).equals(new ArrayList<>(other.candidates));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
