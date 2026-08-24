
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import net.efkrdnz.jjkstrongest.potion.ZoneMobEffect;
import net.efkrdnz.jjkstrongest.potion.PurpleChargingMobEffect;
import net.efkrdnz.jjkstrongest.potion.InformationOverloadMobEffect;
import net.efkrdnz.jjkstrongest.potion.InfinityMobEffect;
import net.efkrdnz.jjkstrongest.potion.DomainAmplificationMobEffect;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, JjkStrongestMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> INFINITY = REGISTRY.register("infinity", () -> new InfinityMobEffect());
	public static final DeferredHolder<MobEffect, MobEffect> PURPLE_CHARGING = REGISTRY.register("purple_charging", () -> new PurpleChargingMobEffect());
	public static final DeferredHolder<MobEffect, MobEffect> ZONE = REGISTRY.register("zone", () -> new ZoneMobEffect());
	public static final DeferredHolder<MobEffect, MobEffect> INFORMATION_OVERLOAD = REGISTRY.register("information_overload", () -> new InformationOverloadMobEffect());
	public static final DeferredHolder<MobEffect, MobEffect> DOMAIN_AMPLIFICATION = REGISTRY.register("domain_amplification", () -> new DomainAmplificationMobEffect());
}
