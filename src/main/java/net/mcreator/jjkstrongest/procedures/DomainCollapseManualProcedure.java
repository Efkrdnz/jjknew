package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import net.mcreator.jjkstrongest.entity.MalevolentShrineEntity;
import net.mcreator.jjkstrongest.entity.DomainUVEntity;

public class DomainCollapseManualProcedure {
	// check if entity has ANY active domain (Gojo OR Sukuna)
	public static boolean hasActiveDomain(LevelAccessor world, Entity entity) {
		if (!(world instanceof ServerLevel serverLevel))
			return false;
		if (entity == null)
			return false;
		String playerUUID = entity.getStringUUID();
		AABB searchBox = AABB.ofSize(entity.position(), 200, 200, 200);
		// check for Gojo's domain
		for (DomainUVEntity domainEntity : serverLevel.getEntitiesOfClass(DomainUVEntity.class, searchBox, e -> true)) {
			CompoundTag data = domainEntity.getPersistentData();
			if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(playerUUID)) {
				return true;
			}
		}
		// check for Sukuna's domain
		for (MalevolentShrineEntity shrineEntity : serverLevel.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> true)) {
			CompoundTag data = shrineEntity.getPersistentData();
			if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(playerUUID)) {
				return true;
			}
		}
		return false;
	}

	// collapse player's domain (either type)
	public static void collapsePlayerDomain(LevelAccessor world, Entity entity) {
		if (!(world instanceof ServerLevel serverLevel))
			return;
		if (entity == null)
			return;
		String playerUUID = entity.getStringUUID();
		AABB searchBox = AABB.ofSize(entity.position(), 200, 200, 200);
		// collapse Gojo's domain
		for (DomainUVEntity domainEntity : serverLevel.getEntitiesOfClass(DomainUVEntity.class, searchBox, e -> true)) {
			CompoundTag data = domainEntity.getPersistentData();
			if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(playerUUID)) {
				data.putInt("duration", 0);
				return; // found and collapsed
			}
		}
		// collapse Sukuna's domain
		for (MalevolentShrineEntity shrineEntity : serverLevel.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> true)) {
			CompoundTag data = shrineEntity.getPersistentData();
			if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(playerUUID)) {
				data.putInt("domainLifetimeTicks", 600); // trigger collapse
				return; // found and collapsed
			}
		}
	}

	// collapse domain at specific position
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!(world instanceof ServerLevel serverLevel))
			return;
		AABB searchBox = new AABB(x - 50, y - 50, z - 50, x + 50, y + 50, z + 50);
		// collapse Gojo's domain
		for (DomainUVEntity domainEntity : serverLevel.getEntitiesOfClass(DomainUVEntity.class, searchBox, e -> true)) {
			domainEntity.getPersistentData().putInt("duration", 0);
			return;
		}
		// collapse Sukuna's domain
		for (MalevolentShrineEntity shrineEntity : serverLevel.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> true)) {
			shrineEntity.getPersistentData().putInt("domainLifetimeTicks", 600);
			return;
		}
	}
}
