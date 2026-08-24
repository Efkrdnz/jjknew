package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class TestExecuteMarkProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		ExecuteMarkAbilityProcedure.execute(world, entity);
	}
}
