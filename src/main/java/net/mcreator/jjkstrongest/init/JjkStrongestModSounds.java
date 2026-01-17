
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.mcreator.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JjkStrongestMod.MODID);
	public static final RegistryObject<SoundEvent> HOLLOWPURPLE = REGISTRY.register("hollowpurple", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("jjk_strongest", "hollowpurple")));
	public static final RegistryObject<SoundEvent> KAI = REGISTRY.register("kai", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("jjk_strongest", "kai")));
	public static final RegistryObject<SoundEvent> BLACKFLASH = REGISTRY.register("blackflash", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("jjk_strongest", "blackflash")));
}
