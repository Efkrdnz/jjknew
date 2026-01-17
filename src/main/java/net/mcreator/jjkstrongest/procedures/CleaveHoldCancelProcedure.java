package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class CleaveHoldCancelProcedure {
	// cancel cleave hold when button released
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		boolean hasTarget = data.getBoolean("cleave_has_target");
		// UNCANCELLABLE once target is locked - only allow cancel before lock
		if (data.getBoolean("cleave_holding") && !hasTarget) {
			data.putBoolean("cleave_holding", false);
			data.putBoolean("cleave_has_target", false);
			data.putBoolean("cleave_camera_locked", false);
			data.putDouble("cleave_hold_timer", 0);
			data.putString("cleave_target_uuid", "");
		}
	}
}
