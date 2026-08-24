package net.efkrdnz.jjkstrongest.procedures;

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

import net.efkrdnz.jjkstrongest.init.JjkStrongestModBlocks;

import java.util.UUID;

public class DomainUVEntityTickProcedure {
	private static final int ABSOLUTE_MAX_LIFETIME = 1200;

	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || !(world instanceof ServerLevel serverLevel))
			return;
		CompoundTag data = entity.getPersistentData();
		if (!data.contains("domainType")) {
			entity.discard();
			return;
		}
		double radius = data.getDouble("domainRadius");
		double captureRadius = data.getDouble("captureRadius");
		int expansionTick = data.getInt("expansionTick");
		boolean isExpanding = data.getBoolean("isExpanding");
		boolean isActive = data.getBoolean("isActive");
		int duration = data.getInt("duration");
		boolean isClashing = data.getBoolean("isClashing");
		Vec3 center = entity.position();
		int absoluteTicks = data.getInt("domainAbsoluteTicks") + 1;
		data.putInt("domainAbsoluteTicks", absoluteTicks);
		if (absoluteTicks >= ABSOLUTE_MAX_LIFETIME) {
			collapseDomain(serverLevel, center, radius, data, entity);
			return;
		}
		// check caster validity
		if (isActive || isExpanding || data.getBoolean("isPostLines") || isClashing) {
			if (shouldCollapseDueToCaster(serverLevel, center, data, radius)) {
				collapseDomain(serverLevel, center, radius, data, entity);
				return;
			}
		}
		// run clash detection every tick when active or clashing
		if (isActive || isExpanding || data.getBoolean("isPostLines") || isClashing) {
			isClashing = DomainClashManagerProcedure.detectAndRunClash(serverLevel, entity);
			// re-read in case collapse was triggered inside detectAndRunClash
			if (!entity.isAlive())
				return;
		}
		// progressive expansion (unchanged)
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
		// active domain logic — frozen during clash
		if (isActive) {
			if (!isClashing) {
				// sure-hit only runs outside of clash
				UVDomainSureHitProcedure.execute(entity.level(), entity);
				pullEntities(serverLevel, center, captureRadius, radius);
				// duration only drains when not clashing
				duration--;
				data.putInt("duration", duration);
				if (duration <= 0) {
					collapseDomain(serverLevel, center, radius, data, entity);
				}
			}
			// pull still works during clash to keep entities from escaping the barrier
			if (isClashing) {
				pullEntities(serverLevel, center, captureRadius, radius);
			}
		}
	}

	private static boolean shouldCollapseDueToCaster(ServerLevel level, Vec3 center, CompoundTag data, double radius) {
		if (!data.contains("ownerUUID"))
			return false;
		try {
			UUID ownerUUID = UUID.fromString(data.getString("ownerUUID"));
			Entity caster = level.getEntity(ownerUUID);
			if (caster == null)
				return true;
			if (!caster.isAlive())
				return true;
			if (caster.position().distanceToSqr(center) > radius * radius)
				return true;
			if (caster instanceof Player player && player.isSpectator())
				return true;
		} catch (Exception e) {
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
			if (pos.getY() < platformY) {
				if (dist <= bottomRadius + eps)
					placeBarrierBlock(level, pos, storedBlocks);
				continue;
			}
			if (pos.getY() == platformY) {
				double flatDist = Math.sqrt(dx * dx + dz * dz);
				if (flatDist <= bottomRadius + eps)
					placeBarrierBlock(level, pos, storedBlocks);
				continue;
			}
			if (pos.getY() > platformY && topRadius > 0.0) {
				if (dist < innerTopRadius - eps) {
					setAirBlock(level, pos, storedBlocks);
					continue;
				}
				if (dist <= topRadius + eps && dist >= innerTopRadius - eps)
					placeBarrierBlock(level, pos, storedBlocks);
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
			if (be != null)
				blockData.put("blockEntity", be.saveWithoutMetadata(level.registryAccess()));
			storedBlocks.put(posKey, blockData);
		}
		if (level.getBlockEntity(pos) != null)
			level.removeBlockEntity(pos);
		level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
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
		if (be != null)
			blockData.put("blockEntity", be.saveWithoutMetadata(level.registryAccess()));
		storedBlocks.put(posKey, blockData);
		level.setBlock(pos, JjkStrongestModBlocks.DOMAIN_BARRIER.get().defaultBlockState(), 3);
	}

	private static void pullEntities(ServerLevel level, Vec3 center, double captureRadius, double barrierRadius) {
		AABB pullBox = new AABB(center.x - captureRadius, center.y - captureRadius, center.z - captureRadius, center.x + captureRadius, center.y + captureRadius, center.z + captureRadius);
		for (Entity entity : level.getEntitiesOfClass(Entity.class, pullBox, e -> e instanceof Player || e instanceof LivingEntity)) {
			if (entity instanceof Player player && (player.isCreative() || player.isSpectator()))
				continue;
			Vec3 entityPos = entity.position();
			double dist = Math.sqrt(entityPos.distanceToSqr(center));
			if (dist > barrierRadius - 2.0) {
				Vec3 direction = center.subtract(entityPos).normalize();
				entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(0.3)));
			}
		}
	}

	private static void collapseDomain(ServerLevel level, Vec3 center, double radius, CompoundTag entityData, Entity domainEntity) {
		CompoundTag storedBlocks = entityData.getCompound("storedBlocks");
		for (String posKey : storedBlocks.getAllKeys()) {
			String[] coords = posKey.split(",");
			BlockPos pos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			CompoundTag blockData = storedBlocks.getCompound(posKey);
			BlockState originalState = NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), blockData.getCompound("state"));
			level.setBlock(pos, originalState, 3);
			if (blockData.contains("blockEntity")) {
				BlockEntity be = level.getBlockEntity(pos);
				if (be != null)
					be.loadWithComponents(blockData.getCompound("blockEntity"), level.registryAccess());
			}
		}
		domainEntity.discard();
	}
}
