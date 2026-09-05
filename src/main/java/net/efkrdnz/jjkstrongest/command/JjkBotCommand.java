package net.efkrdnz.jjkstrongest.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.entity.DebugBotAbilities;
import net.efkrdnz.jjkstrongest.entity.DebugBotEntity;
import net.efkrdnz.jjkstrongest.entity.DebugBots;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.List;
import java.util.Locale;

/**
 * Drives debug bots from the command line.
 *
 * <p>Two of these, aimed at each other and told to open a domain, is the view this whole
 * system has never had: a clash watched from outside instead of from inside one of the
 * spheres, where the interior fills the screen and the barrier is behind you.
 */
@EventBusSubscriber
public class JjkBotCommand {

	private static final SuggestionProvider<CommandSourceStack> BOT_NAMES = (ctx, builder) -> SharedSuggestionProvider.suggest(DebugBots.names(ctx.getSource().getLevel()), builder);
	private static final SuggestionProvider<CommandSourceStack> ABILITIES = (ctx, builder) -> SharedSuggestionProvider.suggest(DebugBotAbilities.names(), builder);
	private static final SuggestionProvider<CommandSourceStack> CHARACTERS = (ctx, builder) -> SharedSuggestionProvider.suggest(List.of("gojo", "sukuna", "inumaki", "yuji"), builder);

	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> bot = Commands.literal("bot")

				.then(Commands.literal("spawn").then(Commands.argument("name", StringArgumentType.word())
						.then(Commands.argument("character", StringArgumentType.word()).suggests(CHARACTERS).executes(ctx -> spawn(ctx, StringArgumentType.getString(ctx, "name"),
								StringArgumentType.getString(ctx, "character"))))))

				.then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
				.then(Commands.literal("clear").executes(ctx -> clear(ctx.getSource())))

				.then(Commands.argument("name", StringArgumentType.word()).suggests(BOT_NAMES)

						.then(Commands.literal("here").executes(ctx -> teleport(ctx, ctx.getSource().getPosition())))
						.then(Commands.literal("tp").then(Commands.argument("x", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
								.then(Commands.argument("y", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
										.then(Commands.argument("z", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg()).executes(ctx -> teleport(ctx,
												new Vec3(com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "x"), com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "y"),
														com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "z"))))))))

						.then(Commands.literal("aim")
								.then(Commands.literal("me").executes(ctx -> aimAtSource(ctx)))
								.then(Commands.argument("target", StringArgumentType.word()).suggests(BOT_NAMES).executes(ctx -> aimAtBot(ctx, StringArgumentType.getString(ctx, "target")))))

						.then(Commands.literal("use").then(Commands.argument("ability", StringArgumentType.word()).suggests(ABILITIES)
								.executes(ctx -> use(ctx, StringArgumentType.getString(ctx, "ability")))))

						.then(Commands.literal("chant").then(Commands.argument("ability", StringArgumentType.word()).suggests(ABILITIES)
								.executes(ctx -> chant(ctx, StringArgumentType.getString(ctx, "ability")))))
						.then(Commands.literal("release").executes(ctx -> release(ctx)))

