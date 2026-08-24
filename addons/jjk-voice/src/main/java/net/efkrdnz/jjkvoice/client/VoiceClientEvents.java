package net.efkrdnz.jjkvoice.client;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.efkrdnz.jjkvoice.JjkVoiceMod;
import net.efkrdnz.jjkvoice.audio.MicrophoneCapture;
import net.efkrdnz.jjkvoice.audio.VoicechatBridge;
import net.efkrdnz.jjkvoice.config.VoiceConfig;
import net.efkrdnz.jjkvoice.network.VoiceCastPayload;
import net.efkrdnz.jjkvoice.recognize.PhraseRecognizer;

/**
 * Push-to-talk lifecycle and the hand-off from a finished clip to a cast request.
 *
 * <p>Feature extraction and matching run on a background thread. A one-second
 * clip is a few hundred FFTs plus a dynamic-programming pass per enrolled
 * template, which is not much on its own but scales with how many techniques the
 * player has taught -- and none of it belongs in a frame.
 */
@EventBusSubscriber(modid = JjkVoiceMod.MOD_ID, value = Dist.CLIENT)
public final class VoiceClientEvents {
	/** Ticks between action-bar refreshes while the key is held. */
	private static final int HUD_INTERVAL_TICKS = 10;

	private static final AtomicBoolean PROCESSING = new AtomicBoolean();

	private static boolean captureArmed;
	private static int hudTicks;
	private static boolean warnedAboutVoicechat;

	private VoiceClientEvents() {
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			resetCapture();
			return;
		}

		boolean keyDown = minecraft.screen == null && VoiceKeyMappings.VOICE_COMMAND.isDown();

		if (keyDown && !captureArmed) {
			if (!VoicechatBridge.isClientReady()) {
				warnAboutVoicechatOnce(minecraft);
			} else {
				MicrophoneCapture.arm();
				captureArmed = true;
				actionBar(minecraft, statusKey());
			}
		} else if (!keyDown && captureArmed) {
			boolean receivedAudio = MicrophoneCapture.isReceiving();
			MicrophoneCapture.disarm();
			captureArmed = false;
			if (!receivedAudio)
				actionBar(minecraft, "message.jjkvoice.no_audio", ChatFormatting.RED);
		}

		if (captureArmed && ++hudTicks >= HUD_INTERVAL_TICKS) {
			hudTicks = 0;
			actionBar(minecraft, MicrophoneCapture.isReceiving() ? "message.jjkvoice.listening" : statusKey());
		} else if (!captureArmed) {
			hudTicks = 0;
		}

		MicrophoneCapture.CapturedAudio audio = MicrophoneCapture.pollCompleted();
		if (audio != null)
			process(minecraft, audio);
	}

	private static void resetCapture() {
		if (captureArmed) {
			MicrophoneCapture.disarm();
			captureArmed = false;
		}
		hudTicks = 0;
	}

	private static void process(Minecraft minecraft, MicrophoneCapture.CapturedAudio audio) {
		// One clip at a time. A second capture arriving mid-analysis is dropped
		// rather than queued, because a stale incantation firing later is worse.
		if (!PROCESSING.compareAndSet(false, true))
			return;

		EnrollmentSession session = EnrollmentSession.active();
		if (session != null) {
			Util.backgroundExecutor().execute(() -> {
				float[][] frames = PhraseRecognizer.features(audio);
				minecraft.execute(() -> {
					try {
						// The session may have been cancelled while we were working.
						if (EnrollmentSession.active() == session)
							session.accept(frames);
					} finally {
						PROCESSING.set(false);
					}
				});
			});
			return;
		}

		Util.backgroundExecutor().execute(() -> {
			PhraseRecognizer.Result result = PhraseRecognizer.recognise(audio);
			minecraft.execute(() -> {
				try {
					applyResult(minecraft, result);
				} finally {
					PROCESSING.set(false);
				}
			});
		});
	}

	private static void applyResult(Minecraft minecraft, PhraseRecognizer.Result result) {
		if (minecraft.player == null)
			return;
		VoiceConfig config = VoiceConfig.get();

		switch (result.outcome()) {
			case MATCHED -> {
				if (result.commandKey().isEmpty())
					return;
				// The server re-checks this key against the host mod's own command
				// set, so a wrong guess here costs nothing but a dropped packet.
				PacketDistributor.sendToServer(new VoiceCastPayload(result.commandKey()));
				if (config.announceMatches)
					actionBar(minecraft, Component.translatable("message.jjkvoice.matched",
							result.phrase(), result.commandKey(), String.format("%.2f", result.distance()))
							.withStyle(ChatFormatting.LIGHT_PURPLE));
			}
			case NOT_ENROLLED -> chat(minecraft, Component.translatable("message.jjkvoice.not_enrolled")
					.withStyle(ChatFormatting.YELLOW));
			case NO_MATCH -> {
				if (config.announceMatches)
					actionBar(minecraft, Component.translatable("message.jjkvoice.no_match",
							String.format("%.2f", result.distance()),
							String.format("%.2f", result.threshold()))
							.withStyle(ChatFormatting.GRAY));
			}
			case TOO_SHORT -> actionBar(minecraft, "message.jjkvoice.too_short", ChatFormatting.GRAY);
			case TOO_LONG -> actionBar(minecraft, "message.jjkvoice.too_long", ChatFormatting.GRAY);
			case TOO_QUIET -> actionBar(minecraft, "message.jjkvoice.too_quiet", ChatFormatting.GRAY);
		}
	}

	private static String statusKey() {
		EnrollmentSession session = EnrollmentSession.active();
		return session == null ? "message.jjkvoice.armed" : "message.jjkvoice.enroll.recording";
	}

	private static void warnAboutVoicechatOnce(Minecraft minecraft) {
		if (warnedAboutVoicechat)
			return;
		warnedAboutVoicechat = true;
		chat(minecraft, Component.translatable("message.jjkvoice.voicechat_not_ready")
				.withStyle(ChatFormatting.RED));
	}

	private static void actionBar(Minecraft minecraft, String key) {
		actionBar(minecraft, key, ChatFormatting.AQUA);
	}

	private static void actionBar(Minecraft minecraft, String key, ChatFormatting colour) {
		actionBar(minecraft, Component.translatable(key).withStyle(colour));
	}

	private static void actionBar(Minecraft minecraft, Component message) {
		if (minecraft.player != null)
			minecraft.player.displayClientMessage(message, true);
	}

	private static void chat(Minecraft minecraft, Component message) {
		if (minecraft.player != null)
			minecraft.player.displayClientMessage(message, false);
	}
}
