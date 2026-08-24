package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class CleaveHoldCancelProcedure {
	// cancel if released before execute
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		if (!data.getBoolean("cleave_holding"))
			return;
		data.putBoolean("cleave_holding", false);
		data.putDouble("cleave_hold_timer", 0);
	}
}