						.then(Commands.literal("freeze").executes(ctx -> freeze(ctx, true)))
						.then(Commands.literal("thaw").executes(ctx -> freeze(ctx, false)))
						.then(Commands.literal("kill").executes(ctx -> kill(ctx))));

		event.getDispatcher().register(Commands.literal("jjk").requires(source -> source.hasPermission(3)).then(bot));
	}

	// ---- lifecycle ----------------------------------------------------------

	private static int spawn(CommandContext<CommandSourceStack> ctx, String name, String character) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		if (DebugBots.byName(level, name) != null) {
			source.sendFailure(Component.literal("There is already a bot called " + name + "."));
			return 0;
		}
		String kind = character.toLowerCase(Locale.ROOT);
		Vec3 at = source.getPosition();
		DebugBotEntity bot = JjkStrongestModEntities.DEBUG_BOT.get().spawn(level, net.minecraft.core.BlockPos.containing(at), MobSpawnType.COMMAND);
		if (bot == null) {
			source.sendFailure(Component.literal("Could not spawn the bot."));
			return 0;
		}
		bot.moveTo(at.x, at.y, at.z, source.getEntity() == null ? 0f : source.getEntity().getYRot(), 0f);
		bot.setBotName(name);
		DebugBotAbilities.become(bot, kind);
		source.sendSuccess(() -> Component.literal("Spawned " + name + " as " + kind + ". Try: /jjk bot " + name + " use domain"), true);
		return 1;
	}

	private static int list(CommandSourceStack source) {
		List<DebugBotEntity> bots = DebugBots.in(source.getLevel());
		if (bots.isEmpty()) {
			source.sendSuccess(() -> Component.literal("No bots."), false);
			return 0;
		}
		for (DebugBotEntity bot : bots) {
			if (!bot.isAlive())
				continue;
			source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "%s  %s  %.0f %.0f %.0f  %.0f/%.0f hp%s", bot.getBotName(), bot.getCharacter(), bot.getX(), bot.getY(), bot.getZ(),
					bot.getHealth(), bot.getMaxHealth(), bot.isFrozen() ? "  frozen" : "")), false);
		}
		return bots.size();
	}

	private static int clear(CommandSourceStack source) {
		int killed = 0;
		for (DebugBotEntity bot : DebugBots.in(source.getLevel())) {
			bot.discard();
			killed++;
		}
		int count = killed;
		source.sendSuccess(() -> Component.literal("Removed " + count + " bot" + (count == 1 ? "" : "s") + "."), true);
		return killed;
	}

	private static int kill(CommandContext<CommandSourceStack> ctx) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		String name = bot.getBotName();
		bot.discard();
		ctx.getSource().sendSuccess(() -> Component.literal("Removed " + name + "."), true);
		return 1;
	}

	// ---- control ------------------------------------------------------------

	private static int teleport(CommandContext<CommandSourceStack> ctx, Vec3 to) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		bot.teleportTo(to.x, to.y, to.z);
		bot.setDeltaMovement(Vec3.ZERO);
		bot.resetFallDistance();
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + " moved."), false);
		return 1;
	}

	private static int aimAtBot(CommandContext<CommandSourceStack> ctx, String targetName) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		DebugBotEntity target = DebugBots.byName(ctx.getSource().getLevel(), targetName);
		if (target == null) {
			ctx.getSource().sendFailure(Component.literal("No bot called " + targetName + "."));
			return 0;
		}
		bot.lookAt(target.position().add(0.0, target.getEyeHeight(), 0.0));
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + " is looking at " + target.getBotName() + "."), false);
		return 1;
	}

	private static int aimAtSource(CommandContext<CommandSourceStack> ctx) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		Entity at = ctx.getSource().getEntity();
		bot.lookAt(at == null ? ctx.getSource().getPosition() : at.position().add(0.0, at.getEyeHeight(), 0.0));
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + " is looking at you."), false);
		return 1;
	}

	private static int freeze(CommandContext<CommandSourceStack> ctx, boolean frozen) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		bot.setFrozen(frozen);
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + (frozen ? " will hold position." : " can be moved again.")), false);
		return 1;
	}

	// ---- abilities ----------------------------------------------------------

	private static int use(CommandContext<CommandSourceStack> ctx, String ability) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		if (!DebugBotAbilities.use(bot, ability)) {
			ctx.getSource().sendFailure(Component.literal("No ability called " + ability + ". Known: " + String.join(", ", DebugBotAbilities.names())));
			return 0;
		}
		DebugBotAbilities.Ability info = DebugBotAbilities.get(ability);
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + ": " + info.description()
				+ (info.holdTicks() > 0 ? " (charging " + info.holdTicks() + " ticks, then it fires)" : "")), true);
		return 1;
	}

	private static int chant(CommandContext<CommandSourceStack> ctx, String ability) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		if (!DebugBotAbilities.beginChant(bot, ability)) {
			ctx.getSource().sendFailure(Component.literal("No ability called " + ability + "."));
			return 0;
		}
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + " is charging " + ability + " — /jjk bot " + bot.getBotName() + " release to let it go."), true);
		return 1;
	}

	private static int release(CommandContext<CommandSourceStack> ctx) {
		DebugBotEntity bot = resolve(ctx);
		if (bot == null)
			return 0;
		DebugBotAbilities.releaseHeld(bot);
		ctx.getSource().sendSuccess(() -> Component.literal(bot.getBotName() + " let go."), true);
		return 1;
	}

	// ---- shared -------------------------------------------------------------

	private static DebugBotEntity resolve(CommandContext<CommandSourceStack> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		DebugBotEntity bot = DebugBots.byName(ctx.getSource().getLevel(), name);
		if (bot == null)
			ctx.getSource().sendFailure(Component.literal("No bot called " + name + ". /jjk bot list to see them."));
		return bot;
	}
}
