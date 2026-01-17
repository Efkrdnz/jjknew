package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.entity.DomainUVEntity;

public class DomainExpansionStartProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity caster, int domainType) {
		if (!(world instanceof ServerLevel serverLevel)) {
			if (caster instanceof Player player) {
				player.sendSystemMessage(Component.literal("§c[DEBUG] Not server level!"));
			}
			return;
		}
		if (caster == null)
			return;
		// spawn domain entity
		DomainUVEntity domainEntity = new DomainUVEntity(net.mcreator.jjkstrongest.init.JjkStrongestModEntities.DOMAIN_UV.get(), serverLevel);
		double domainY = y;
		domainEntity.setPos(x, domainY, z);
		// IMPORTANT: Make entity persistent
		domainEntity.setPersistenceRequired();
		domainEntity.setInvulnerable(true);
		// set domain data
		CompoundTag data = domainEntity.getPersistentData();
		data.putInt("domainType", domainType);
		data.putDouble("domainRadius", 30.0);
		data.putDouble("captureRadius", 35.0);
		data.putString("ownerUUID", caster.getStringUUID());
		data.putInt("duration", 600); // 30 seconds
		data.putInt("expansionTick", 0);
		data.putBoolean("isExpanding", true);
		data.putBoolean("isActive", false);
		data.put("storedBlocks", new CompoundTag());
		data.putInt("postTick", 0);
		data.putBoolean("isPostLines", false);
		data.putInt("wallDamageWindow", 0);
		data.putInt("wallBrokenCount", 0);
		data.putInt("repairCooldown", 0);
		boolean spawned = serverLevel.addFreshEntity(domainEntity);
		// debug message
		if (caster instanceof Player player) {
			if (spawned) {
				player.sendSystemMessage(Component.literal("§a[DEBUG] Domain entity spawned! UUID: " + domainEntity.getStringUUID()));
			} else {
				player.sendSystemMessage(Component.literal("§c[DEBUG] Failed to spawn domain entity!"));
			}
		}
		// capture entities
		captureEntities(serverLevel, x, domainY, z, 35.0);
		caster.teleportTo(caster.getX(), y, caster.getZ());
	}

	private static void captureEntities(Level level, double centerX, double centerY, double centerZ, double radius) {
		double minY = centerY - 15.0;
		double maxY = centerY + radius;
		// wide box so we never miss entities due to y bounds
		AABB searchBox = new AABB(centerX - radius, centerY - 64.0, centerZ - radius, centerX + radius, centerY + 64.0, centerZ + radius);
		Vec3 center = new Vec3(centerX, centerY, centerZ);
		double radiusSq = radius * radius;
		for (Entity entity : level.getEntitiesOfClass(Entity.class, searchBox, e -> shouldCapture(e))) {
			// hard vertical clamp
			double ey = entity.getY();
			if (ey < minY || ey > maxY)
				continue;
			// hard horizontal (true circle, not square)
			double dx = entity.getX() - centerX;
			double dz = entity.getZ() - centerZ;
			if ((dx * dx + dz * dz) > radiusSq)
				continue;
			// teleport onto the domain middle height (adjust as you want)
			entity.teleportTo(entity.getX(), centerY, entity.getZ());
			entity.resetFallDistance();
			if (entity instanceof LivingEntity living) {
				living.setDeltaMovement(living.getDeltaMovement().multiply(0.5, 0.0, 0.5));
			}
		}
	}

	private static Vec3 findSafeSpotForEntity(Level level, Entity entity, double centerX, double centerY, double centerZ, double maxRadius) {
		// try ring points around the center so everyone doesn’t stack
		int[] ring = new int[]{0, 2, -2, 4, -4, 6, -6, 8, -8, 10, -10};
		for (int ox : ring) {
			for (int oz : ring) {
				double x = centerX + ox;
				double z = centerZ + oz;
				// clamp into circle
				double dx = x - centerX;
				double dz = z - centerZ;
				double dist = Math.sqrt(dx * dx + dz * dz);
				if (dist > maxRadius && dist > 0.0001) {
					double s = maxRadius / dist;
					x = centerX + dx * s;
					z = centerZ + dz * s;
				}
				int bx = (int) Math.floor(x);
				int bz = (int) Math.floor(z);
				// terrain-safe base
				int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
				// prefer standing on your domain platform height if it’s not underground here
				int preferredFeetY = (int) Math.floor(centerY) + 1;
				int startY = Math.max(surfaceY + 1, preferredFeetY);
				// scan a bit upward to avoid trees/edges
				for (int dy = 0; dy <= 12; dy++) {
					int y = startY + dy;
					BlockPos feet = new BlockPos(bx, y, bz);
					if (isStandableAndFits(level, entity, feet)) {
						return new Vec3(bx + 0.5, y, bz + 0.5);
					}
				}
			}
		}
		// fallback: center column
		int cx = (int) Math.floor(centerX);
		int cz = (int) Math.floor(centerZ);
		int sy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
		for (int dy = 1; dy <= 20; dy++) {
			BlockPos feet = new BlockPos(cx, sy + dy, cz);
			if (isStandableAndFits(level, entity, feet)) {
				return new Vec3(cx + 0.5, feet.getY(), cz + 0.5);
			}
		}
		return null;
	}

	private static boolean isStandableAndFits(Level level, Entity entity, BlockPos feet) {
		BlockPos below = feet.below();
		// solid floor
		boolean floorOk = level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
		if (!floorOk)
			return false;
		// entity hitbox must fit here
		double tx = feet.getX() + 0.5;
		double ty = feet.getY();
		double tz = feet.getZ() + 0.5;
		AABB moved = entity.getBoundingBox().move(tx - entity.getX(), ty - entity.getY(), tz - entity.getZ());
		return level.noCollision(entity, moved);
	}

	private static boolean isSafeStandPos(Level level, BlockPos feet) {
		// feet and head must be empty collision, and block below must be solid enough to stand on
		BlockPos below = feet.below();
		boolean feetFree = level.getBlockState(feet).getCollisionShape(level, feet).isEmpty();
		boolean headFree = level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
		boolean hasFloor = !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
		return feetFree && headFree && hasFloor;
	}

	private static boolean isTwoBlocksFree(Level level, BlockPos feet) {
		return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty() && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
	}

	private static boolean shouldCapture(Entity entity) {
		if (entity instanceof Player player) {
			return !player.isCreative() && !player.isSpectator();
		}
		if (entity instanceof TamableAnimal tamed && tamed.isTame()) {
			return false;
		}
		if (entity instanceof Mob || entity instanceof LivingEntity) {
			return true;
		}
		return false;
	}
}
