package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;

public class ReversalRedRightClickedOnEntityProcedure {
	public static void execute(Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		double motionZ = 0;
		double deltaZ = 0;
		double deltaX = 0;
		double motionY = 0;
		double Yspeed = 0;
		double deltaY = 0;
		double motionX = 0;
		double speed = 0;
		if ((entity.getPersistentData().getString("caster")).equals(sourceentity.getDisplayName().getString())) {
			entity.getPersistentData().putString("state", "move");
			entity.setDeltaMovement(new Vec3((sourceentity.getLookAngle().x * entity.getPersistentData().getDouble("TechniquePower") * 1), (sourceentity.getLookAngle().y * entity.getPersistentData().getDouble("TechniquePower") * 1),
					(sourceentity.getLookAngle().z * entity.getPersistentData().getDouble("TechniquePower") * 1)));
			entity.getPersistentData().putDouble("RedX", (sourceentity.getLookAngle().x * entity.getPersistentData().getDouble("TechniquePower") * 1));
			entity.getPersistentData().putDouble("RedY", (sourceentity.getLookAngle().y * entity.getPersistentData().getDouble("TechniquePower") * 1));
			entity.getPersistentData().putDouble("RedZ", (sourceentity.getLookAngle().z * entity.getPersistentData().getDouble("TechniquePower") * 1));
		}
	}
}
