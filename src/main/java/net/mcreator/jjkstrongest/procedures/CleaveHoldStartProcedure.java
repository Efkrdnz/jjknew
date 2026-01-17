package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class CleaveHoldStartProcedure {
	// start cleave hold state
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		if (data.getBoolean("cleave_holding"))
			return;
		// just set holding state, target finding happens in tick
		data.putBoolean("cleave_holding", true);
		data.putBoolean("cleave_has_target", false);
		data.putDouble("cleave_hold_timer", 0);
		data.putString("cleave_target_uuid", "");
	}
}
