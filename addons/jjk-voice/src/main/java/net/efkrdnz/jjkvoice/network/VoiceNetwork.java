package net.efkrdnz.jjkvoice.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.efkrdnz.jjkvoice.server.VoiceServerHandler;

/** Registers the single client-to-server request this addon needs. */
public final class VoiceNetwork {
	private static final String PROTOCOL_VERSION = "1";

	private VoiceNetwork() {
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
		registrar.playToServer(VoiceCastPayload.TYPE, VoiceCastPayload.STREAM_CODEC,
				VoiceServerHandler::handleCast);
	}
}
