package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public class TriggerChargedImpactProcedure {
	// trigger with all parameters
	public static void execute(Level world, Entity entity, int durationTicks, float desaturate, float gamma, float contrast) {
		if (entity == null || world == null || !world.isClientSide)
			return;
		ImpactFrameStateProcedure.INSTANCE.triggerCharged(durationTicks, desaturate, gamma, contrast);
	}

	// default: INTENSE impact frame
	public static void execute(Level world, Entity entity, int durationTicks) {
		execute(world, entity, durationTicks, 1.0f, 1.8f, 2.2f);
	}

	// simple trigger
	public static void execute(Level world, Entity entity) {
		execute(world, entity, 5, 1.0f, 1.8f, 2.2f);
	}
}
