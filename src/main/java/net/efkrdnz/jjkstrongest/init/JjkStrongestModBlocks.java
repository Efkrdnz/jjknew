
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.efkrdnz.jjkstrongest.block.DomainBarrierBlock;
import net.efkrdnz.jjkstrongest.block.CustomPortalBlock;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK, JjkStrongestMod.MODID);
	public static final DeferredHolder<Block, Block> CUSTOM_PORTAL = REGISTRY.register("custom_portal", () -> new CustomPortalBlock());
	public static final DeferredHolder<Block, Block> DOMAIN_BARRIER = REGISTRY.register("domain_barrier", () -> new DomainBarrierBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
