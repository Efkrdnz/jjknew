package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;

public class CleaveForcePlayerLookProcedure {
	// force player to look at target entity center
	public static void execute(Entity entity, Entity target) {
		if (entity == null || target == null)
			return;
		// get player eye position
		Vec3 playerPos = entity.getEyePosition();
		// calculate target center position (middle of bounding box)
		double targetCenterX = target.getX();
		double targetCenterY = target.getY() + (target.getBbHeight() / 2.0);
		double targetCenterZ = target.getZ();
		// calculate direction from player to target center
		double dx = targetCenterX - playerPos.x;
		double dy = targetCenterY - playerPos.y;
		double dz = targetCenterZ - playerPos.z;
		// calculate yaw (horizontal rotation)
		double horizontalDist = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		// calculate pitch (vertical rotation)
		float pitch = (float) Math.toDegrees(Math.atan2(-dy, horizontalDist));
		// clamp pitch between -90 and 90
		pitch = Math.max(-90.0f, Math.min(90.0f, pitch));
		// set player rotation
		entity.setYRot(yaw);
		entity.setXRot(pitch);
		entity.setYHeadRot(yaw);
		// prevent rotation changes (client side smoothing)
		if (entity.level().isClientSide()) {
			entity.xRotO = pitch;
			entity.yRotO = yaw;
		}
	}
}
