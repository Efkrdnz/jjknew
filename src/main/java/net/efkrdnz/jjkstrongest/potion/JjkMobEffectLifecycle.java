package net.efkrdnz.jjkstrongest.potion;

import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.procedures.InfinityOnEffectExpireProcedure;
import net.efkrdnz.jjkstrongest.procedures.InformationOverloadEffectExpiresProcedure;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

/**
 * On 1.20.1 the "effect wore off" hook was MobEffect#removeAttributeModifiers,
 * which still received the LivingEntity. 1.21 narrowed that method to
 * {@code removeAttributeModifiers(AttributeMap)} and offers no replacement that
 * knows the entity, so the two effects that needed it listen for the removal
 * events instead.
 *
 * <p>Expired fires when the duration runs out; Remove fires when the effect is
 * cleared early (milk, /effect clear, death cleanup). Both used to funnel
 * through the same call, so both do here.
 */
@EventBusSubscriber(modid = JjkStrongestMod.MODID)
public class JjkMobEffectLifecycle {
	@SubscribeEvent
	public static void onEffectExpired(MobEffectEvent.Expired event) {
		dispatch(event.getEntity(), event.getEffectInstance());
	}

	@SubscribeEvent
	public static void onEffectRemoved(MobEffectEvent.Remove event) {
		dispatch(event.getEntity(), event.getEffectInstance());
	}

	private static void dispatch(LivingEntity entity, MobEffectInstance instance) {
		if (entity == null || instance == null)
			return;
		if (instance.getEffect() == JjkStrongestModMobEffects.INFINITY) {
			InfinityOnEffectExpireProcedure.execute(entity);
		} else if (instance.getEffect() == JjkStrongestModMobEffects.INFORMATION_OVERLOAD) {
			InformationOverloadEffectExpiresProcedure.execute(entity);
		}
	}
}
