package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class InfinityFreezeProjectileProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CompoundTag nbt = entity.getPersistentData();
		// store original velocity only once
		if (!nbt.getBoolean("infinityFrozen")) {
			Vec3 vel = entity.getDeltaMovement();
			nbt.putDouble("infinityOriginalVelX", vel.x);
			nbt.putDouble("infinityOriginalVelY", vel.y);
			nbt.putDouble("infinityOriginalVelZ", vel.z);
			nbt.putBoolean("infinityFrozen", true);
		}
		// freeze movement only
		entity.setDeltaMovement(Vec3.ZERO);
	}
}
