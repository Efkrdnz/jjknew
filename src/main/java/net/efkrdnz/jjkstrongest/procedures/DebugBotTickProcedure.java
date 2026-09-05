package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.Level;

import net.efkrdnz.jjkstrongest.entity.DebugBotEntity;

/**
 * Runs, for a bot, the per-tick work the mod only ever does for players.
 *
 * <p>Fourteen of the mod's tick handlers subscribe to {@code PlayerTickEvent.Post}, so for
 * anything that is not a player they simply never run. The consequence is not subtle:
 * {@code ChantOnTickProcedure} is what climbs {@code ChantCounter} and sets
 * {@code TechniquePower} at each threshold, so without it a bot's charge never builds and
 * every technique comes out at base output — and cooldowns, having nothing to decrement
 * them, never expire, so each ability fires exactly once and then goes quiet forever.
 *
 * <p>Almost all of them expose an entity-generic {@code execute(...)} next to the event
 * subscriber. This calls those. The bot is otherwise a puppet — nothing here decides to do
 * anything, it only lets what was already told to happen actually progress.
 */
public class DebugBotTickProcedure {

	public static void execute(DebugBotEntity bot) {
		if (bot == null)
			return;
		Level level = bot.level();
		if (level == null || level.isClientSide())
			return;
		double x = bot.getX();
		double y = bot.getY();
		double z = bot.getZ();

		// The charge ladder, and the arm animation it drives at the end of its own tick.
		ChantOnTickProcedure.execute(level, x, y, z, bot);

		// Cooldowns, each of which gates an ability behind a counter nothing else decrements.
		TeleportCooldownTickProcedure.execute(level, bot);
		BlackFlashQTETickProcedure.execute(bot);
		Hold2UseOnTickProcedure.execute(level, bot);
		MeleeOntickProcedure.execute(level, bot);
		BlockingTickProcedure.execute(bot);

		// State that keeps a technique running once started.
		RCTSelftickProcedure.execute(level, x, y, z, bot);
		RedFlightHandlerProcedure.execute(bot);
		DomainEffectTickProcedure.execute(bot);
		MaxHealthSetOnTickProcedure.execute(level, bot);
	}
}
