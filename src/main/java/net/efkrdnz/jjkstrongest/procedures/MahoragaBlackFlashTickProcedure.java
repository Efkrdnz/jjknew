package net.efkrdnz.jjkstrongest.procedures;


import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

public class MahoragaBlackFlashTickProcedure {
	// consumes bf markers and decays bf meter
	public static void execute(ServerLevel level, Entity entity) {
		if (level == null || entity == null)
			return;
		int cd = entity.getPersistentData().getInt("maho_bf_cd");
		if (cd > 0)
			entity.getPersistentData().putInt("maho_bf_cd", cd - 1);
		double add = entity.getPersistentData().getDouble("jjk_bf_add");
		if (add <= 0 && entity.getPersistentData().getInt("jjk_bf_hit") == 1)
			add = 1;
		if (add > 0 && entity.getPersistentData().getInt("maho_bf_cd") <= 0) {
			double bf = entity.getPersistentData().getDouble("maho_bf");
			bf = Math.min(MahoragaConstantsProcedure.BF_CAP, bf + add);
			entity.getPersistentData().putDouble("maho_bf", bf);
			entity.getPersistentData().putInt("maho_bf_cd", 6);
		}
		entity.getPersistentData().putDouble("jjk_bf_add", 0);
		entity.getPersistentData().putInt("jjk_bf_hit", 0);
		double bf = entity.getPersistentData().getDouble("maho_bf");
		if (bf > 0) {
			bf = Math.max(0, bf - MahoragaConstantsProcedure.BF_DECAY);
			entity.getPersistentData().putDouble("maho_bf", bf);
		}
	}
}
