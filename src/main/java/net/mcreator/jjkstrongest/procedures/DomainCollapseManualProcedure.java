package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.jjkstrongest.entity.DomainUVEntity;

public class DomainCollapseManualProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!(world instanceof ServerLevel serverLevel))
			return;
		// find domain entity near position
		AABB searchBox = new AABB(x - 50, y - 50, z - 50, x + 50, y + 50, z + 50);
		for (DomainUVEntity domainEntity : serverLevel.getEntitiesOfClass(DomainUVEntity.class, searchBox, e -> true)) {
			// trigger collapse by setting duration to 0
			domainEntity.getPersistentData().putInt("duration", 0);
			break; // only collapse nearest domain
		}
	}
}
