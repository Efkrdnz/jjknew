package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Comparator;

public class ImaginaryPurpleOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		// enable noclip movement
		entity.noPhysics = true;
		entity.setNoGravity(true);
		entity.getPersistentData().putDouble("IA", (entity.getPersistentData().getDouble("IA") + 1));
		entity.getPersistentData().putDouble("PurpleLife", (entity.getPersistentData().getDouble("PurpleLife") + 1));
		// store previous position
		double prevX = entity.getPersistentData().getDouble("PrevX");
		double prevY = entity.getPersistentData().getDouble("PrevY");
		double prevZ = entity.getPersistentData().getDouble("PrevZ");
		// faster speed - 2.5x multiplier
		double speedMultiplier = 5.0;
		entity.setDeltaMovement(new Vec3((entity.getPersistentData().getDouble("PurpleX") * speedMultiplier), (entity.getPersistentData().getDouble("PurpleY") * speedMultiplier), (entity.getPersistentData().getDouble("PurpleZ") * speedMultiplier)));
		// track entities already hit this tick to avoid double-hitting
		Set<Entity> hitEntities = new HashSet<>();
		// destroy blocks and damage entities along path
		if (entity.getPersistentData().getDouble("PurpleLife") >= 1) {
			// if we have previous position, interpolate between prev and current
			if (entity.getPersistentData().getDouble("PurpleLife") > 1) {
				double distance = Math.sqrt(Math.pow(x - prevX, 2) + Math.pow(y - prevY, 2) + Math.pow(z - prevZ, 2));
				// sample points along the path (every 0.5 blocks)
				int samples = (int) Math.ceil(distance / 0.5);
				samples = Math.max(1, samples);
				for (int sample = 0; sample <= samples; sample++) {
					double t = samples > 0 ? (double) sample / samples : 0;
					double interpX = prevX + (x - prevX) * t;
					double interpY = prevY + (y - prevY) * t;
					double interpZ = prevZ + (z - prevZ) * t;
					destroySphere(world, interpX, interpY, interpZ);
					damageEntities(world, interpX, interpY, interpZ, entity, hitEntities);
				}
			} else {
				// first tick, just destroy at current position
				destroySphere(world, x, y, z);
				damageEntities(world, x, y, z, entity, hitEntities);
			}
		}
		// store current position for next tick
		entity.getPersistentData().putDouble("PrevX", x);
		entity.getPersistentData().putDouble("PrevY", y);
		entity.getPersistentData().putDouble("PrevZ", z);
		if (entity.getPersistentData().getDouble("PurpleLife") >= 4 && entity.getPersistentData().getDouble("PurpleLife") % 2 == 0) {
			if (world instanceof Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 1, 1, false);
				}
			}
		}
		if (entity.getPersistentData().getDouble("PurpleLife") >= 100) {
			if (!entity.level().isClientSide())
				entity.discard();
		}
	}

	// destroy 2 radius sphere at given coordinates
	private static void destroySphere(LevelAccessor world, double x, double y, double z) {
		int horizontalRadiusSphere = 2;
		int verticalRadiusSphere = 2;
		int yIterationsSphere = verticalRadiusSphere;
		for (int i = -yIterationsSphere; i <= yIterationsSphere; i++) {
			for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
				for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
					double distanceSq = (xi * xi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere) + (i * i) / (double) (verticalRadiusSphere * verticalRadiusSphere)
							+ (zi * zi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere);
					if (distanceSq <= 1.0) {
						BlockPos pos = BlockPos.containing(x + xi, y + i, z + zi);
						if (world.getBlockState(pos).getDestroySpeed(world, pos) != -1) {
							if (world instanceof ServerLevel _level) {
								_level.getServer().getCommands().performPrefixedCommand(
										new CommandSourceStack(CommandSource.NULL, new Vec3(x + xi, y + i, z + zi), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), "setblock ~ ~ ~ air");
							}
						}
					}
				}
			}
		}
	}

	// damage entities at given coordinates
	private static void damageEntities(LevelAccessor world, double x, double y, double z, Entity projectile, Set<Entity> hitEntities) {
		final Vec3 _center = new Vec3(x, y, z);
		List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
		for (Entity entityiterator : _entfound) {
			// skip if already hit this tick, is the caster, or is the projectile itself
			if (!hitEntities.contains(entityiterator) && !(projectile.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString()) && !(projectile == entityiterator)) {
				entityiterator.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu")))), 50);
				hitEntities.add(entityiterator);
			}
		}
	}
}
