package net.efkrdnz.jjkvoice.client;

import java.util.List;
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
import net.efkrdnz.jjkvoice.compat.JjkBridge;
import net.efkrdnz.jjkvoice.network.VoiceCastPayload;
import net.efkrdnz.jjkvoice.network.VoiceChantPayload;
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

		// Chanting is measured only against the ability already selected. That is
		// what lets one phrase do both jobs: say an ability's name to draw it, say
		// it again to charge it. Without the "already active" test the two would be
		// indistinguishable.
		String moveset = JjkBridge.currentMoveset(minecraft.player);
		List<String> chantPhrases = JjkBridge.isChantable(moveset)
				? VoiceConfig.get().chantPhrasesFor(moveset)
				: List.of();

		Util.backgroundExecutor().execute(() -> {
			PhraseRecognizer.ChantResult chant = chantPhrases.isEmpty()
					? null
					: PhraseRecognizer.recogniseChant(audio, chantPhrases);
			PhraseRecognizer.Result result = PhraseRecognizer.recognise(audio);
			minecraft.execute(() -> {
				try {
					if (preferChant(chant, result))
						applyChant(minecraft, chant);
					else
						applyResult(minecraft, result);
				} finally {
					PROCESSING.set(false);
				}
			});
		});
	}

	/**
	 * Decides between charging the active ability and doing something else.
	 *
	 * <p>The chant band is deliberately loose, which on its own would let a near
	 * miss swallow a real command -- switch to Red while Purple is up, and "reversal
	 * red" could land inside Purple's near band and charge Purple instead. So a
	 * near chant yields to any confident match, and only an exact chant outranks
	 * one. Two exact readings is the genuinely ambiguous case, and there the closer
	 * distance wins.
	 */
	private static boolean preferChant(PhraseRecognizer.ChantResult chant, PhraseRecognizer.Result result) {
		if (chant == null || !chant.charged())
			return false;
		if (result == null || !result.matched())
			return true;
		if (chant.quality() != PhraseRecognizer.ChantQuality.EXACT)
			return false;
		return chant.distance() <= result.distance();
	}

	/**
	 * Turns a recognised chant into ticks of hold.
	 *
	 * <p>The charge is the time actually spoken, so a longer incantation charges
	 * more -- which is the same relationship holding the key already has. A near
	 * match is credited at a fraction of it, so being slightly off still progresses
	 * but saying it cleanly is worth more.
	 */
	private static void applyChant(Minecraft minecraft, PhraseRecognizer.ChantResult chant) {
		if (minecraft.player == null || chant == null || !chant.charged())
			return;
		VoiceConfig config = VoiceConfig.get();

		double credit = chant.quality() == PhraseRecognizer.ChantQuality.EXACT ? 1.0D : config.nearChantCredit;
		int ticks = (int) Math.round(chant.seconds() * 20.0D * credit);
		ticks = Math.min(Math.max(ticks, 1), config.maxChantTicks);

		PacketDistributor.sendToServer(new VoiceChantPayload(ticks));
		if (config.announceMatches)
			actionBar(minecraft, Component.translatable(
					chant.quality() == PhraseRecognizer.ChantQuality.EXACT
							? "message.jjkvoice.chant.exact"
							: "message.jjkvoice.chant.near",
					chant.phrase(), String.format("%.1f", ticks / 20.0D))
					.withStyle(chant.quality() == PhraseRecognizer.ChantQuality.EXACT
							? ChatFormatting.LIGHT_PURPLE
							: ChatFormatting.GRAY));
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
