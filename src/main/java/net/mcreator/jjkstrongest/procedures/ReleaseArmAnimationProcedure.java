package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class ReleaseArmAnimationProcedure {
	// release held animation (start playing)
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putBoolean("arm_anim_holding", false);
		entity.getPersistentData().putBoolean("arm_anim_playing", true);
		entity.getPersistentData().putFloat("arm_anim_progress", 0.0f);
		entity.getPersistentData().putLong("arm_anim_start_tick", entity.level().getGameTime());
	}
}
