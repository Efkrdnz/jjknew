package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;

// called from mcreator "when entity ticks" procedure
// also called directly from SukunaEntity.customServerAiStep()
public class SukunaNPCTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || world.isClientSide())
			return;
		if (!(entity instanceof PathfinderMob))
			return;
		if (!(world instanceof Level level))
			return;
		SukunaNPCAIProcedure.execute(level, entity);
	}

	// overload for direct Level calls from SukunaEntity
	public static void execute(Level world, Entity entity) {
		execute((LevelAccessor) world, entity);
	}
}
