package net.efkrdnz.jjkvoice.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.efkrdnz.jjkvoice.JjkVoiceMod;

/**
 * "The player chanted for this long."
 *
 * <p>Carries ticks of hold rather than a phrase or audio, because that is the
 * only thing the server needs: charging is expressed as time spent holding the
 * technique key, and a spoken chant simply supplies that time.
 *
 * <p>The client decides <em>whether</em> the chant matched, since that is where
 * the microphone and the voiceprints are. The server cannot verify it, so it
 * treats the number as a request, clamps it, and throttles the sender -- the same
 * stance it takes towards a keypress arriving over the network.
 */
public record VoiceChantPayload(int holdTicks) implements CustomPacketPayload {
	public static final Type<VoiceChantPayload> TYPE = new Type<>(JjkVoiceMod.id("voice_chant"));

	public static final StreamCodec<ByteBuf, VoiceChantPayload> STREAM_CODEC =
			ByteBufCodecs.VAR_INT.map(VoiceChantPayload::new, VoiceChantPayload::holdTicks);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
