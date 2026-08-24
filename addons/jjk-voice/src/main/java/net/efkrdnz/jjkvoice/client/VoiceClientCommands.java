package net.efkrdnz.jjkvoice.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import net.efkrdnz.jjkvoice.JjkVoiceMod;
import net.efkrdnz.jjkvoice.audio.VoicechatBridge;
import net.efkrdnz.jjkvoice.compat.JjkBridge;
import net.efkrdnz.jjkvoice.config.VoiceConfig;
import net.efkrdnz.jjkvoice.recognize.VoicePrintStore;

/**
 * Client-only commands for enrollment and tuning.
 *
 * <p>Client-side on purpose: voiceprints, thresholds, and the phrase list are all
 * local player settings. A server never sees them and has no say in them.
 *
 * <p>Enrolling a single technique is its own subcommand rather than an
 * afterthought. With nineteen techniques configured, {@code enroll} on its own is
 * a long sit, and most players only ever speak two or three of them.
 */
@EventBusSubscriber(modid = JjkVoiceMod.MOD_ID, value = Dist.CLIENT)
public final class VoiceClientCommands {
	/** Suggests the host mod's command keys, so nobody has to memorise them. */
	private static final SuggestionProvider<CommandSourceStack> COMMAND_KEYS =
			(context, builder) -> SharedSuggestionProvider.suggest(
					JjkBridge.commandKeys().stream().sorted().toList(), builder);

