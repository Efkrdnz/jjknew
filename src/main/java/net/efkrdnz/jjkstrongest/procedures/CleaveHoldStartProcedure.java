package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class CleaveHoldStartProcedure {
	// start cleave immediately, execute after 15 ticks
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		if (data.getBoolean("cleave_holding"))
			return;
		data.putBoolean("cleave_holding", true);
		data.putDouble("cleave_hold_timer", 0);
		// trigger visuals immediately (client side only)
		if (world instanceof Level lvl && lvl.isClientSide()) {
			TriggerCleaveDistortionProcedure.execute(lvl, entity, 15, 1.0f, 8);
		}
	}
}
