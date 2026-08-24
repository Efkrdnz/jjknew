
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

import net.efkrdnz.jjkstrongest.block.entity.CustomPortalBlockEntity;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, JjkStrongestMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> CUSTOM_PORTAL = register("custom_portal", JjkStrongestModBlocks.CUSTOM_PORTAL, CustomPortalBlockEntity::new);

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