	private VoiceClientCommands() {
	}

	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("jjkvoice")
				.then(Commands.literal("enroll")
						.executes(context -> enrollAll(context.getSource()))
						.then(Commands.argument("command", StringArgumentType.word())
								.suggests(COMMAND_KEYS)
								.executes(context -> enrollCommand(context.getSource(),
										StringArgumentType.getString(context, "command")))))
				.then(Commands.literal("add")
						.then(Commands.argument("command", StringArgumentType.word())
								.suggests(COMMAND_KEYS)
								.then(Commands.argument("phrase", StringArgumentType.greedyString())
										.executes(context -> addPhrase(context.getSource(),
												StringArgumentType.getString(context, "command"),
												StringArgumentType.getString(context, "phrase"))))))
				.then(Commands.literal("cancel")
						.executes(context -> cancel(context.getSource())))
				.then(Commands.literal("status")
						.executes(context -> status(context.getSource())))
				.then(Commands.literal("forget")
						.then(Commands.literal("all")
								.executes(context -> forgetAll(context.getSource())))
						.then(Commands.argument("phrase", StringArgumentType.greedyString())
								.executes(context -> forget(context.getSource(),
										StringArgumentType.getString(context, "phrase")))))
				.then(Commands.literal("mode")
						.then(Commands.literal(VoiceConfig.MODE_VOICEPRINT)
								.executes(context -> setMode(context.getSource(), VoiceConfig.MODE_VOICEPRINT)))
						.then(Commands.literal(VoiceConfig.MODE_SHOUT)
								.executes(context -> setMode(context.getSource(), VoiceConfig.MODE_SHOUT))))
				.then(Commands.literal("reload")
						.executes(context -> reload(context.getSource()))));
	}

	private static int enrollAll(CommandSourceStack source) {
		List<String> phrases = new ArrayList<>(VoiceConfig.get().allPhrases());
		if (!EnrollmentSession.start(phrases)) {
			feedback(source, Component.translatable("message.jjkvoice.command.no_phrases")
					.withStyle(ChatFormatting.RED));
			return 0;
		}
		return phrases.size();
	}

	private static int enrollCommand(CommandSourceStack source, String rawCommand) {
		String command = VoiceConfig.normaliseCommand(rawCommand);
		if (!JjkBridge.commandKeys().contains(command)) {
			feedback(source, Component.translatable("message.jjkvoice.command.unknown_command", command)
					.withStyle(ChatFormatting.RED));
			return 0;
		}
		List<String> phrases = VoiceConfig.get().phrasesFor(command);
		if (phrases.isEmpty() || !EnrollmentSession.start(phrases)) {
			feedback(source, Component.translatable("message.jjkvoice.command.no_phrases_for", command)
					.withStyle(ChatFormatting.RED));
			return 0;
		}
		return phrases.size();
	}

	private static int addPhrase(CommandSourceStack source, String rawCommand, String rawPhrase) {
		String command = VoiceConfig.normaliseCommand(rawCommand);
		String phrase = VoiceConfig.normalisePhrase(rawPhrase);
		if (!JjkBridge.commandKeys().contains(command)) {
			feedback(source, Component.translatable("message.jjkvoice.command.unknown_command", command)
					.withStyle(ChatFormatting.RED));
			return 0;
		}
		if (!VoiceConfig.get().addPhrase(command, phrase)) {
			// Either empty, or already bound -- one phrase cannot mean two techniques.
			feedback(source, Component.translatable("message.jjkvoice.command.phrase_rejected", phrase)
					.withStyle(ChatFormatting.RED));
			return 0;
		}
		feedback(source, Component.translatable("message.jjkvoice.command.phrase_added", phrase, command)
				.withStyle(ChatFormatting.GREEN));
		EnrollmentSession.start(List.of(phrase));
		return 1;
	}

	private static int cancel(CommandSourceStack source) {
		if (!EnrollmentSession.isActive()) {
			feedback(source, Component.translatable("message.jjkvoice.command.nothing_to_cancel")
					.withStyle(ChatFormatting.GRAY));
			return 0;
		}
		EnrollmentSession.cancel();
		return 1;
	}

	private static int status(CommandSourceStack source) {
		VoiceConfig config = VoiceConfig.get();
		feedback(source, Component.translatable("message.jjkvoice.command.status_header")
				.withStyle(ChatFormatting.LIGHT_PURPLE));
		feedback(source, Component.translatable("message.jjkvoice.command.status_mode", config.mode)
				.withStyle(ChatFormatting.GRAY));
		feedback(source, Component.translatable("message.jjkvoice.command.status_voicechat",
				VoicechatBridge.isClientReady()
						? Component.translatable("message.jjkvoice.command.ready").withStyle(ChatFormatting.GREEN)
						: Component.translatable("message.jjkvoice.command.not_ready").withStyle(ChatFormatting.RED))
				.withStyle(ChatFormatting.GRAY));

		int enrolled = 0;
		for (String command : config.commands.keySet()) {
			List<String> phrases = config.phrasesFor(command);
			List<String> done = new ArrayList<>();
			List<String> missing = new ArrayList<>();
			for (String phrase : phrases)
				(VoicePrintStore.isEnrolled(phrase) ? done : missing).add(phrase);
			if (!done.isEmpty())
				enrolled++;

			// One line per technique rather than per phrase: nineteen techniques with
			// two phrases each would otherwise bury the chat.
			feedback(source, Component.translatable("message.jjkvoice.command.status_command",
					command, done.size(), phrases.size(),
					missing.isEmpty() ? "-" : String.join(", ", missing))
					.withStyle(done.isEmpty() ? ChatFormatting.YELLOW : ChatFormatting.AQUA));
		}
		feedback(source, Component.translatable("message.jjkvoice.command.status_summary",
				enrolled, config.commands.size()).withStyle(ChatFormatting.GRAY));
		return enrolled;
	}

	private static int forget(CommandSourceStack source, String rawPhrase) {
		String phrase = VoiceConfig.normalisePhrase(rawPhrase);
		boolean removed = VoicePrintStore.forget(phrase);
		feedback(source, removed
				? Component.translatable("message.jjkvoice.command.forgot", phrase).withStyle(ChatFormatting.GREEN)
				: Component.translatable("message.jjkvoice.command.not_enrolled_phrase", phrase)
						.withStyle(ChatFormatting.GRAY));
		return removed ? 1 : 0;
	}

	private static int forgetAll(CommandSourceStack source) {
		VoicePrintStore.forgetAll();
		feedback(source, Component.translatable("message.jjkvoice.command.forgot_all")
				.withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int setMode(CommandSourceStack source, String mode) {
		VoiceConfig config = VoiceConfig.get();
		config.mode = mode;
		config.save();
		feedback(source, Component.translatable("message.jjkvoice.command.mode_set", mode)
				.withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int reload(CommandSourceStack source) {
		VoiceConfig.reload();
		feedback(source, Component.translatable("message.jjkvoice.command.reloaded")
				.withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static void feedback(CommandSourceStack source, Component message) {
		source.sendSuccess(() -> message, false);
	}
}
