
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import net.mcreator.jjkstrongest.potion.ZoneMobEffect;
import net.mcreator.jjkstrongest.potion.PurpleChargingMobEffect;
import net.mcreator.jjkstrongest.potion.InfinityMobEffect;
import net.mcreator.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, JjkStrongestMod.MODID);
	public static final RegistryObject<MobEffect> INFINITY = REGISTRY.register("infinity", () -> new InfinityMobEffect());
	public static final RegistryObject<MobEffect> PURPLE_CHARGING = REGISTRY.register("purple_charging", () -> new PurpleChargingMobEffect());
	public static final RegistryObject<MobEffect> ZONE = REGISTRY.register("zone", () -> new ZoneMobEffect());
}
