package net.efkrdnz.jjkstrongest.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The bots in each level, addressable by name.
 *
 * <p>Names rather than entity selectors because these are typed by hand, repeatedly, while
 * iterating: {@code /jjk bot a aim b} is the whole point, and it has to be quicker to write
 * than what it replaces.
 */
@EventBusSubscriber(modid = "jjk_strongest")
public final class DebugBots {

	private static final Map<Level, List<DebugBotEntity>> BOTS = Collections.synchronizedMap(new WeakHashMap<>());

	private DebugBots() {
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof DebugBotEntity bot))
			return;
		synchronized (BOTS) {
			List<DebugBotEntity> list = BOTS.computeIfAbsent(event.getLevel(), l -> new ArrayList<>(2));
			if (!list.contains(bot))
				list.add(bot);
		}
	}

	@SubscribeEvent
	public static void onLeave(EntityLeaveLevelEvent event) {
		if (!(event.getEntity() instanceof DebugBotEntity bot))
			return;
		synchronized (BOTS) {
			List<DebugBotEntity> list = BOTS.get(event.getLevel());
			if (list != null)
				list.remove(bot);
		}
	}

	/** The list holds entities that hold the level, so the weak key cannot expire on its own. */
	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event) {
		synchronized (BOTS) {
			BOTS.remove(event.getLevel());
		}
	}

	public static List<DebugBotEntity> in(Level level) {
		synchronized (BOTS) {
			List<DebugBotEntity> list = BOTS.get(level);
			if (list == null || list.isEmpty())
				return Collections.emptyList();
			return new ArrayList<>(list);
		}
	}

	public static DebugBotEntity byName(Level level, String name) {
		if (name == null || name.isEmpty())
			return null;
		String wanted = name.toLowerCase(Locale.ROOT);
		for (DebugBotEntity bot : in(level)) {
			if (bot.isAlive() && bot.getBotName().toLowerCase(Locale.ROOT).equals(wanted))
				return bot;
		}
		return null;
	}

	public static List<String> names(Level level) {
		List<String> out = new ArrayList<>();
		for (DebugBotEntity bot : in(level))
			if (bot.isAlive() && !bot.getBotName().isEmpty())
				out.add(bot.getBotName());
		return out;
	}
}
