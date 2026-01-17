package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class DismantleBarageOnTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		Entity ent = null;
		if (world.getLevelData().getGameTime() % 5 == 0) {
			BarrageSlashSpamProcedure.execute(entity.level(), entity);
		}
	}
}
