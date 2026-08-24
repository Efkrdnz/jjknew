package net.efkrdnz.jjkvoice.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.efkrdnz.jjkvoice.JjkVoiceMod;

/**
 * "The player spoke this."
 *
 * <p>Deliberately carries a short command key rather than a transcript or audio.
 * The server treats it as a request against a fixed allow-list, never as an
 * instruction -- a modified client can send any key it likes and still only
 * reaches commands the server already agreed to expose, for a sorcerer it
 * actually is.
 *
 * <p>What the key <em>means</em> is not carried, because the client cannot know:
 * the same ability name selects, charges or releases depending on server state.
 * Only how it was heard travels.
 *
 * @param exact heard cleanly, rather than close enough to be worth half
 * @param line  which line of the ability's incantation this was, or -1 when the
 *              ability's own name was spoken instead
 * @param lines how many lines that incantation has, so the server can tell when
 *              one has been recited to the end
 */
public record VoiceCastPayload(String commandKey, boolean exact, int line, int lines) implements CustomPacketPayload {
	/** Long enough for a command key, short enough that spam costs the sender more than us. */
	public static final int MAX_KEY_LENGTH = 32;

	public static final Type<VoiceCastPayload> TYPE = new Type<>(JjkVoiceMod.id("voice_cast"));

	public static final StreamCodec<ByteBuf, VoiceCastPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.stringUtf8(MAX_KEY_LENGTH), VoiceCastPayload::commandKey,
			ByteBufCodecs.BOOL, VoiceCastPayload::exact,
			ByteBufCodecs.VAR_INT, VoiceCastPayload::line,
			ByteBufCodecs.VAR_INT, VoiceCastPayload::lines,
			VoiceCastPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
