package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

import net.efkrdnz.jjkstrongest.block.entity.CustomPortalBlockEntity;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

@EventBusSubscriber(modid = JjkStrongestMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class JjkStrongestModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, JjkStrongestMod.MODID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> CUSTOM_PORTAL = register("custom_portal", JjkStrongestModBlocks.CUSTOM_PORTAL, CustomPortalBlockEntity::new);

	private static DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> register(String registryname, DeferredHolder<Block, Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}

	/**
	 * On 1.20.1 the block entity exposed its sided IItemHandler by overriding
	 * getCapability. NeoForge 1.21.1 registers block capabilities against the
	 * block entity type instead, so it lives here rather than on the class.
	 */
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType<CustomPortalBlockEntity>) CUSTOM_PORTAL.get(),
				(blockEntity, side) -> side == null ? new InvWrapper(blockEntity) : new SidedInvWrapper(blockEntity, side));
	}
}
