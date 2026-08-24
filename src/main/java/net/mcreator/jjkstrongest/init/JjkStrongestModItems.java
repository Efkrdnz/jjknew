
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.jjkstrongest.item.TestDismantleItem;
import net.mcreator.jjkstrongest.item.BFTestItemItem;
import net.mcreator.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, JjkStrongestMod.MODID);
	public static final RegistryObject<Item> REVERSAL_RED_SPAWN_EGG = REGISTRY.register("reversal_red_spawn_egg", () -> new ForgeSpawnEggItem(JjkStrongestModEntities.REVERSAL_RED, -1, -1, new Item.Properties()));
	public static final RegistryObject<Item> CUSTOM_PORTAL = block(JjkStrongestModBlocks.CUSTOM_PORTAL);
	public static final RegistryObject<Item> BF_TEST_ITEM = REGISTRY.register("bf_test_item", () -> new BFTestItemItem());
	public static final RegistryObject<Item> TEST_DISMANTLE = REGISTRY.register("test_dismantle", () -> new TestDismantleItem());
	public static final RegistryObject<Item> DOMAIN_BARRIER = block(JjkStrongestModBlocks.DOMAIN_BARRIER);
	public static final RegistryObject<Item> MAHORAGA_SPAWN_EGG = REGISTRY.register("mahoraga_spawn_egg", () -> new ForgeSpawnEggItem(JjkStrongestModEntities.MAHORAGA, -1, -26317, new Item.Properties()));
	public static final RegistryObject<Item> SUKUNA_SPAWN_EGG = REGISTRY.register("sukuna_spawn_egg", () -> new ForgeSpawnEggItem(JjkStrongestModEntities.SUKUNA, -13159, -39271, new Item.Properties()));

	// Start of user code block custom items
	// Gojo spawn egg — 0x4FC3F7 (infinity blue) / 0xFFFFFF (white blindfold)
	public static final RegistryObject<Item> GOJO_SPAWN_EGG = REGISTRY.register("gojo_spawn_egg",
			() -> new ForgeSpawnEggItem(JjkStrongestModEntities.GOJO, 0x4FC3F7, 0xFFFFFF, new Item.Properties()));
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
