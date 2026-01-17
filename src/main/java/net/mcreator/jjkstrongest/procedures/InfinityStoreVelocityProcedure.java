package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class InfinityStoreVelocityProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CompoundTag nbt = entity.getPersistentData();
		// only store if not already affected by infinity
		if (!nbt.getBoolean("infinityAffected")) {
			Vec3 vel = entity.getDeltaMovement();
			nbt.putDouble("infinityOriginalVelX", vel.x);
			nbt.putDouble("infinityOriginalVelY", vel.y);
			nbt.putDouble("infinityOriginalVelZ", vel.z);
			nbt.putFloat("infinityOriginalYaw", entity.getYRot());
			nbt.putFloat("infinityOriginalPitch", entity.getXRot());
			nbt.putBoolean("infinityAffected", true);
		}
	}
}
