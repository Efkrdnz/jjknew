
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.mcreator.jjkstrongest.block.DomainBarrierBlock;
import net.mcreator.jjkstrongest.block.CustomPortalBlock;
import net.mcreator.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, JjkStrongestMod.MODID);
	public static final RegistryObject<Block> CUSTOM_PORTAL = REGISTRY.register("custom_portal", () -> new CustomPortalBlock());
	public static final RegistryObject<Block> DOMAIN_BARRIER = REGISTRY.register("domain_barrier", () -> new DomainBarrierBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
