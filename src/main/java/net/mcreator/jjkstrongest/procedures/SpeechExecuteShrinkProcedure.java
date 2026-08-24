package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class SpeechExecuteShrinkProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CursedSpeechTargetingProcedure.execute(entity.level(), entity, "shrink");
	}
}
