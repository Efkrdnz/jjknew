package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.CompoundTag;

public class CleaveSlashGenerateProcedure {
	public static void execute(Entity entity, long seed, int wantedCount) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		int count = Math.max(1, Math.min(4, wantedCount));
		data.putInt("cleave_slash_count", count);
		RandomSource r = RandomSource.create(seed);
		for (int i = 1; i <= 4; i++) {
			data.remove("cleave_slash" + i);
		}
		for (int i = 1; i <= count; i++) {
			float sx = 0.25f + r.nextFloat() * 0.50f;
			float sy = 0.25f + r.nextFloat() * 0.50f;
			float angle = (float) (r.nextFloat() * Math.PI);
			float strength = 0.9f + r.nextFloat() * 0.4f;
			ListTag tag = new ListTag();
			tag.add(FloatTag.valueOf(sx));
			tag.add(FloatTag.valueOf(sy));
			tag.add(FloatTag.valueOf(angle));
			tag.add(FloatTag.valueOf(strength));
			data.put("cleave_slash" + i, tag);
		}
	}
}
