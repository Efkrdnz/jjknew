package net.efkrdnz.jjkstrongest.procedures;

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
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.List;
import java.util.Comparator;

public class ReversalRedOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putDouble("RedParticle", (entity.getPersistentData().getDouble("RedParticle") + 1));
		// trail dust particle
		if (world instanceof ServerLevel serverLevel) {
			DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 2.0F);
			serverLevel.sendParticles(redDust, x, y, z, 2, 0.1, 0.1, 0.1, 0.01);
		}
		// caster tracking before launch
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
			double techniquePower = entity.getPersistentData().getDouble("TechniquePower");
			boolean isMaxCharge = techniquePower >= 2.0;
			// lifetime check
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
			// leaf destruction every 2 ticks
			if (world instanceof ServerLevel _level && redLife % 2 == 0) {
				double pushRadius = 5.0 + (techniquePower * 6.0);
				BlockPos centerPos = BlockPos.containing(x, y, z);
				int iRadius = (int) Math.ceil(pushRadius);
				int leavesDestroyed = 0;
				int maxLeaves = 300;
				for (int dx = -iRadius; dx <= iRadius && leavesDestroyed < maxLeaves; dx++) {
					for (int dy = -iRadius; dy <= iRadius && leavesDestroyed < maxLeaves; dy++) {
						for (int dz = -iRadius; dz <= iRadius && leavesDestroyed < maxLeaves; dz++) {
							double distSq = dx * dx + dy * dy + dz * dz;
							if (distSq <= pushRadius * pushRadius) {
								BlockPos pos = centerPos.offset(dx, dy, dz);
								BlockState state = _level.getBlockState(pos);
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
			if (isMaxCharge) {
				// max charge: repulsion aura with per-entity uuid hit tracking
				if (redLife % 2 == 0 && world instanceof Level level && !level.isClientSide()) {
					double auraRadius = 4.0;
					String casterName = entity.getPersistentData().getString("caster");
					final Vec3 auraCenter = new Vec3(x, y, z);
					// load already-hit uuid list from nbt
					ListTag hitList = entity.getPersistentData().getList("RedHitUUIDs", 8);
					List<Entity> nearbyTargets = world.getEntitiesOfClass(Entity.class, new AABB(auraCenter, auraCenter).inflate(auraRadius), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(auraCenter))).toList();
					for (Entity target : nearbyTargets) {
						if (target == entity)
							continue;
						if (casterName.equals(target.getDisplayName().getString()))
							continue;
						if (target.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique"))))
							continue;
						if (!(target instanceof LivingEntity))
							continue;
						String targetUUID = target.getStringUUID();
						// skip if already hit by aura
						boolean alreadyHit = false;
						for (int i = 0; i < hitList.size(); i++) {
							if (hitList.getString(i).equals(targetUUID)) {
								alreadyHit = true;
								break;
							}
						}
						if (alreadyHit)
							continue;
						// find caster entity for damage source attribution
						Entity caster = null;
						List<Entity> casterSearch = world.getEntitiesOfClass(Entity.class, new AABB(auraCenter, auraCenter).inflate(100), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(auraCenter))).toList();
						for (Entity potential : casterSearch) {
							if (casterName.equals(potential.getDisplayName().getString())) {
								caster = potential;
								break;
							}
						}
						// apply damage
						DamageSource auraSource;
						if (caster != null) {
							auraSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_red"))), caster);
						} else {
							auraSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC));
						}
						target.invulnerableTime = 0;
						target.hurt(auraSource, (float) (4.0 * techniquePower));
						// repel target away from orb
						Vec3 repelDir = target.position().subtract(auraCenter).normalize();
						double repelStrength = 1.2 * techniquePower;
						target.setDeltaMovement(target.getDeltaMovement().add(repelDir.x * repelStrength, repelDir.y * repelStrength * 0.6 + 0.3, repelDir.z * repelStrength));
						// save uuid to hit list
						hitList.add(StringTag.valueOf(targetUUID));
					}
					// write updated hit list back
					entity.getPersistentData().put("RedHitUUIDs", hitList);
				}
			} else {
				// below max charge: original drag + periodic damage pulse
				if (redLife >= 2 && redLife % 2 == 0) {
					if (world instanceof Level level && !level.isClientSide()) {
						level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.explode")), SoundSource.NEUTRAL, 0.5F, 1.2F);
					}
					double radius = 3 * techniquePower;
					final Vec3 center = new Vec3(x, y, z);
					List<Entity> targets = world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius / 2d), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList();
					for (Entity target : targets) {
						if (!entity.getPersistentData().getString("caster").equals(target.getDisplayName().getString()) && target != entity && !target.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique")))) {
							target.getPersistentData().putBoolean("RedDrag", true);
						}
					}
				}
			}
			// apply movement
			if (redLife >= 2) {
				entity.setDeltaMovement(motion);
			}
			// particles
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
							player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket((SimpleParticleType) JjkStrongestModParticleTypes.RED_03.get(), true, entity.getX(), entity.getY(), entity.getZ(),
									(float) entity.getDeltaMovement().x(), (float) entity.getDeltaMovement().y(), (float) entity.getDeltaMovement().z(), 1.0f, 0));
						}
					}
				}
			}
		}
	}

	// spherical explosion scaled by technique power
	private static void triggerExplosion(LevelAccessor world, double x, double y, double z, Entity entity) {
		double techniquePower = entity.getPersistentData().getDouble("TechniquePower");
		String casterName = entity.getPersistentData().getString("caster");
		boolean isMaxCharge = techniquePower >= 2.0;
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
			float volume = isMaxCharge ? 6.0F : 4.0F;
			float pitch = isMaxCharge ? 0.5F : 0.6F;
			level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.explode")), SoundSource.HOSTILE, volume, pitch);
		}
		// find caster
		Entity caster = null;
		final Vec3 searchCenter = new Vec3(x, y, z);
		List<Entity> nearbyEntities = world.getEntitiesOfClass(Entity.class, new AABB(searchCenter, searchCenter).inflate(100), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(searchCenter))).toList();
		for (Entity potential : nearbyEntities) {
			if (casterName.equals(potential.getDisplayName().getString())) {
				caster = potential;
				break;
			}
		}
		// damage and knockback
		double baseDamage = isMaxCharge ? 25.0 + (techniquePower * 10.0) : 30.0 + (techniquePower * 5.0);
		final Vec3 explosionCenter = new Vec3(x, y, z);
		final Entity finalCaster = caster;
		List<Entity> targets = world.getEntitiesOfClass(Entity.class, new AABB(explosionCenter, explosionCenter).inflate(sphereRadius), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(explosionCenter))).toList();
		for (Entity target : targets) {
			if (target instanceof LivingEntity && !casterName.equals(target.getDisplayName().getString()) && target != entity) {
				double distance = target.distanceTo(entity);
				double damageFalloff = Math.max(0.3, 1.0 - (distance / sphereRadius));
				float finalDamage = (float) (baseDamage * damageFalloff);
				DamageSource damageSource;
				if (finalCaster != null) {
					damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_red"))), finalCaster);
				} else {
					damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC));
				}
				target.invulnerableTime = 0;
				target.hurt(damageSource, finalDamage);
				// always repel outward regardless of charge level
				Vec3 knockbackDir = target.position().subtract(explosionCenter).normalize();
				double knockbackStrength = isMaxCharge ? (3.0 + techniquePower * 1.5) * damageFalloff : (1.5 + techniquePower * 0.5) * damageFalloff;
				target.setDeltaMovement(target.getDeltaMovement().add(knockbackDir.x * knockbackStrength, knockbackDir.y * knockbackStrength * 0.7 + (isMaxCharge ? 0.5 : 0.0), knockbackDir.z * knockbackStrength));
			}
		}
		// block destruction
		if (world instanceof ServerLevel _level) {
			BlockPos centerPos = BlockPos.containing(x, y, z);
			int iRadius = (int) Math.ceil(sphereRadius);
			for (int dx = -iRadius; dx <= iRadius; dx++) {
				for (int dy = -iRadius; dy <= iRadius; dy++) {
					for (int dz = -iRadius; dz <= iRadius; dz++) {
						BlockPos pos = centerPos.offset(dx, dy, dz);
						double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
						if (distance <= sphereRadius) {
							BlockState state = _level.getBlockState(pos);
							if (state.getDestroySpeed(_level, pos) != -1 && !state.isAir()) {
								_level.getServer().getCommands().performPrefixedCommand(
										new CommandSourceStack(CommandSource.NULL, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
										"setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " air replace");
							}
						}
					}
				}
			}
		}
		// remove orb entity
		if (!entity.level().isClientSide()) {
			entity.discard();
		}
	}
}
