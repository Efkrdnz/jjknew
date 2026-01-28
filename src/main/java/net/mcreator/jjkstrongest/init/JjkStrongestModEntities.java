
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.ReversalRedEntity;
import net.mcreator.jjkstrongest.entity.MalevolentShrineEntity;
import net.mcreator.jjkstrongest.entity.LapseBlueEntity;
import net.mcreator.jjkstrongest.entity.InfiniteHollowEntity;
import net.mcreator.jjkstrongest.entity.HollowPurpleProjectileEntity;
import net.mcreator.jjkstrongest.entity.HollowPurpleChargeEntity;
import net.mcreator.jjkstrongest.entity.HollowPurpleBigEntity;
import net.mcreator.jjkstrongest.entity.FlameArrowExplosionEntity;
import net.mcreator.jjkstrongest.entity.FlameArrowEntity;
import net.mcreator.jjkstrongest.entity.DomainUVEntity;
import net.mcreator.jjkstrongest.entity.DismantleProjectileEntity;
import net.mcreator.jjkstrongest.entity.BFEntityEntity;
import net.mcreator.jjkstrongest.JjkStrongestMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class JjkStrongestModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JjkStrongestMod.MODID);
	public static final RegistryObject<EntityType<LapseBlueEntity>> LAPSE_BLUE = register("lapse_blue", EntityType.Builder.<LapseBlueEntity>of(LapseBlueEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
			.setUpdateInterval(3).setCustomClientFactory(LapseBlueEntity::new).fireImmune().sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<ReversalRedEntity>> REVERSAL_RED = register("reversal_red", EntityType.Builder.<ReversalRedEntity>of(ReversalRedEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(ReversalRedEntity::new).fireImmune().sized(0.2f, 0.2f));
	public static final RegistryObject<EntityType<HollowPurpleChargeEntity>> HOLLOW_PURPLE_CHARGE = register("hollow_purple_charge", EntityType.Builder.<HollowPurpleChargeEntity>of(HollowPurpleChargeEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HollowPurpleChargeEntity::new).fireImmune().sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<HollowPurpleProjectileEntity>> HOLLOW_PURPLE_PROJECTILE = register("hollow_purple_projectile",
			EntityType.Builder.<HollowPurpleProjectileEntity>of(HollowPurpleProjectileEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)
					.setCustomClientFactory(HollowPurpleProjectileEntity::new).fireImmune().sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<InfiniteHollowEntity>> INFINITE_HOLLOW = register("infinite_hollow", EntityType.Builder.<InfiniteHollowEntity>of(InfiniteHollowEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(InfiniteHollowEntity::new).fireImmune().sized(3f, 3f));
	public static final RegistryObject<EntityType<DismantleProjectileEntity>> DISMANTLE_PROJECTILE = register("dismantle_projectile", EntityType.Builder.<DismantleProjectileEntity>of(DismantleProjectileEntity::new, MobCategory.MISC)
			.setCustomClientFactory(DismantleProjectileEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.1f, 0.1f));
	public static final RegistryObject<EntityType<BFEntityEntity>> BF_ENTITY = register("bf_entity",
			EntityType.Builder.<BFEntityEntity>of(BFEntityEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(BFEntityEntity::new).fireImmune().sized(0.1f, 0.1f));
	public static final RegistryObject<EntityType<MalevolentShrineEntity>> MALEVOLENT_SHRINE = register("malevolent_shrine", EntityType.Builder.<MalevolentShrineEntity>of(MalevolentShrineEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(MalevolentShrineEntity::new).fireImmune().sized(4f, 4f));
	public static final RegistryObject<EntityType<DomainUVEntity>> DOMAIN_UV = register("domain_uv",
			EntityType.Builder.<DomainUVEntity>of(DomainUVEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(DomainUVEntity::new).fireImmune().sized(0.1f, 0.1f));
	public static final RegistryObject<EntityType<FlameArrowEntity>> FLAME_ARROW = register("flame_arrow",
			EntityType.Builder.<FlameArrowEntity>of(FlameArrowEntity::new, MobCategory.MISC).setCustomClientFactory(FlameArrowEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<FlameArrowExplosionEntity>> FLAME_ARROW_EXPLOSION = register("flame_arrow_explosion", EntityType.Builder.<FlameArrowExplosionEntity>of(FlameArrowExplosionEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(FlameArrowExplosionEntity::new).fireImmune().sized(0.1f, 0.1f));
	public static final RegistryObject<EntityType<HollowPurpleBigEntity>> HOLLOW_PURPLE_BIG = register("hollow_purple_big", EntityType.Builder.<HollowPurpleBigEntity>of(HollowPurpleBigEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(HollowPurpleBigEntity::new).fireImmune().sized(0.6f, 0.6f));

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			LapseBlueEntity.init();
			ReversalRedEntity.init();
			HollowPurpleChargeEntity.init();
			HollowPurpleProjectileEntity.init();
			InfiniteHollowEntity.init();
			BFEntityEntity.init();
			MalevolentShrineEntity.init();
			DomainUVEntity.init();
			FlameArrowExplosionEntity.init();
			HollowPurpleBigEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(LAPSE_BLUE.get(), LapseBlueEntity.createAttributes().build());
		event.put(REVERSAL_RED.get(), ReversalRedEntity.createAttributes().build());
		event.put(HOLLOW_PURPLE_CHARGE.get(), HollowPurpleChargeEntity.createAttributes().build());
		event.put(HOLLOW_PURPLE_PROJECTILE.get(), HollowPurpleProjectileEntity.createAttributes().build());
		event.put(INFINITE_HOLLOW.get(), InfiniteHollowEntity.createAttributes().build());
		event.put(BF_ENTITY.get(), BFEntityEntity.createAttributes().build());
		event.put(MALEVOLENT_SHRINE.get(), MalevolentShrineEntity.createAttributes().build());
		event.put(DOMAIN_UV.get(), DomainUVEntity.createAttributes().build());
		event.put(FLAME_ARROW_EXPLOSION.get(), FlameArrowExplosionEntity.createAttributes().build());
		event.put(HOLLOW_PURPLE_BIG.get(), HollowPurpleBigEntity.createAttributes().build());
	}
}
