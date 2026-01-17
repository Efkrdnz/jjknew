package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;

public class GetCurrentArmPoseProcedure {
	// easing functions
	private static float applyEasing(float t, String easingType) {
		switch (easingType) {
			case "ease_out" :
				return 1 - (float) Math.pow(1 - t, 3);
			case "ease_in" :
				return (float) Math.pow(t, 3);
			case "ease_in_out" :
				return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
			default :
				return t;
		}
	}

	// interpolate between two values
	private static float lerp(float start, float end, float t) {
		return start + (end - start) * t;
	}

	// get current pose for rendering with smooth interpolation
	public static float[] execute(Entity entity, HumanoidArm arm) {
		if (entity == null)
			return null;
		String animName = entity.getPersistentData().getString("current_arm_animation");
		if (animName.isEmpty()) {
			return null;
		}
		ArmAnimationDataProcedure anim = ArmAnimationRegistryProcedure.getAnimation(animName);
		if (anim == null) {
			return null;
		}
		// check if this arm should be animated
		boolean isRight = (arm == HumanoidArm.RIGHT);
		if (isRight && !anim.useRightArm)
			return null;
		if (!isRight && !anim.useLeftArm)
			return null;
		boolean holding = entity.getPersistentData().getBoolean("arm_anim_holding");
		float progress = entity.getPersistentData().getFloat("arm_anim_progress");
		// add partial tick interpolation for smooth rendering
		if (!holding) {
			float partialTick = Minecraft.getInstance().getFrameTime();
			float smoothProgress = progress + (1.0f / anim.duration) * partialTick;
			progress = Math.min(smoothProgress, 1.0f);
		}
		// apply easing
		float easedProgress = applyEasing(progress, anim.easingType);
		// get poses based on arm
		if (isRight) {
			if (holding) {
				return new float[]{anim.rightStartTransX, anim.rightStartTransY, anim.rightStartTransZ, anim.rightStartRotX, anim.rightStartRotY, anim.rightStartRotZ};
			} else {
				return new float[]{lerp(anim.rightStartTransX, anim.rightEndTransX, easedProgress), lerp(anim.rightStartTransY, anim.rightEndTransY, easedProgress), lerp(anim.rightStartTransZ, anim.rightEndTransZ, easedProgress),
						lerp(anim.rightStartRotX, anim.rightEndRotX, easedProgress), lerp(anim.rightStartRotY, anim.rightEndRotY, easedProgress), lerp(anim.rightStartRotZ, anim.rightEndRotZ, easedProgress)};
			}
		} else {
			if (holding) {
				return new float[]{anim.leftStartTransX, anim.leftStartTransY, anim.leftStartTransZ, anim.leftStartRotX, anim.leftStartRotY, anim.leftStartRotZ};
			} else {
				return new float[]{lerp(anim.leftStartTransX, anim.leftEndTransX, easedProgress), lerp(anim.leftStartTransY, anim.leftEndTransY, easedProgress), lerp(anim.leftStartTransZ, anim.leftEndTransZ, easedProgress),
						lerp(anim.leftStartRotX, anim.leftEndRotX, easedProgress), lerp(anim.leftStartRotY, anim.leftEndRotY, easedProgress), lerp(anim.leftStartRotZ, anim.leftEndRotZ, easedProgress)};
			}
		}
	}
}
