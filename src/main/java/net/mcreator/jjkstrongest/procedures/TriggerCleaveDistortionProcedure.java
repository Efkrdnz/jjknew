package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public class TriggerCleaveDistortionProcedure {
	public static void execute(Level world, Entity entity, int durationTicks, float intensity, int numSlashes) {
		if (entity == null || world == null)
			return;
		// Store trigger data in entity NBT
		entity.getPersistentData().putInt("cleave_distortion_ticks", durationTicks);
		entity.getPersistentData().putFloat("cleave_distortion_intensity", intensity);
		entity.getPersistentData().putInt("cleave_distortion_slashes", numSlashes);
		entity.getPersistentData().putLong("cleave_distortion_trigger", world.getGameTime());
		//System.out.println("[Cleave Distortion] Trigger data stored in NBT");
	}

	public static void execute(Level world, Entity entity) {
		execute(world, entity, 20, 1.0f, 10);
	}
}
