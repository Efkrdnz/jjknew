package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class MarkExecuteOnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		Entity ent = null;
		ent = entity;
		TestExecuteMarkProcedure.execute(entity.level(), entity);
	}
}
