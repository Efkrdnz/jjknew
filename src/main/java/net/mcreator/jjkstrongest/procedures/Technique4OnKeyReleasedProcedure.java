package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import java.util.List;
import java.util.Comparator;

public class Technique4OnKeyReleasedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (false) {
			world.addParticle(ParticleTypes.ASH, 0, 0, 0, 0, 1, 0);
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("sukuna")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_wcs")) {
				if (new Object() {
					public boolean checkGamemode(Entity _ent) {
						if (_ent instanceof ServerPlayer _serverPlayer) {
							return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
						} else if (_ent.level().isClientSide() && _ent instanceof Player _player) {
							return Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
									&& Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode() == GameType.CREATIVE;
						}
						return false;
					}
				}.checkGamemode(entity) || (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_chant_progress == 3) {
					{
						double _setval = 100;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.wcs_power = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					{
						double _setval = entity.getX() + 125 * entity.getLookAngle().x;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.wcs_x2 = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					{
						double _setval = entity.getY() + entity.getBbHeight() + 125 * entity.getLookAngle().y;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.wcs_y2 = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					{
						double _setval = entity.getZ() + 125 * entity.getLookAngle().z;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.wcs_z2 = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					WorldSlashExecuteProcedure.execute(world, entity);
					{
						double _setval = 0;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.wcs_power = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					ReleaseArmAnimationProcedure.execute(entity);
					{
						double _setval = 0;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.wcs_chant_progress = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
				}
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_shrine")) {
				ReleaseArmAnimationProcedure.execute(entity);
				MalevolentShrineSummonProcedure.execute(entity.level(), entity);
				{
					double _setval = 1;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.domain_image_2 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:sukuna_domain_act")), SoundSource.PLAYERS, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:sukuna_domain_act")), SoundSource.PLAYERS, 1, 1, false);
					}
				}
				TriggerScreenShakeProcedure.execute((Level) world, entity, 5, 3.0f);
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
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("gojo")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_red")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red > 0) {
					entity.getPersistentData().putString("chanting", "");
					{
						double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red - 1;
						entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.charge_red = _setval;
							capability.syncPlayerVariables(entity);
						});
					}
					ReleaseArmAnimationProcedure.execute(entity);
					{
						final Vec3 _center = new Vec3(x, y, z);
						List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(25 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
						for (Entity entityiterator : _entfound) {
							if (!(entity == entityiterator)) {
								entityiterator.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), (float) (16 * entity.getPersistentData().getDouble("TechniquePower")));
								entityiterator.setDeltaMovement(new Vec3(
										((entityiterator.getX() - entity.getX()) * ((3 * entity.getPersistentData().getDouble("TechniquePower"))
												/ Math.sqrt(Math.pow(entityiterator.getX() - entity.getX(), 2) + Math.pow(entityiterator.getY() - entity.getY(), 2) + Math.pow(entityiterator.getZ() - entity.getZ(), 2)))),
										0.5, ((entityiterator.getZ() - entity.getZ()) * ((3 * entity.getPersistentData().getDouble("TechniquePower"))
												/ Math.sqrt(Math.pow(entityiterator.getX() - entity.getX(), 2) + Math.pow(entityiterator.getY() - entity.getY(), 2) + Math.pow(entityiterator.getZ() - entity.getZ(), 2))))));
							}
						}
					}
					if (world instanceof ServerLevel _level)
						_level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 100, 10, 10, 10, 1);
					if (world instanceof ServerLevel _level)
						_level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 100, 10, 10, 10, 1);
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 3, (float) 0.75);
						} else {
							_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 3, (float) 0.75, false);
						}
					}
					int horizontalRadiusSphere = (int) (8 * entity.getPersistentData().getDouble("TechniquePower")) - 1;
					int verticalRadiusSphere = (int) (8 * entity.getPersistentData().getDouble("TechniquePower")) - 1;
					int yIterationsSphere = verticalRadiusSphere;
					for (int i = -yIterationsSphere; i <= yIterationsSphere; i++) {
						for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
							for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
								double distanceSq = (xi * xi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere) + (i * i) / (double) (verticalRadiusSphere * verticalRadiusSphere)
										+ (zi * zi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere);
								if (distanceSq <= 1.0) {
									if (world instanceof ServerLevel _level)
										_level.getServer().getCommands().performPrefixedCommand(
												new CommandSourceStack(CommandSource.NULL, new Vec3(x + xi, y + i, z + zi), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
												"/particle dust 255 0 0 2 ~ ~ ~ 0 0 0 0 1 force");
								}
							}
						}
					}
					entity.getPersistentData().putDouble("TechniquePower", 0);
				}
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_limitless")) {
				ReleaseArmAnimationProcedure.execute(entity);
				{
					double _setval = 1;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.domain_image_1 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = 1;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.domain_image_2 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (!world.isClientSide()) {
					DomainExpansionStartProcedure.execute(entity.level(), x, y, z, entity, 0);
				}
			}
		}
	}
}
