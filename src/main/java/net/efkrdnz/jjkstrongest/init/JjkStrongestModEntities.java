
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.entity.DebugBotEntity;
import net.efkrdnz.jjkstrongest.entity.SukunaEntity;
import net.efkrdnz.jjkstrongest.entity.GojoEntity;
import net.efkrdnz.jjkstrongest.entity.ReversalRedEntity;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;
import net.efkrdnz.jjkstrongest.entity.LapseBlueEntity;
import net.efkrdnz.jjkstrongest.entity.ImaginaryPurpleEntity;
import net.efkrdnz.jjkstrongest.entity.HollowPurpleProjectileEntity;
import net.efkrdnz.jjkstrongest.entity.HollowPurpleChargeEntity;
import net.efkrdnz.jjkstrongest.entity.HollowPurpleBigEntity;
import net.efkrdnz.jjkstrongest.entity.HollowNukeEntity;
import net.efkrdnz.jjkstrongest.entity.FugaDomainExplosionEntity;
import net.efkrdnz.jjkstrongest.entity.FlameArrowExplosionEntity;
import net.efkrdnz.jjkstrongest.entity.FlameArrowEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.entity.DismantleTravelEntity;
import net.efkrdnz.jjkstrongest.entity.DismantleProjectileEntity;
import net.efkrdnz.jjkstrongest.entity.BFEntityEntity;
import net.efkrdnz.jjkstrongest.entity.BlueVortexEntity;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class JjkStrongestModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, JjkStrongestMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<LapseBlueEntity>> LAPSE_BLUE = register("lapse_blue", EntityType.Builder.<LapseBlueEntity>of(LapseBlueEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
			.setUpdateInterval(3).fireImmune().sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<ReversalRedEntity>> REVERSAL_RED = register("reversal_red", EntityType.Builder.<ReversalRedEntity>of(ReversalRedEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.2f, 0.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<HollowPurpleChargeEntity>> HOLLOW_PURPLE_CHARGE = register("hollow_purple_charge", EntityType.Builder.<HollowPurpleChargeEntity>of(HollowPurpleChargeEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<HollowPurpleProjectileEntity>> HOLLOW_PURPLE_PROJECTILE = register("hollow_purple_projectile",
			EntityType.Builder.<HollowPurpleProjectileEntity>of(HollowPurpleProjectileEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)
					.fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<DismantleProjectileEntity>> DISMANTLE_PROJECTILE = register("dismantle_projectile", EntityType.Builder.<DismantleProjectileEntity>of(DismantleProjectileEntity::new, MobCategory.MISC)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.1f, 0.1f));
	public static final DeferredHolder<EntityType<?>, EntityType<BFEntityEntity>> BF_ENTITY = register("bf_entity",
			EntityType.Builder.<BFEntityEntity>of(BFEntityEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.1f, 0.1f));
	public static final DeferredHolder<EntityType<?>, EntityType<MalevolentShrineEntity>> MALEVOLENT_SHRINE = register("malevolent_shrine", EntityType.Builder.<MalevolentShrineEntity>of(MalevolentShrineEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(4f, 4f));
	public static final DeferredHolder<EntityType<?>, EntityType<DomainUVEntity>> DOMAIN_UV = register("domain_uv",
			EntityType.Builder.<DomainUVEntity>of(DomainUVEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.1f, 0.1f));
	public static final DeferredHolder<EntityType<?>, EntityType<FlameArrowEntity>> FLAME_ARROW = register("flame_arrow",
			EntityType.Builder.<FlameArrowEntity>of(FlameArrowEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<FlameArrowExplosionEntity>> FLAME_ARROW_EXPLOSION = register("flame_arrow_explosion", EntityType.Builder.<FlameArrowExplosionEntity>of(FlameArrowExplosionEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.1f, 0.1f));
	public static final DeferredHolder<EntityType<?>, EntityType<HollowPurpleBigEntity>> HOLLOW_PURPLE_BIG = register("hollow_purple_big", EntityType.Builder.<HollowPurpleBigEntity>of(HollowPurpleBigEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 0.6f));
	public static final DeferredHolder<EntityType<?>, EntityType<ImaginaryPurpleEntity>> IMAGINARY_PURPLE = register("imaginary_purple", EntityType.Builder.<ImaginaryPurpleEntity>of(ImaginaryPurpleEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.1f, 0.1f));
	public static final DeferredHolder<EntityType<?>, EntityType<MahoragaEntity>> MAHORAGA = register("mahoraga",
			EntityType.Builder.<MahoragaEntity>of(MahoragaEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3)

					.sized(1.2f, 3.6f));
	public static final DeferredHolder<EntityType<?>, EntityType<FugaDomainExplosionEntity>> FUGA_DOMAIN_EXPLOSION = register("fuga_domain_explosion", EntityType.Builder.<FugaDomainExplosionEntity>of(FugaDomainExplosionEntity::new, MobCategory.MONSTER)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<HollowNukeEntity>> HOLLOW_NUKE = register("hollow_nuke", EntityType.Builder.<HollowNukeEntity>of(HollowNukeEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
			.setUpdateInterval(3).fireImmune().sized(0.2f, 0.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<DismantleTravelEntity>> DISMANTLE_TRAVEL = register("dismantle_travel", EntityType.Builder.<DismantleTravelEntity>of(DismantleTravelEntity::new, MobCategory.MISC)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<EntityType<?>, EntityType<SukunaEntity>> SUKUNA = register("sukuna",
			EntityType.Builder.<SukunaEntity>of(SukunaEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3)

					.sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<GojoEntity>> GOJO = register("gojo",
			EntityType.Builder.<GojoEntity>of(GojoEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3)
					.sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<DebugBotEntity>> DEBUG_BOT = register("debug_bot",
			EntityType.Builder.<DebugBotEntity>of(DebugBotEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(1).sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<BlueVortexEntity>> BLUE_VORTEX = register("blue_vortex", EntityType.Builder.<BlueVortexEntity>of(BlueVortexEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(0.2f, 0.2f));

	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
		LapseBlueEntity.init(event);
		ReversalRedEntity.init(event);
		HollowPurpleChargeEntity.init(event);
		HollowPurpleProjectileEntity.init(event);
		BFEntityEntity.init(event);
		MalevolentShrineEntity.init(event);
		DomainUVEntity.init(event);
		FlameArrowExplosionEntity.init(event);
		HollowPurpleBigEntity.init(event);
		ImaginaryPurpleEntity.init(event);
		MahoragaEntity.init(event);
		FugaDomainExplosionEntity.init(event);
		HollowNukeEntity.init(event);
		SukunaEntity.init(event);
		GojoEntity.init(event);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(LAPSE_BLUE.get(), LapseBlueEntity.createAttributes().build());
		event.put(REVERSAL_RED.get(), ReversalRedEntity.createAttributes().build());
		event.put(HOLLOW_PURPLE_CHARGE.get(), HollowPurpleChargeEntity.createAttributes().build());
		event.put(HOLLOW_PURPLE_PROJECTILE.get(), HollowPurpleProjectileEntity.createAttributes().build());
		event.put(BF_ENTITY.get(), BFEntityEntity.createAttributes().build());
		event.put(MALEVOLENT_SHRINE.get(), MalevolentShrineEntity.createAttributes().build());
		event.put(DOMAIN_UV.get(), DomainUVEntity.createAttributes().build());
		event.put(FLAME_ARROW_EXPLOSION.get(), FlameArrowExplosionEntity.createAttributes().build());
		event.put(HOLLOW_PURPLE_BIG.get(), HollowPurpleBigEntity.createAttributes().build());
		event.put(IMAGINARY_PURPLE.get(), ImaginaryPurpleEntity.createAttributes().build());
		event.put(MAHORAGA.get(), MahoragaEntity.createAttributes().build());
		event.put(FUGA_DOMAIN_EXPLOSION.get(), FugaDomainExplosionEntity.createAttributes().build());
		event.put(HOLLOW_NUKE.get(), HollowNukeEntity.createAttributes().build());
		event.put(SUKUNA.get(), SukunaEntity.createAttributes().build());
		event.put(GOJO.get(), GojoEntity.createAttributes().build());
		event.put(DEBUG_BOT.get(), DebugBotEntity.createAttributes().build());
	}
}
