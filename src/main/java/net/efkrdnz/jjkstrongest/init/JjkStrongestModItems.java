
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.efkrdnz.jjkstrongest.item.TestDismantleItem;
import net.efkrdnz.jjkstrongest.item.BFTestItemItem;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, JjkStrongestMod.MODID);
	public static final DeferredHolder<Item, Item> REVERSAL_RED_SPAWN_EGG = REGISTRY.register("reversal_red_spawn_egg", () -> new DeferredSpawnEggItem(JjkStrongestModEntities.REVERSAL_RED, -1, -1, new Item.Properties()));
	public static final DeferredHolder<Item, Item> CUSTOM_PORTAL = block(JjkStrongestModBlocks.CUSTOM_PORTAL);
	public static final DeferredHolder<Item, Item> BF_TEST_ITEM = REGISTRY.register("bf_test_item", () -> new BFTestItemItem());
	public static final DeferredHolder<Item, Item> TEST_DISMANTLE = REGISTRY.register("test_dismantle", () -> new TestDismantleItem());
	public static final DeferredHolder<Item, Item> DOMAIN_BARRIER = block(JjkStrongestModBlocks.DOMAIN_BARRIER);
	public static final DeferredHolder<Item, Item> MAHORAGA_SPAWN_EGG = REGISTRY.register("mahoraga_spawn_egg", () -> new DeferredSpawnEggItem(JjkStrongestModEntities.MAHORAGA, -1, -26317, new Item.Properties()));
	public static final DeferredHolder<Item, Item> SUKUNA_SPAWN_EGG = REGISTRY.register("sukuna_spawn_egg", () -> new DeferredSpawnEggItem(JjkStrongestModEntities.SUKUNA, -13159, -39271, new Item.Properties()));

	// Start of user code block custom items
	// Gojo spawn egg — 0x4FC3F7 (infinity blue) / 0xFFFFFF (white blindfold)
	public static final DeferredHolder<Item, Item> GOJO_SPAWN_EGG = REGISTRY.register("gojo_spawn_egg",
			() -> new DeferredSpawnEggItem(JjkStrongestModEntities.GOJO, 0x4FC3F7, 0xFFFFFF, new Item.Properties()));
	// End of user code block custom items
	private static DeferredHolder<Item, Item> block(DeferredHolder<Block, Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
