package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class SpeechExecuteWeepProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CursedSpeechTargetingProcedure.execute(entity.level(), entity, "weep");
	}
}
