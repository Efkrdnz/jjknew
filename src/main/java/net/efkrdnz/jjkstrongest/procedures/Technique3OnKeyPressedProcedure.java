package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;
import net.efkrdnz.jjkstrongest.entity.LapseBlueEntity;
import net.efkrdnz.jjkstrongest.entity.HollowPurpleChargeEntity;
import net.efkrdnz.jjkstrongest.entity.HollowPurpleBigEntity;

import java.util.Comparator;

public class Technique3OnKeyPressedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("gojo")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_blue")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue >= 1) {
					ReleaseArmAnimationProcedure.execute(entity);
					{
						double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue - 1;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.charge_blue = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					if (world instanceof ServerLevel _serverLevel) {
						Entity entityinstance = JjkStrongestModEntities.LAPSE_BLUE.get().create(_serverLevel, null, null,
								BlockPos.containing(entity.getX() + (6 + 3 * entity.getPersistentData().getDouble("TechniquePower")) * entity.getLookAngle().x,
										entity.getY() + 1.6 + (5 + 3 * entity.getPersistentData().getDouble("TechniquePower")) * entity.getLookAngle().y,
										entity.getZ() + (6 + 3 * entity.getPersistentData().getDouble("TechniquePower")) * entity.getLookAngle().z),
								MobSpawnType.MOB_SUMMONED, false, false);
						if (entityinstance != null) {
							entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
							if (entityinstance instanceof TamableAnimal _toTame && entity instanceof Player _owner)
								_toTame.tame(_owner);
							entityinstance.getPersistentData().putString("caster", (entity.getDisplayName().getString()));
							entityinstance.getPersistentData().putDouble("TechniquePower", (entity.getPersistentData().getDouble("TechniquePower")));
							if (entity.getPersistentData().getDouble("TechniquePower") == 2) {
								if (!entity.isShiftKeyDown()) {
									entityinstance.getPersistentData().putBoolean("stay", true);
									if (entity instanceof Player _player && !_player.level().isClientSide())
										_player.displayClientMessage(Component.literal("\u00A79Amplification Blue"), false);
								} else {
									entityinstance.getPersistentData().putBoolean("stay", false);
									if (entity instanceof Player _player && !_player.level().isClientSide())
										_player.displayClientMessage(Component.literal("\u00A79Maximum Output Blue!"), false);
								}
							} else {
								entityinstance.getPersistentData().putBoolean("stay", true);
								if (entity instanceof Player _player && !_player.level().isClientSide())
									_player.displayClientMessage(Component.literal("\u00A79Amplification Blue"), false);
							}
							entity.getPersistentData().putDouble("TechniquePower", 0);
							_serverLevel.addFreshEntity(entityinstance);
						}
					}
					entity.getPersistentData().putString("chanting", "");
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5, false);
						}
					}
					if (!world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).isEmpty()) {
						if ((((Entity) world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).stream().sorted(new Object() {
							Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
								return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
							}
						}.compareDistOf(x, y, z)).findFirst().orElse(null)).getPersistentData().getString("caster")).equals("")) {
							((Entity) world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).stream().sorted(new Object() {
								Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
									return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
								}
							}.compareDistOf(x, y, z)).findFirst().orElse(null)).getPersistentData().putString("caster", (entity.getDisplayName().getString()));
							((Entity) world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).stream().sorted(new Object() {
								Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
									return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
								}
							}.compareDistOf(x, y, z)).findFirst().orElse(null)).getPersistentData().putDouble("TechniquePower", (entity.getPersistentData().getDouble("TechniquePower")));
							if (entity.getPersistentData().getDouble("TechniquePower") == 1) {
								if (!entity.isShiftKeyDown()) {
									((Entity) world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).stream().sorted(new Object() {
										Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
											return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
										}
									}.compareDistOf(x, y, z)).findFirst().orElse(null)).getPersistentData().putBoolean("stay", true);
									if (entity instanceof Player _player && !_player.level().isClientSide())
										_player.displayClientMessage(Component.literal("\u00A79Amplification Blue"), false);
								} else {
									((Entity) world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).stream().sorted(new Object() {
										Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
											return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
										}
									}.compareDistOf(x, y, z)).findFirst().orElse(null)).getPersistentData().putBoolean("stay", false);
									if (entity instanceof Player _player && !_player.level().isClientSide())
										_player.displayClientMessage(Component.literal("\u00A79Maximum Output Blue!"), false);
								}
							} else {
								((Entity) world.getEntitiesOfClass(LapseBlueEntity.class, AABB.ofSize(new Vec3(x, y, z), 40, 40, 40), e -> true).stream().sorted(new Object() {
									Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
										return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
									}
								}.compareDistOf(x, y, z)).findFirst().orElse(null)).getPersistentData().putBoolean("stay", true);
								if (entity instanceof Player _player && !_player.level().isClientSide())
									_player.displayClientMessage(Component.literal("\u00A79Amplification Blue"), false);
							}
							entity.getPersistentData().putDouble("TechniquePower", 0);
						}
					}
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_red")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red >= 1) {
					{
						double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red - 1;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.charge_red = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					ReleaseArmAnimationProcedure.execute(entity);
					if (world instanceof ServerLevel _serverLevel) {
						Entity entityinstance = JjkStrongestModEntities.REVERSAL_RED.get().create(_serverLevel, null, null,
								BlockPos.containing(entity.getX() + entity.getLookAngle().x * 1, entity.getY() + 1.6 + entity.getLookAngle().y * 1, entity.getZ() + entity.getLookAngle().z * 1), MobSpawnType.MOB_SUMMONED, false, false);
						if (entityinstance != null) {
							entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
							entityinstance.getPersistentData().putString("caster", (entity.getDisplayName().getString()));
							entityinstance.getPersistentData().putDouble("TechniquePower", (entity.getPersistentData().getDouble("TechniquePower")));
							entityinstance.getPersistentData().putString("state", "move");
							if (entityinstance instanceof TamableAnimal _toTame && entity instanceof Player _owner)
								_toTame.tame(_owner);
							entityinstance.setDeltaMovement(new Vec3((entity.getLookAngle().x * entityinstance.getPersistentData().getDouble("TechniquePower") * 1),
									(entity.getLookAngle().y * entityinstance.getPersistentData().getDouble("TechniquePower") * 1), (entity.getLookAngle().z * entityinstance.getPersistentData().getDouble("TechniquePower") * 1)));
							entityinstance.getPersistentData().putDouble("RedX", (entity.getLookAngle().x * entityinstance.getPersistentData().getDouble("TechniquePower") * 1));
							entityinstance.getPersistentData().putDouble("RedY", (entity.getLookAngle().y * entityinstance.getPersistentData().getDouble("TechniquePower") * 1));
							entityinstance.getPersistentData().putDouble("RedZ", (entity.getLookAngle().z * entityinstance.getPersistentData().getDouble("TechniquePower") * 1));
							_serverLevel.addFreshEntity(entityinstance);
						}
					}
					entity.getPersistentData().putDouble("TechniquePower", 0);
					entity.getPersistentData().putString("chanting", "");
					if (entity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal("\u00A74Reversal Red"), false);
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5, false);
						}
					}
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_purple")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple >= 3) {
					{
						double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple - 3;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.charge_purple = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					if (entity instanceof LivingEntity _livEnt100 && _livEnt100.hasEffect(JjkStrongestModMobEffects.PURPLE_CHARGING)) {
						if (!world.getEntitiesOfClass(HollowPurpleChargeEntity.class, AABB.ofSize(new Vec3(x, y, z), 100, 100, 100), e -> true).isEmpty()) {
							if (!((Entity) world.getEntitiesOfClass(HollowPurpleChargeEntity.class, AABB.ofSize(new Vec3(x, y, z), 100, 100, 100), e -> true).stream().sorted(new Object() {
								Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
									return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
								}
							}.compareDistOf(x, y, z)).findFirst().orElse(null)).level().isClientSide())
								((Entity) world.getEntitiesOfClass(HollowPurpleChargeEntity.class, AABB.ofSize(new Vec3(x, y, z), 100, 100, 100), e -> true).stream().sorted(new Object() {
									Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
										return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
									}
								}.compareDistOf(x, y, z)).findFirst().orElse(null)).discard();
							if (entity instanceof LivingEntity _entity)
								_entity.removeEffect(JjkStrongestModMobEffects.PURPLE_CHARGING);
						}
						if (entity instanceof Player _player && !_player.level().isClientSide())
							_player.displayClientMessage(Component.literal("dismissed"), false);
						entity.getPersistentData().putString("chanting", "");
						ReleaseArmAnimationProcedure.execute(entity);
					} else {
						ReleaseArmAnimationProcedure.execute(entity);
						if (world instanceof ServerLevel _serverLevel) {
							Entity entityinstance = JjkStrongestModEntities.HOLLOW_PURPLE_BIG.get().create(_serverLevel, null, null,
									BlockPos.containing(entity.getX() + entity.getLookAngle().x * 3, entity.getY() + 1.6 + entity.getLookAngle().y * 3, entity.getZ() + entity.getLookAngle().z * 3), MobSpawnType.MOB_SUMMONED, false, false);
							if (entityinstance != null) {
								entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
								entityinstance.getPersistentData().putDouble("TechniquePower", (entity.getPersistentData().getDouble("TechniquePower")));
								if (entityinstance instanceof HollowPurpleBigEntity _datEntSetI)
									_datEntSetI.getEntityData().set(HollowPurpleBigEntity.DATA_size10, (int) (10 * entity.getPersistentData().getDouble("TechniquePower")));
								entityinstance.getPersistentData().putString("caster", (entity.getDisplayName().getString()));
								_serverLevel.addFreshEntity(entityinstance);
							}
						}
						entity.getPersistentData().putDouble("TechniquePower", 0);
						if (world instanceof Level _level) {
							if (_level.isClientSide()) {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:hollowpurple")), SoundSource.NEUTRAL, (float) 0.3, 1, false);
							}
						}
						if (entity instanceof Player _player && !_player.level().isClientSide())
							_player.displayClientMessage(Component.literal("\u00A75Imaginary Technique: Purple"), false);
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5, false);
							}
						}
						entity.getPersistentData().putString("chanting", "");
					}
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_limitless")) {
				TeleportBlinkProcedure.execute(world, entity);
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("sukuna")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_dismantle")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).precision) {
					{
						double _setval = 0;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_power = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					{
						double _setval = entity.getX() + 25 * entity.getLookAngle().x;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_x2 = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					{
						double _setval = entity.getY() + entity.getBbHeight() + 25 * entity.getLookAngle().y;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_y2 = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					{
						double _setval = entity.getZ() + 25 * entity.getLookAngle().z;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_z2 = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					WorldSlashExecuteProcedure.execute(world, entity);
					ReleaseArmAnimationProcedure.execute(entity);
				} else {
					ShootDismantleNetProcedure.execute(entity.level(), entity, ReturnOutputDismantleProcedure.execute(entity.level(), entity), entity.getPersistentData().getDouble("TechniquePower"), true);
					ReleaseArmAnimationProcedure.execute(entity);
					entity.getPersistentData().putString("chanting", "");
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_fuga")) {
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
				if (DomainCollapseManualProcedure.hasActiveDomain(entity.level(), entity)) {
					FugaDomainExplosionExecuteProcedure.execute(entity.level(), entity);
				} else {
					FlameArrowShootExecuteProcedure.execute(entity);
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
				entity.getPersistentData().putString("chanting", "");
			}
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_x2 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_y2 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_z2 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_x1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_y1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_z1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("inumaki")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_assault")) {
				SpeechExecuteBurnProcedure.execute(entity);
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_control")) {
				SpeechExecuteFleeProcedure.execute(entity);
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_binding")) {
				SpeechExecuteShrinkProcedure.execute(entity);
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_utility")) {
				SpeechExecutePullProcedure.execute(entity);
			}
		}
	}
}
