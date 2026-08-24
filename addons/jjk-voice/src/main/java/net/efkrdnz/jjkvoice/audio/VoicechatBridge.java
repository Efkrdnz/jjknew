package net.efkrdnz.jjkvoice.audio;

import java.util.concurrent.atomic.AtomicBoolean;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

import net.efkrdnz.jjkvoice.JjkVoiceMod;

/**
 * Simple Voice Chat addon entry point. The microphone feed enters the mod here.
 *
 * <p>Audio is only intercepted while the player is holding the Voice Command key.
 * At every other moment the event passes through untouched, so ordinary proximity
 * chat is unaffected and other voice addons -- which follow the same
 * cancel-only-while-armed rule -- can coexist on the same microphone.
 */
@ForgeVoicechatPlugin
public final class VoicechatBridge implements VoicechatPlugin {
	private static final AtomicBoolean CLIENT_READY = new AtomicBoolean();

	@Override
	public String getPluginId() {
		return JjkVoiceMod.MOD_ID;
	}

	@Override
	public void initialize(VoicechatApi api) {
		JjkVoiceMod.LOGGER.info("Voice Commands attached to Simple Voice Chat");
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(ClientVoicechatInitializationEvent.class, this::onClientInitialization);
		registration.registerEvent(ClientSoundEvent.class, this::onClientSound);
	}

	/** True once Simple Voice Chat's client side has handed us its API. */
	public static boolean isClientReady() {
		return CLIENT_READY.get();
	}

	private void onClientInitialization(ClientVoicechatInitializationEvent event) {
		VoicechatClientApi api = event.getVoicechat();
		if (api != null && CLIENT_READY.compareAndSet(false, true))
			JjkVoiceMod.LOGGER.info("Voice Commands microphone bridge ready");
	}

	private void onClientSound(ClientSoundEvent event) {
		CLIENT_READY.set(true);
		if (!MicrophoneCapture.isArmed())
			return;

		// Cancelling here keeps the incantation private: Simple Voice Chat has not
		// yet encoded or transmitted this audio, so nearby players never hear it.
		event.cancel();
		MicrophoneCapture.accept(event.getRawAudio());
	}
}
