package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class ArmAnimationTickProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.getPersistentData().getBoolean("arm_anim_playing")) {
			String animName = entity.getPersistentData().getString("current_arm_animation");
			ArmAnimationDataProcedure anim = ArmAnimationRegistryProcedure.getAnimation(animName);
			if (anim != null) {
				float progress = entity.getPersistentData().getFloat("arm_anim_progress");
				progress += 1.0f / anim.duration;
				if (progress >= 1.0f) {
					StopArmAnimationProcedure.execute(entity);
				} else {
					entity.getPersistentData().putFloat("arm_anim_progress", progress);
				}
			}
		}
	}
}
