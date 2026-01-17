package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class DismantleBarrageOnTickAlternativeProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		// get cooldown from entity nbt
		int cooldown = entity.getPersistentData().getInt("barrage_cooldown");
		if (cooldown <= 0) {
			// execute slash
			BarrageSlashSpamProcedure.execute(entity.level(), entity);
			// set cooldown (5 ticks = 0.25 seconds between slashes)
			// lower number = faster barrage
			// adjust this value to balance:
			// 3 ticks = very fast
			// 5 ticks = balanced
			// 8 ticks = slower, more controlled
			entity.getPersistentData().putInt("barrage_cooldown", 3);
		} else {
			// decrease cooldown
			entity.getPersistentData().putInt("barrage_cooldown", cooldown - 1);
		}
	}
}
