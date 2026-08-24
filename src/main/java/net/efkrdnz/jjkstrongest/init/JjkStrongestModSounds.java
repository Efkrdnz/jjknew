
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, JjkStrongestMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> HOLLOWPURPLE = REGISTRY.register("hollowpurple", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "hollowpurple")));
	public static final DeferredHolder<SoundEvent, SoundEvent> KAI = REGISTRY.register("kai", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "kai")));
	public static final DeferredHolder<SoundEvent, SoundEvent> BLACKFLASH = REGISTRY.register("blackflash", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "blackflash")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SUKUNA_DOMAIN_OST = REGISTRY.register("sukuna_domain_ost", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "sukuna_domain_ost")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SUKUNA_DOMAIN_ACT = REGISTRY.register("sukuna_domain_act", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "sukuna_domain_act")));
	public static final DeferredHolder<SoundEvent, SoundEvent> IMAGINARY_PURPLE_SHOOT = REGISTRY.register("imaginary_purple_shoot", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "imaginary_purple_shoot")));
	public static final DeferredHolder<SoundEvent, SoundEvent> WHEELSPIN = REGISTRY.register("wheelspin", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jjk_strongest", "wheelspin")));
}
