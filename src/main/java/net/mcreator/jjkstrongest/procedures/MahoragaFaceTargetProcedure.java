package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.h;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class MahoragaFaceTargetProcedure {
	// forces yaw/pitch to face target
	public static void execute(Entity entity, LivingEntity target) {
		if (entity == null || target == null)
			return;
		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx))) - 90.0F;
		double dy = (target.getY() + target.getEyeHeight()) - (entity.getY() + entity.getBbHeight() * 0.85);
		double h = Math.sqrt(dx * dx + dz * dz);
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, h));
		entity.setYRot(yaw);
		entity.yRotO = yaw;
		entity.setXRot(pitch);
		entity.xRotO = pitch;
		if (entity instanceof LivingEntity le) {
			le.yBodyRot = yaw;
			le.yHeadRot = yaw;
		}
	}
}
