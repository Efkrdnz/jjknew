package net.mcreator.jjkstrongest.procedures;

import org.joml.Vector3f;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.List;
import java.util.Comparator;

public class ReversalRedOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putDouble("RedParticle", (entity.getPersistentData().getDouble("RedParticle") + 1));
		// improved particle trail
		if (world instanceof ServerLevel serverLevel) {
			DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 2.0F);
			serverLevel.sendParticles(redDust, x, y, z, 2, 0.1, 0.1, 0.1, 0.01);
		}
		// caster tracking state
		if ((entity.getPersistentData().getString("state")).equals("")) {
			final Vec3 center = new Vec3(x, y, z);
			List<Entity> entfound = world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(50), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList();
			for (Entity entityiterator : entfound) {
				if ((entity.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString())) {
					double offsetY = entity.isShiftKeyDown() ? 1.2 : 1.6;
					Vec3 lookVec = entityiterator.getLookAngle();
					Vec3 targetPos = new Vec3(entityiterator.getX() + lookVec.x, entityiterator.getY() + offsetY + lookVec.y, entityiterator.getZ() + lookVec.z);
					entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
					if (entity instanceof ServerPlayer serverPlayer) {
						serverPlayer.connection.teleport(targetPos.x, targetPos.y, targetPos.z, entity.getYRot(), entity.getXRot());
					}
					break;
				}
			}
		}
		// movement and collision state
		if ((entity.getPersistentData().getString("state")).equals("move")) {
			entity.getPersistentData().putDouble("RedLife", (entity.getPersistentData().getDouble("RedLife") + 1));
			double redLife = entity.getPersistentData().getDouble("RedLife");
			// lifetime check - explode after 4 seconds (80 ticks)
			if (redLife >= 80) {
				triggerExplosion(world, x, y, z, entity);
				return;
			}
			// block collision detection
			Vec3 currentPos = entity.position();
			Vec3 motion = new Vec3(entity.getPersistentData().getDouble("RedX"), entity.getPersistentData().getDouble("RedY"), entity.getPersistentData().getDouble("RedZ"));
			Vec3 nextPos = currentPos.add(motion);
			ClipContext clipContext = new ClipContext(currentPos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
			BlockHitResult blockHit = world.clip(clipContext);
			if (blockHit.getType() == HitResult.Type.BLOCK) {
				triggerExplosion(world, x, y, z, entity);
				return;
			}
			// optimized push force - destroy leaves while traveling (every 2 ticks)
			if (world instanceof ServerLevel _level && redLife % 2 == 0) {
				double pushRadius = 5.0 + (entity.getPersistentData().getDouble("TechniquePower") * 6.0);
				BlockPos centerPos = BlockPos.containing(x, y, z);
				int iRadius = (int) Math.ceil(pushRadius);
				int leavesDestroyed = 0;
				int maxLeaves = 300;
				// iterate through sphere checking only for leaves
				for (int dx = -iRadius; dx <= iRadius && leavesDestroyed < maxLeaves; dx++) {
					for (int dy = -iRadius; dy <= iRadius && leavesDestroyed < maxLeaves; dy++) {
						for (int dz = -iRadius; dz <= iRadius && leavesDestroyed < maxLeaves; dz++) {
							double distSq = dx * dx + dy * dy + dz * dz;
							double radiusSq = pushRadius * pushRadius;
							// optimized distance check using squared values
							if (distSq <= radiusSq) {
								BlockPos pos = centerPos.offset(dx, dy, dz);
								BlockState state = _level.getBlockState(pos);
								// only check leaf blocks
								if (state.getBlock() instanceof LeavesBlock) {
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
											"setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " air destroy");
									leavesDestroyed++;
								}
							}
						}
					}
				}
			}
			// periodic damage pulse
			if (redLife >= 2 && redLife % 2 == 0) {
				if (world instanceof Level level && !level.isClientSide()) {
					level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 0.5F, 1.2F);
				}
				double radius = 3 * entity.getPersistentData().getDouble("TechniquePower");
				final Vec3 center = new Vec3(x, y, z);
				List<Entity> targets = world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius / 2d), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList();
				for (Entity target : targets) {
					if (!entity.getPersistentData().getString("caster").equals(target.getDisplayName().getString()) && target != entity) {
						target.getPersistentData().putBoolean("RedDrag", true);
					}
				}
			}
			// apply movement
			if (redLife >= 2) {
				entity.setDeltaMovement(motion);
			}
			// red 1 and red 2 particles with forced rendering
			if (world instanceof ServerLevel serverLevel) {
				if (world.getLevelData().getGameTime() % 5 == 0) {
					for (ServerPlayer player : serverLevel.players()) {
						if (player.distanceToSqr(entity) < 10000) {
							player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket((SimpleParticleType) JjkStrongestModParticleTypes.RED_01.get(), true, entity.getX(), entity.getY(), entity.getZ(),
									(float) entity.getDeltaMovement().x(), (float) entity.getDeltaMovement().y(), (float) entity.getDeltaMovement().z(), 1.0f, 0));
						}
					}
				}
				if (world.getLevelData().getGameTime() % 3 == 0) {
					for (ServerPlayer player : serverLevel.players()) {
						if (player.distanceToSqr(entity) < 10000) {
							player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket((SimpleParticleType) JjkStrongestModParticleTypes.RED_02.get(), true, entity.getX(), entity.getY(), entity.getZ(),
									(float) entity.getDeltaMovement().x(), (float) entity.getDeltaMovement().y(), (float) entity.getDeltaMovement().z(), 1.0f, 0));
						}
					}
					for (ServerPlayer player : serverLevel.players()) {
						if (player.distanceToSqr(entity) < 10000) {
							player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket((SimpleParticleType) JjkStrongestModParticleTypes.RED_03.get(), true, entity.getX(), entity.getY(), entity.getZ(),
									(float) entity.getDeltaMovement().x(), (float) entity.getDeltaMovement().y(), (float) entity.getDeltaMovement().z(), 1.0f, 0));
						}
					}
				}
			}
		}
	}

	// custom spherical explosion scaled by technique power
	private static void triggerExplosion(LevelAccessor world, double x, double y, double z, Entity entity) {
		double techniquePower = entity.getPersistentData().getDouble("TechniquePower");
		String casterName = entity.getPersistentData().getString("caster");
		// calculate sphere radius - 10 blocks at full power
		double sphereRadius = 3.0 + (techniquePower * 7.0);
		// explosion particles
		if (world instanceof ServerLevel serverLevel) {
			DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 3.0F);
			serverLevel.sendParticles(redDust, x, y, z, 200, sphereRadius * 0.5, sphereRadius * 0.5, sphereRadius * 0.5, 0.2);
			serverLevel.sendParticles((SimpleParticleType) JjkStrongestModParticleTypes.RED_01.get(), x, y, z, 100, sphereRadius * 0.6, sphereRadius * 0.6, sphereRadius * 0.6, 0.3);
			serverLevel.sendParticles((SimpleParticleType) JjkStrongestModParticleTypes.RED_02.get(), x, y, z, 80, sphereRadius * 0.5, sphereRadius * 0.5, sphereRadius * 0.5, 0.25);
			serverLevel.sendParticles((SimpleParticleType) JjkStrongestModParticleTypes.RED_03.get(), x, y, z, 60, sphereRadius * 0.4, sphereRadius * 0.4, sphereRadius * 0.4, 0.2);
			serverLevel.sendParticles((SimpleParticleType) (JjkStrongestModParticleTypes.EXPLOSION_CUSTOM.get()), x, y, z, 10, sphereRadius * 0.2, sphereRadius * 0.2, sphereRadius * 0.2, 1);
		}
		// explosion sound
		if (world instanceof Level level && !level.isClientSide()) {
			level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation("entity.generic.explode")), SoundSource.HOSTILE, 4.0F, 0.6F);
		}
		// find caster entity for damage source
		Entity caster = null;
		final Vec3 searchCenter = new Vec3(x, y, z);
		List<Entity> nearbyEntities = world.getEntitiesOfClass(Entity.class, new AABB(searchCenter, searchCenter).inflate(100), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(searchCenter))).toList();
		for (Entity potentialCaster : nearbyEntities) {
			if (casterName.equals(potentialCaster.getDisplayName().getString())) {
				caster = potentialCaster;
				break;
			}
		}
		// damage calculation
		double baseDamage = 30.0 + (techniquePower * 10.0);
		final Vec3 explosionCenter = new Vec3(x, y, z);
		List<Entity> targets = world.getEntitiesOfClass(Entity.class, new AABB(explosionCenter, explosionCenter).inflate(sphereRadius), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(explosionCenter))).toList();
		final Entity finalCaster = caster;
		for (Entity target : targets) {
			if (target instanceof LivingEntity && !casterName.equals(target.getDisplayName().getString()) && target != entity) {
				double distance = target.distanceTo(entity);
				double damageFalloff = Math.max(0.3, 1.0 - (distance / sphereRadius));
				float finalDamage = (float) (baseDamage * damageFalloff);
				// create damage source with caster attribution
				DamageSource damageSource;
				if (finalCaster != null) {
					damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), finalCaster);
				} else {
					damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC));
				}
				target.hurt(damageSource, finalDamage);
				// knockback effect
				Vec3 knockbackDir = target.position().subtract(explosionCenter).normalize();
				double knockbackStrength = (1.5 + techniquePower * 0.5) * damageFalloff;
				target.setDeltaMovement(target.getDeltaMovement().add(knockbackDir.x * knockbackStrength, knockbackDir.y * knockbackStrength * 0.7, knockbackDir.z * knockbackStrength));
			}
		}
		// perfect spherical block destruction
		if (world instanceof ServerLevel _level) {
			BlockPos centerPos = BlockPos.containing(x, y, z);
			int iRadius = (int) Math.ceil(sphereRadius);
			for (int dx = -iRadius; dx <= iRadius; dx++) {
				for (int dy = -iRadius; dy <= iRadius; dy++) {
					for (int dz = -iRadius; dz <= iRadius; dz++) {
						BlockPos pos = centerPos.offset(dx, dy, dz);
						// calculate exact distance from center
						double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
						// only destroy if within sphere radius
						if (distance <= sphereRadius) {
							BlockState state = _level.getBlockState(pos);
							if (state.getDestroySpeed(_level, pos) != -1 && !state.isAir()) {
								// destroy block using command
								_level.getServer().getCommands().performPrefixedCommand(
										new CommandSourceStack(CommandSource.NULL, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
										"setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " air replace");
							}
						}
					}
				}
			}
		}
		// remove projectile entity
		if (!entity.level().isClientSide()) {
			entity.discard();
		}
	}
}
