package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;

// Called from GojoEntity.customServerAiStep() every tick on the server.
public class GojoNPCTickProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || world.isClientSide())
			return;
		if (!(entity instanceof PathfinderMob))
			return;
		if (!(world instanceof Level level))
			return;
		GojoNPCAIProcedure.execute(level, entity);
	}

	// Overload accepting Level directly (called from customServerAiStep).
	public static void execute(Level world, Entity entity) {
		execute((LevelAccessor) world, entity);
	}
}
