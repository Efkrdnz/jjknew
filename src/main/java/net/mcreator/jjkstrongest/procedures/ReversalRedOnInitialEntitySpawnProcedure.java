package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.ReversalRedEntity;

public class ReversalRedOnInitialEntitySpawnProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ReversalRedEntity) {
			((ReversalRedEntity) entity).setAnimation("spawn");
		}
		entity.getPersistentData().putDouble("RedLife", 0);
	}
}
