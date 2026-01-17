package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

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

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;
import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.LapseBlueEntity;
import net.mcreator.jjkstrongest.entity.HollowPurpleProjectileEntity;
import net.mcreator.jjkstrongest.entity.HollowPurpleChargeEntity;

import java.util.Comparator;

public class Technique3OnKeyPressedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("gojo")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_blue")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue >= 1) {
					ReleaseArmAnimationProcedure.execute(entity);
					{
						double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue - 1;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.charge_blue = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					if (world instanceof ServerLevel _level) {
						Entity entityToSpawn = JjkStrongestModEntities.LAPSE_BLUE.get().spawn(_level,
								BlockPos.containing(entity.getX() + (6 + 3 * entity.getPersistentData().getDouble("TechniquePower")) * entity.getLookAngle().x,
										entity.getY() + 1.6 + (5 + 3 * entity.getPersistentData().getDouble("TechniquePower")) * entity.getLookAngle().y,
										entity.getZ() + (6 + 3 * entity.getPersistentData().getDouble("TechniquePower")) * entity.getLookAngle().z),
								MobSpawnType.MOB_SUMMONED);
						if (entityToSpawn != null) {
							entityToSpawn.setDeltaMovement(0, 0, 0);
						}
					}
					entity.getPersistentData().putString("chanting", "");
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5);
						} else {
							_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5, false);
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
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_red")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red >= 1) {
					{
						double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red - 1;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.charge_red = _setval;
							capability.syncPlayerVariables(entity);
						});
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
							_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5);
						} else {
							_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5, false);
						}
					}
				}
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_purple")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple >= 3) {
					{
						double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple - 3;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.charge_purple = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					if (entity instanceof LivingEntity _livEnt80 && _livEnt80.hasEffect(JjkStrongestModMobEffects.PURPLE_CHARGING.get())) {
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
								_entity.removeEffect(JjkStrongestModMobEffects.PURPLE_CHARGING.get());
						}
						if (entity instanceof Player _player && !_player.level().isClientSide())
							_player.displayClientMessage(Component.literal("dismissed"), false);
						entity.getPersistentData().putString("chanting", "");
					} else {
						ReleaseArmAnimationProcedure.execute(entity);
						if (world instanceof ServerLevel _serverLevel) {
							Entity entityinstance = JjkStrongestModEntities.HOLLOW_PURPLE_PROJECTILE.get().create(_serverLevel, null, null,
									BlockPos.containing(entity.getX() + entity.getLookAngle().x * 3, entity.getY() + 1.6 + entity.getLookAngle().y * 3, entity.getZ() + entity.getLookAngle().z * 3), MobSpawnType.MOB_SUMMONED, false, false);
							if (entityinstance != null) {
								entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
								entityinstance.getPersistentData().putDouble("TechniquePower", (entity.getPersistentData().getDouble("TechniquePower")));
								if (entityinstance instanceof HollowPurpleProjectileEntity _datEntSetI)
									_datEntSetI.getEntityData().set(HollowPurpleProjectileEntity.DATA_size10, (int) (10 * entity.getPersistentData().getDouble("TechniquePower")));
								entityinstance.getPersistentData().putString("caster", (entity.getDisplayName().getString()));
								_serverLevel.addFreshEntity(entityinstance);
							}
						}
						entity.getPersistentData().putDouble("TechniquePower", 0);
						if (world instanceof Level _level) {
							if (_level.isClientSide()) {
								_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:hollowpurple")), SoundSource.NEUTRAL, (float) 0.3, 1, false);
							}
						}
						if (entity instanceof Player _player && !_player.level().isClientSide())
							_player.displayClientMessage(Component.literal("\u00A75Imaginary Technique: Purple"), false);
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5);
							} else {
								_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1, (float) 0.5, false);
							}
						}
						entity.getPersistentData().putString("chanting", "");
					}
				}
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("sukuna")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_dismantle")) {
				{
					double _setval = 0;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_power = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = entity.getX() + 25 * entity.getLookAngle().x;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_x2 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = entity.getY() + entity.getBbHeight() + 25 * entity.getLookAngle().y;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_y2 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = entity.getZ() + 25 * entity.getLookAngle().z;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_z2 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				WorldSlashExecuteProcedure.execute(world, entity);
				ReleaseArmAnimationProcedure.execute(entity);
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_fuga")) {
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
				FlameArrowShootExecuteProcedure.execute(entity);
			}
			{
				double _setval = 0;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wcs_x2 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = 0;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wcs_y2 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = 0;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wcs_z2 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = 0;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wcs_x1 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = 0;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wcs_y1 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = 0;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.wcs_z1 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
	}
}
