package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.entity.ReversalRedEntity;

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
