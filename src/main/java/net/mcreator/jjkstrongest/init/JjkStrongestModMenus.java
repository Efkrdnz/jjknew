
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

import net.mcreator.jjkstrongest.world.inventory.ArmAnimationEditorMenu;
import net.mcreator.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, JjkStrongestMod.MODID);
	public static final RegistryObject<MenuType<ArmAnimationEditorMenu>> ARM_ANIMATION_EDITOR = REGISTRY.register("arm_animation_editor", () -> IForgeMenuType.create(ArmAnimationEditorMenu::new));
}
