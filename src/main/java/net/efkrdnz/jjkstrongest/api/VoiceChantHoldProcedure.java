package net.efkrdnz.jjkstrongest.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Lets a spoken chant lapse if it is never released.
 *
 * <p>Holding a technique key has a natural end: the player lets go. A chant does
 * not, so without this a charge raised by voice would sit on the player until
 * they happened to press something, and would still be there after they had
 * moved on to another ability entirely.
 *
 * <p>It only ever ends a chant a voice started, and only while that same chant is
 * still the one running. A player who took over with the key owns it from then
 * on, and this must not cut them off mid-charge.
 */
@EventBusSubscriber
public class VoiceChantHoldProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player == null || player.level().isClientSide())
			return;
		CompoundTag data = player.getPersistentData();

		int remaining = data.getInt(JjkVoiceApi.HOLD_TICKS);
		if (remaining <= 0)
			return;

		String owned = data.getString(JjkVoiceApi.HOLD_STATE);
		if (owned == null || owned.isEmpty() || !owned.equals(data.getString("chanting"))) {
			// Released, or replaced by something else. Either way it is no longer ours.
			data.putInt(JjkVoiceApi.HOLD_TICKS, 0);
			data.putString(JjkVoiceApi.HOLD_STATE, "");
			return;
		}

		keepPurpleAlive(player, owned);

		remaining--;
		data.putInt(JjkVoiceApi.HOLD_TICKS, remaining);
		if (remaining > 0)
			return;

		// Lapsed. Clearing the chant is enough to drop the charge -- ChantOnTick
		// zeroes the counter and the multiplier on the next tick by itself.
		data.putString("chanting", "");
		data.putString(JjkVoiceApi.HOLD_STATE, "");
		player.displayClientMessage(Component.translatable("message.jjk_strongest.chant_lapsed"), true);
	}

	/**
	 * Holds Hollow Purple's charging effect open for as long as it is being
	 * chanted.
	 *
	 * <p>Technique3's press grants PURPLE_CHARGING for fifty ticks and its release
	 * refuses to fire without it, but Purple's first output tier is at seventy --
	 * so the effect is already gone by the time there is anything to show for the
	 * charge, and nothing anywhere refreshes it. Chanting takes longer still, being
	 * paced by speech rather than a held key, so without this the technique could
	 * be charged to full and then decline to come out.
	 *
	 * <p>Scoped to chants this started. It does not change what holding the key
	 * does, which is left exactly as it was.
	 */
	private static void keepPurpleAlive(Player player, String chant) {
		if (!"purple".equals(chant) || !(player instanceof LivingEntity living))
			return;
		if (living.hasEffect(JjkStrongestModMobEffects.PURPLE_CHARGING))
			return;
		living.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.PURPLE_CHARGING, 50, 1, false, false));
	}
}
