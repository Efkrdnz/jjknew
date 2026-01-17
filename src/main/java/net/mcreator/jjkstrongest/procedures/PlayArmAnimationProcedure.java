package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class PlayArmAnimationProcedure {
	public static void execute(Entity entity, String animationName, boolean holdFirstFrame) {
		if (entity == null)
			return;
		// set data directly - will sync automatically for persistent NBT
		entity.getPersistentData().putString("current_arm_animation", animationName);
		entity.getPersistentData().putBoolean("arm_anim_holding", holdFirstFrame);
		entity.getPersistentData().putBoolean("arm_anim_playing", !holdFirstFrame);
		entity.getPersistentData().putFloat("arm_anim_progress", 0.0f);
		entity.getPersistentData().putLong("arm_anim_start_tick", entity.level().getGameTime());
		//System.out.println("PlayArmAnimation called: " + animationName + " | holding: " + holdFirstFrame + " | isClientSide: " + entity.level().isClientSide());
	}
}
