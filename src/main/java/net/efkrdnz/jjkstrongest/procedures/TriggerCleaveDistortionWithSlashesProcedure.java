package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;

public class TriggerCleaveDistortionWithSlashesProcedure {
	public static void execute(Level world, Entity entity, int durationTicks, float intensity) {
		if (world == null || entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		entity.getPersistentData().putInt("cleave_distortion_ticks", durationTicks);
		entity.getPersistentData().putFloat("cleave_distortion_intensity", intensity);
		int count = Math.max(1, Math.min(4, data.getInt("cleave_slash_count")));
		entity.getPersistentData().putInt("cleave_distortion_slashes", count);
		// copy slash arrays into NBT so the client tick can read exact slashes
		for (int i = 1; i <= 4; i++) {
			if (data.get("cleave_slash" + i) instanceof ListTag t) {
				entity.getPersistentData().put("cleave_distortion_slash" + i, t.copy());
			} else {
				entity.getPersistentData().remove("cleave_distortion_slash" + i);
			}
		}
		entity.getPersistentData().putLong("cleave_distortion_trigger", world.getGameTime());
	}
}
