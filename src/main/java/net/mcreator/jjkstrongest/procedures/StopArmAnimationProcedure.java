package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class StopArmAnimationProcedure {
	// stop animation
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putString("current_arm_animation", "");
		entity.getPersistentData().putBoolean("arm_anim_holding", false);
		entity.getPersistentData().putBoolean("arm_anim_playing", false);
		entity.getPersistentData().putFloat("arm_anim_progress", 0.0f);
	}
}
