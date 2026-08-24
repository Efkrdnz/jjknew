package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class DismantleBarrageProjectileOnTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		int cooldown = entity.getPersistentData().getInt("barrage_proj_cooldown");
		if (cooldown <= 0) {
			BarrageProjectileSpamProcedure.execute(entity.level(), entity);
			entity.getPersistentData().putInt("barrage_proj_cooldown", 3);
		} else {
			entity.getPersistentData().putInt("barrage_proj_cooldown", cooldown - 1);
		}
	}
}
