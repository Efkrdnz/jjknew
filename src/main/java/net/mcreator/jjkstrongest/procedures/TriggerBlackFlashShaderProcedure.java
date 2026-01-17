package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public class TriggerBlackFlashShaderProcedure {
	// trigger with custom parameters
	public static void execute(Level world, Entity entity, int durationTicks, float intensity) {
		if (entity == null || world == null)
			return;
		// store trigger data in entity NBT for client sync
		entity.getPersistentData().putInt("blackflash_shader_ticks", durationTicks);
		entity.getPersistentData().putFloat("blackflash_shader_intensity", intensity);
		entity.getPersistentData().putLong("blackflash_shader_trigger", world.getGameTime());
	}

	// trigger with default parameters (10 ticks, full intensity)
	public static void execute(Level world, Entity entity) {
		execute(world, entity, 10, 1.0f);
	}
}
