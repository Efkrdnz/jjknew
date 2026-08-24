package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class CleaveHoldTickProcedure {
	// counts to 15 ticks then executes cleave
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		if (!data.getBoolean("cleave_holding"))
			return;
		double timer = data.getDouble("cleave_hold_timer") + 1;
		data.putDouble("cleave_hold_timer", timer);
		if (timer >= 15) {
			CleaveHoldExecuteProcedure.execute(world, entity);
			data.putBoolean("cleave_holding", false);
			data.putDouble("cleave_hold_timer", 0);
		}
	}
}
