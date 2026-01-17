package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.LapseBlueEntity;

public class LapseBlueOnInitialEntitySpawnProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LapseBlueEntity) {
			((LapseBlueEntity) entity).setAnimation("spawn");
		}
		entity.getPersistentData().putDouble("BlueLife", 0);
	}
}
