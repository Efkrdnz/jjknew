
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.MenuType;

import net.efkrdnz.jjkstrongest.world.inventory.ArmAnimationEditorMenu;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.MENU, JjkStrongestMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<ArmAnimationEditorMenu>> ARM_ANIMATION_EDITOR = REGISTRY.register("arm_animation_editor", () -> IMenuTypeExtension.create(ArmAnimationEditorMenu::new));
}
