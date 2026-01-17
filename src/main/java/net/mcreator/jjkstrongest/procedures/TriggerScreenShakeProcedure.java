package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public class TriggerScreenShakeProcedure {
	// trigger screen shake with custom parameters
	public static void execute(Level world, Entity entity, int durationTicks, float intensity) {
		if (entity == null || world == null)
			return;
		// only trigger on client side
		if (!world.isClientSide)
			return;
		ScreenShakeStateProcedure.INSTANCE.trigger(durationTicks, intensity);
	}

	// convenience method with default values
	public static void execute(Level world, Entity entity, int durationTicks) {
		execute(world, entity, durationTicks, 2.0f); // default intensity
	}

	// quick shake
	public static void execute(Level world, Entity entity) {
		execute(world, entity, 10, 2.0f); // 0.5 seconds, medium intensity
	}
}
