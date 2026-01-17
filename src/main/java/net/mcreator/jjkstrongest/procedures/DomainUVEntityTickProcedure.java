package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.init.JjkStrongestModBlocks;

import java.util.UUID;

public class DomainUVEntityTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || !(world instanceof ServerLevel serverLevel))
			return;
		CompoundTag data = entity.getPersistentData();
		if (!data.contains("domainType")) {
			// debug 1
			//System.out.println("[DEBUG] Domain entity has no data! Removing...");
			entity.discard();
			return;
		}
		double radius = data.getDouble("domainRadius");
		double captureRadius = data.getDouble("captureRadius");
		int expansionTick = data.getInt("expansionTick");
		boolean isExpanding = data.getBoolean("isExpanding");
		boolean isActive = data.getBoolean("isActive");
		int duration = data.getInt("duration");
		Vec3 center = entity.position();
		// check if caster conditions
		if (isActive || isExpanding) {
			if (shouldCollapseDueToCaster(serverLevel, center, data, radius)) {
				collapseDomain(serverLevel, center, radius, data, entity);
				return;
			}
		}
		// debug 2
		if (entity.tickCount % 20 == 0) {
			//System.out.println("[DEBUG] Domain tick: " + entity.tickCount + " | Expansion: " + data.getInt("expansionTick") + " | Active: " + data.getBoolean("isActive") + " | Duration: " + data.getInt("duration"));
		}
		// progressive expansion - 40 ticks (2 seconds)
		// Ticks 0-20: Bottom hemisphere (platform)
		// Ticks 21-40: Top hemisphere (roof)
		if (isExpanding && expansionTick < 40) {
			expandDomainProgressive(serverLevel, center, radius, expansionTick, data);
			data.putInt("expansionTick", expansionTick + 1);
			if (expansionTick >= 39) {
				data.putBoolean("isExpanding", false);
				data.putBoolean("isPostLines", true);
				data.putInt("postTick", 0);
			}
		}
		if (data.getBoolean("isPostLines")) {
			DomainUVPostLinesPhaseProcedure.execute(world, entity);
		}
		// active domain
		if (isActive) {
			pullEntities(serverLevel, center, captureRadius, radius);
			duration--;
			data.putInt("duration", duration);
			if (duration <= 0) {
				collapseDomain(serverLevel, center, radius, data, entity);
			}
		}
	}

	private static boolean shouldCollapseDueToCaster(ServerLevel level, Vec3 center, CompoundTag data, double radius) {
		if (!data.contains("ownerUUID")) {
			//System.out.println("[DEBUG] No ownerUUID found!");
			return false; // Changed to false - don't collapse if no owner
		}
		try {
			UUID ownerUUID = UUID.fromString(data.getString("ownerUUID"));
			Entity caster = level.getEntity(ownerUUID);
			if (caster == null) {
				//System.out.println("[DEBUG] Caster is NULL - collapsing!");
				return true;
			}
			if (!caster.isAlive()) {
				//System.out.println("[DEBUG] Caster is dead - collapsing!");
				return true;
			}
			double distSq = caster.position().distanceToSqr(center);
			double dist = Math.sqrt(distSq);
			//System.out.println("[DEBUG] Caster distance: " + dist + " (max: " + radius + ")");
			if (distSq > radius * radius) {
				//System.out.println("[DEBUG] Caster too far - collapsing!");
				return true;
			}
			if (caster instanceof Player player && (player.isSpectator())) {
				//System.out.println("[DEBUG] Caster is creative/spectator - collapsing!");
				return true;
			}
			//System.out.println("[DEBUG] Caster check passed - NOT collapsing");
		} catch (Exception e) {
			//System.out.println("[DEBUG] Exception in caster check: " + e.getMessage());
			return true;
		}
		return false;
	}

	private static void expandDomainProgressive(ServerLevel level, Vec3 center, double maxRadius, int tick, CompoundTag entityData) {
		BlockPos centerPos = BlockPos.containing(center.x, center.y, center.z);
		CompoundTag storedBlocks = entityData.getCompound("storedBlocks");
		double wallThickness = 1.6;
		double eps = 0.55;
		int platformY = centerPos.getY() - 1;
		// match radii: bottom grows to full in 20 ticks, top grows to full in next 20
		double bottomProgress = Math.min(1.0, (tick + 1) / 20.0);
		double bottomRadius = maxRadius * bottomProgress;
		double topProgress = tick < 21 ? 0.0 : Math.min(1.0, (tick - 20) / 20.0);
		double topRadius = maxRadius * topProgress;
		double effectiveRadius = Math.max(bottomRadius, topRadius);
		int searchRadius = (int) Math.ceil(effectiveRadius + wallThickness + 2);
		double innerTopRadius = Math.max(0, topRadius - wallThickness);
		for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-searchRadius, -searchRadius, -searchRadius), centerPos.offset(searchRadius, searchRadius, searchRadius))) {
			double dx = pos.getX() - centerPos.getX();
			double dy = pos.getY() - centerPos.getY();
			double dz = pos.getZ() - centerPos.getZ();
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			// 1) bottom hemisphere: solid fill
			if (pos.getY() < platformY) {
				if (dist <= bottomRadius + eps) {
					placeBarrierBlock(level, pos, storedBlocks);
				}
				continue;
			}
			// 2) middle platform disc
			if (pos.getY() == platformY) {
				double flatDist = Math.sqrt(dx * dx + dz * dz);
				if (flatDist <= bottomRadius + eps) {
					placeBarrierBlock(level, pos, storedBlocks);
				}
				continue;
			}
			// 3) upper hemisphere: clear interior to air, shell to barrier
			if (pos.getY() > platformY && topRadius > 0.0) {
				// clear anything inside (so no leftover terrain blocks)
				if (dist < innerTopRadius - eps) {
					setAirBlock(level, pos, storedBlocks);
					continue;
				}
				// shell wall
				if (dist <= topRadius + eps && dist >= innerTopRadius - eps) {
					placeBarrierBlock(level, pos, storedBlocks);
				}
			}
		}
		entityData.put("storedBlocks", storedBlocks);
	}

	private static void setAirBlock(ServerLevel level, BlockPos pos, CompoundTag storedBlocks) {
		BlockState currentState = level.getBlockState(pos);
		if (currentState.is(net.minecraft.world.level.block.Blocks.BEDROCK) || currentState.is(net.minecraft.world.level.block.Blocks.AIR))
			return;
		String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();
		if (!storedBlocks.contains(posKey)) {
			CompoundTag blockData = new CompoundTag();
			blockData.put("state", NbtUtils.writeBlockState(currentState));
			BlockEntity be = level.getBlockEntity(pos);
			if (be != null) {
				blockData.put("blockEntity", be.saveWithoutMetadata());
			}
			storedBlocks.put(posKey, blockData);
		}
		// remove block entity if any
		if (level.getBlockEntity(pos) != null) {
			level.removeBlockEntity(pos);
		}
		level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
	}

	private static void restoreIfDomainBarrier(ServerLevel level, BlockPos pos, CompoundTag storedBlocks) {
		if (!level.getBlockState(pos).is(JjkStrongestModBlocks.DOMAIN_BARRIER.get()))
			return;
		String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();
		if (!storedBlocks.contains(posKey)) {
			level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
			return;
		}
		CompoundTag blockData = storedBlocks.getCompound(posKey);
		BlockState originalState = NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), blockData.getCompound("state"));
		level.setBlock(pos, originalState, 3);
		if (blockData.contains("blockEntity")) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be != null) {
				be.load(blockData.getCompound("blockEntity"));
			}
		}
		storedBlocks.remove(posKey);
	}

	private static void placeBarrierBlock(ServerLevel level, BlockPos pos, CompoundTag storedBlocks) {
		BlockState currentState = level.getBlockState(pos);
		if (currentState.is(net.minecraft.world.level.block.Blocks.BEDROCK) || currentState.is(JjkStrongestModBlocks.DOMAIN_BARRIER.get()))
			return;
		String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();
		if (storedBlocks.contains(posKey))
			return;
		CompoundTag blockData = new CompoundTag();
		blockData.put("state", NbtUtils.writeBlockState(currentState));
		BlockEntity be = level.getBlockEntity(pos);
		if (be != null) {
			blockData.put("blockEntity", be.saveWithoutMetadata());
		}
		storedBlocks.put(posKey, blockData);
		level.setBlock(pos, JjkStrongestModBlocks.DOMAIN_BARRIER.get().defaultBlockState(), 3);
	}

	private static void pullEntities(ServerLevel level, Vec3 center, double captureRadius, double barrierRadius) {
		AABB pullBox = new AABB(center.x - captureRadius, center.y - captureRadius, center.z - captureRadius, center.x + captureRadius, center.y + captureRadius, center.z + captureRadius);
		for (Entity entity : level.getEntitiesOfClass(Entity.class, pullBox, e -> e instanceof Player || e instanceof LivingEntity)) {
			Vec3 entityPos = entity.position();
			double distSq = entityPos.distanceToSqr(center);
			double dist = Math.sqrt(distSq);
			// pull entities trying to escape
			if (dist > barrierRadius - 2.0) {
				Vec3 direction = center.subtract(entityPos).normalize();
				Vec3 pullForce = direction.scale(0.3);
				entity.setDeltaMovement(entity.getDeltaMovement().add(pullForce));
			}
		}
	}

	private static void collapseDomain(ServerLevel level, Vec3 center, double radius, CompoundTag entityData, Entity domainEntity) {
		BlockPos centerPos = new BlockPos((int) center.x, (int) center.y, (int) center.z);
		CompoundTag storedBlocks = entityData.getCompound("storedBlocks");
		// restore all blocks
		for (String posKey : storedBlocks.getAllKeys()) {
			String[] coords = posKey.split(",");
			BlockPos pos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			CompoundTag blockData = storedBlocks.getCompound(posKey);
			BlockState originalState = NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), blockData.getCompound("state"));
			// restore block
			level.setBlock(pos, originalState, 3);
			// restore block entity
			if (blockData.contains("blockEntity")) {
				BlockEntity be = level.getBlockEntity(pos);
				if (be != null) {
					be.load(blockData.getCompound("blockEntity"));
				}
			}
		}
		// remove entity
		domainEntity.discard();
	}
}
