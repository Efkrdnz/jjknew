package net.efkrdnz.jjkstrongest.api;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Keeps a spoken chant "held" for as long as it was spoken for.
 *
 * <p>Holding a technique key charges an ability by leaving {@code chanting} set
 * while ChantOnTickProcedure advances the counter one per tick. A spoken chant
 * grants the same thing measured in ticks, and this drains that grant so the
 * chant ends on its own when the voice runs out — the same way letting go of the
 * key ends it.
 *
 * <p>Nothing here touches ChantCounter or the power multipliers. Doing so would
 * duplicate a curve that already exists in exactly one place, and the two copies
 * would eventually disagree. The chant simply stays open, and the mod's own tick
 * procedure does the charging.
 *
 * <p>Only chants this API started are ever ended here. A player physically
 * holding the key owns their chant and keeps it until they release.
 */
@EventBusSubscriber
public final class VoiceChantHoldProcedure {
	private VoiceChantHoldProcedure() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player) || entity.level().isClientSide())
			return;

		double remaining = entity.getPersistentData().getDouble(JjkVoiceApi.HOLD_TICKS);
		if (remaining <= 0)
			return;

		remaining -= 1;
		entity.getPersistentData().putDouble(JjkVoiceApi.HOLD_TICKS, Math.max(0, remaining));
		if (remaining > 0)
			return;

		// The grant is spent. Release the chant, but only if a voice opened it --
		// otherwise a player mid-hold would have the key pulled out from under them.
		if (entity.getPersistentData().getBoolean(JjkVoiceApi.HOLD_OWNED)) {
			entity.getPersistentData().putString("chanting", "");
			entity.getPersistentData().putBoolean(JjkVoiceApi.HOLD_OWNED, false);
		}
	}
}
