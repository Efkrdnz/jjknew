package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.List;
import java.util.Comparator;

public class HollowPurpleProjectileOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double motionZ = 0;
		double deltaZ = 0;
		double deltaX = 0;
		double motionY = 0;
		double deltaY = 0;
		double motionX = 0;
		double speed = 0;
		entity.getPersistentData().putDouble("IA", (entity.getPersistentData().getDouble("IA") + 1));
		if ((entity.getPersistentData().getString("state")).equals("")) {
			{
				final Vec3 _center = new Vec3(x, y, z);
				List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(100 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
				for (Entity entityiterator : _entfound) {
					if ((entity.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString())) {
						if (!entity.isShiftKeyDown()) {
							{
								Entity _ent = entity;
								_ent.teleportTo((entityiterator.getX() + 5 * entityiterator.getLookAngle().x), (entityiterator.getY() + 1.6 + 5 * entityiterator.getLookAngle().y), (entityiterator.getZ() + 5 * entityiterator.getLookAngle().z));
								if (_ent instanceof ServerPlayer _serverPlayer)
									_serverPlayer.connection.teleport((entityiterator.getX() + 5 * entityiterator.getLookAngle().x), (entityiterator.getY() + 1.6 + 5 * entityiterator.getLookAngle().y),
											(entityiterator.getZ() + 5 * entityiterator.getLookAngle().z), _ent.getYRot(), _ent.getXRot());
							}
						} else {
							{
								Entity _ent = entity;
								_ent.teleportTo((entityiterator.getX() + 5 * entityiterator.getLookAngle().x), (entityiterator.getY() + 1.2 + 5 * entityiterator.getLookAngle().y), (entityiterator.getZ() + 5 * entityiterator.getLookAngle().z));
								if (_ent instanceof ServerPlayer _serverPlayer)
									_serverPlayer.connection.teleport((entityiterator.getX() + 5 * entityiterator.getLookAngle().x), (entityiterator.getY() + 1.2 + 5 * entityiterator.getLookAngle().y),
											(entityiterator.getZ() + 5 * entityiterator.getLookAngle().z), _ent.getYRot(), _ent.getXRot());
							}
						}
						entity.getPersistentData().putDouble("PurpleX", (3 * entityiterator.getLookAngle().x));
						entity.getPersistentData().putDouble("PurpleY", (3 * entityiterator.getLookAngle().y));
						entity.getPersistentData().putDouble("PurpleZ", (3 * entityiterator.getLookAngle().z));
					}
				}
			}
		}
		if (entity.getPersistentData().getDouble("IA") == 50) {
			entity.getPersistentData().putString("state", "move");
		}
		if ((entity.getPersistentData().getString("state")).equals("move")) {
			entity.getPersistentData().putDouble("PurpleLife", (entity.getPersistentData().getDouble("PurpleLife") + 1));
			entity.setDeltaMovement(new Vec3((entity.getPersistentData().getDouble("PurpleX")), (entity.getPersistentData().getDouble("PurpleY")), (entity.getPersistentData().getDouble("PurpleZ"))));
			deltaX = entity.getDeltaMovement().x();
			deltaY = entity.getDeltaMovement().y();
			deltaZ = entity.getDeltaMovement().z();
			world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.PURPLE_01.get()), (entity.getX()), (entity.getY()), (entity.getZ()), deltaX, deltaY, deltaZ);
			world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.PURPLE_02.get()), (entity.getX()), (entity.getY()), (entity.getZ()), deltaX, deltaY, deltaZ);
			if (entity.getPersistentData().getDouble("PurpleLife") >= 4 && entity.getPersistentData().getDouble("PurpleLife") % 2 == 0) {
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 1, 1, false);
					}
				}
				{
					final Vec3 _center = new Vec3(x, y, z);
					List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((3 * entity.getPersistentData().getDouble("TechniquePower")) / 2d), e -> true).stream()
							.sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
					for (Entity entityiterator : _entfound) {
						if (!(entity.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString()) && !(entity == entityiterator)) {
							entityiterator.getPersistentData().putBoolean("RedDrag", true);
						}
					}
				}
			}
			if (entity.getPersistentData().getDouble("PurpleLife") >= 4 && entity.getPersistentData().getDouble("PurpleLife") % 5 == 0) {
				if (world instanceof ServerLevel _level) {
					LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
					entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
					entityToSpawn.setVisualOnly(true);
					_level.addFreshEntity(entityToSpawn);
				}
			}
			if (entity.getPersistentData().getDouble("PurpleLife") >= 4) {
				int horizontalRadiusSphere = (int) (5 * entity.getPersistentData().getDouble("TechniquePower")) - 1;
				int verticalRadiusSphere = (int) (5 * entity.getPersistentData().getDouble("TechniquePower")) - 1;
				int yIterationsSphere = verticalRadiusSphere;
				for (int i = -yIterationsSphere; i <= yIterationsSphere; i++) {
					for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
						for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
							double distanceSq = (xi * xi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere) + (i * i) / (double) (verticalRadiusSphere * verticalRadiusSphere)
									+ (zi * zi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere);
							if (distanceSq <= 1.0) {
								if (world.getBlockState(BlockPos.containing(x + xi, y + i, z + zi)).getDestroySpeed(world, BlockPos.containing(x + xi, y + i, z + zi)) != -1) {
									world.setBlock(BlockPos.containing(x + xi, y + i, z + zi), Blocks.AIR.defaultBlockState(), 3);
								}
							}
						}
					}
				}
				if (world instanceof ServerLevel _level)
					_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
							("/particle dust_color_transition 0.94 0.05 0.87 1 0.38 0.01 0.42 ~ ~ ~ " + ("" + 3 * entity.getPersistentData().getDouble("TechniquePower")) + ("" + 3 * entity.getPersistentData().getDouble("TechniquePower"))
									+ ("" + 3 * entity.getPersistentData().getDouble("TechniquePower")) + " 0 12 force"));
			}
			if (entity.getPersistentData().getDouble("PurpleLife") >= 100) {
				if (!entity.level().isClientSide())
					entity.discard();
			}
		}
	}
}
