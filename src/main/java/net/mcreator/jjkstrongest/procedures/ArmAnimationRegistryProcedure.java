package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.HumanoidArm;

import java.util.Map;
import java.util.HashMap;

public class ArmAnimationRegistryProcedure {
    private static final Map<String, ArmAnimationDataProcedure> ANIMATIONS = new HashMap<>();
    static {
        // dismantle - right hand only, force visible
        ANIMATIONS.put("dismantle", new ArmAnimationDataProcedure("dismantle")
            .setArms(true, false)
            .setForceVisible(true)
            .setRightStartPose(0.8f, 0.47f, -0.35f, -11.56f, 48.75f, 0.94f)
            .setRightEndPose(0.8f, 0.47f, -0.35f, -11.56f, 48.75f, 0.94f)
            .setDuration(8f)
            .setEasing("ease_out"));
        
        // cleave - right hand only, force visible
        ANIMATIONS.put("cleave", new ArmAnimationDataProcedure("cleave")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(0.8f, 0.47f, -0.35f, -11.56f, 48.75f, 0.00f)
    		.setRightEndPose(0.8f, 0.47f, -0.35f, -11.56f, 48.75f, 0.00f)
    		// left arm
    		.setLeftStartPose(-0.8f, 0.47f, -0.35f, -11.56f, -48.75f, 0.00f)
    		.setLeftEndPose(-0.8f, 0.47f, -0.35f, -11.56f, -48.75f, 0.00f)
    		.setDuration(10f)
    		.setEasing("ease_in_out"));
        
        // blue charge - right hand only, force visible
        ANIMATIONS.put("blue_charge", new ArmAnimationDataProcedure("blue_charge")
            .setArms(true, false)
            .setForceVisible(true)
            .setRightStartPose(0, 0, 0, 0, 0, 0)
            .setRightEndPose(-0.3f, 0.2f, -0.4f, -45f, 30f, 20f)
            .setDuration(15f)
            .setEasing("ease_in_out"));
        
        // red charge - right hand only, force visible
        ANIMATIONS.put("red_charge", new ArmAnimationDataProcedure("red_charge")
            .setArms(true, false)
            .setForceVisible(true)
            .setRightStartPose(0.72f, -0.14f, 0.13f, 13.44f, 58.44f, 9.69f)
            .setRightEndPose(0.72f, -0.14f, 0.13f, 13.44f, 58.44f, 9.69f)
            .setDuration(12f)
            .setEasing("ease_out"));

        //gojo domain pose
        ANIMATIONS.put("domain_gojo", new ArmAnimationDataProcedure("domain_gojo")
            .setArms(true, false)
            .setForceVisible(true)
            .setRightStartPose(0.72f, -0.14f, 0.13f, 13.44f, 58.44f, 9.69f)
            .setRightEndPose(0.72f, -0.14f, 0.13f, 13.44f, 58.44f, 9.69f)
            .setDuration(20f)
            .setEasing("ease_out"));

		//sukuna domain pose
    	ANIMATIONS.put("domain_sukuna", new ArmAnimationDataProcedure("domain_sukuna")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(1.00f, 0.12f, -0.51f, 0f, 76.25f, 0f)
    		.setRightEndPose(1.00f, 0.12f, -0.51f, 0f, 76.25f, 0f)
    		// left arm
    		.setLeftStartPose(-1.00f, 0.12f, -0.51f, 0f, -76.25f, 0f)
    		.setLeftEndPose(-1.00f, 0.12f, -0.51f, 0f, -76.25f, 0f)
    		.setDuration(20f)
    		.setEasing("ease_in_out"));

    	//gojo blue pose (2 armed)
    	ANIMATIONS.put("blue_charge", new ArmAnimationDataProcedure("blue_charge")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(1.00f, 0.30f, -0.28f, -14.06f, 61.25f, 0f)
    		.setRightEndPose(1.00f, 0.30f, -0.28f, -14.06f, 61.25f, 0f)
    		// left arm
    		.setLeftStartPose(-1.00f, 0.30f, -0.28f, -14.06f, -61.25f, 0f)
    		.setLeftEndPose(-1.00f, 0.30f, -0.28f, -14.06f, -61.25f, 0f)
    		.setDuration(20f)
    		.setEasing("ease_in_out"));

    	//hollow_purple pose
    	ANIMATIONS.put("hollow_purple", new ArmAnimationDataProcedure("hollow_purple")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(1.00f, 0.12f, -0.51f, 0f, 76.25f, 0f)
    		.setRightEndPose(1.00f, 0.39f, -1.00f, -23.44f, 53.44f, 0f)
    		// left arm
    		.setLeftStartPose(-1.00f, 0.12f, -0.51f, 0f, -76.25f, 0f)
    		.setLeftEndPose(-0.64f, -0.55f, -0.10f, 29.69f, -22.19f, 0f)
    		.setDuration(40f)
    		.setEasing("ease_in_out"));

    	//imaginary_purple pose
    	ANIMATIONS.put("imaginary_purple", new ArmAnimationDataProcedure("imaginary_purple")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(0.58f, 0.39f, -0.58f, -10.94f, 36.88f, 0f)
    		.setRightEndPose(0.58f, 0.39f, -0.58f, -10.94f, 36.88f, 0f)
    		// left arm
    		.setLeftStartPose(-0.70f, 0.35f, -0.85f, -25.00f, -90.00f, 0f)
    		.setLeftEndPose(-0.70f, 0.35f, -0.85f, -25.00f, -90.00f, 0f)
    		.setDuration(20f)
    		.setEasing("ease_in_out"));

		//open furnace pose
    	ANIMATIONS.put("fuga_hold", new ArmAnimationDataProcedure("fuga_hold")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(1.00f, 0.19f, 0.19f, -5.00f, 52.50f, 0f)
    		.setRightEndPose(1.00f, 0.19f, 0.19f, -5.00f, 52.50f, 0f)
    		// left arm
    		.setLeftStartPose(-0.39f, 0.31f, -1.00f, -20.00f, -62.50f, 0f)
    		.setLeftEndPose(-0.39f, 0.31f, -1.00f, -20.00f, -62.50f, 0f)
    		.setDuration(20f)
    		.setEasing("ease_in_out"));

    	//MELEE - BLOCK
    	ANIMATIONS.put("blocking", new ArmAnimationDataProcedure("blocking")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(0.78f, -0.60f, -0.19f, 34.06f, 44.69f, 0f)
    		.setRightEndPose(0.78f, -0.38f, 0.05f, 33.13f, 50.63f, 0f)
    		// left arm
    		.setLeftStartPose(-0.78f, -0.60f, -0.19f, 34.06f, -44.69f, 0f)
    		.setLeftEndPose(-0.78f, -0.38f, 0.05f, 33.13f, -50.63f, 0f)
    		.setDuration(5f)
    		.setEasing("ease_out"));

    	//MELEE - right jab
    	ANIMATIONS.put("jab_right", new ArmAnimationDataProcedure("jab_right")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(-0.03f, 0.61f, -0.65f, -41.88f, -1.88f, 0f)
    		.setRightEndPose(0.61f, 0.48f, -0.98f, -16.25f, 52.19f, 0f)
    		// left arm
    		.setLeftStartPose(-0.61f, 0.48f, -0.98f, -16.25f, -52.19f, 0f)
    		.setLeftEndPose(0.03f, 0.61f, -0.65f, -41.88f, 1.88f, 0f)
    		.setDuration(5f)
    		.setEasing("ease_out"));

    	//MELEE - left jab
    	ANIMATIONS.put("jab_left", new ArmAnimationDataProcedure("jab_left")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(0.61f, 0.48f, -0.98f, -16.25f, 52.19f, 0f)
    		.setRightEndPose(-0.03f, 0.61f, -0.65f, -41.88f, -1.88f, 0f)
    		// left arm
    		.setLeftStartPose(0.03f, 0.61f, -0.65f, -41.88f, 1.88f, 0f)
    		.setLeftEndPose(-0.61f, 0.48f, -0.98f, -16.25f, -52.19f, 0f)
    		.setDuration(5f)
    		.setEasing("ease_out"));

    	//MELEE - right uppercut
    	ANIMATIONS.put("uppercut_right", new ArmAnimationDataProcedure("uppercut_right")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(0.40f, 0.25f, -0.27f, -18.75f, -26.25f, 0f)
    		.setRightEndPose(0.38f, -0.29f, -0.20f, 43.44f, 41.88f, 0f)
    		// left arm
    		.setLeftStartPose(-0.61f, 0.48f, -0.98f, -16.25f, -52.19f, 0f)
    		.setLeftEndPose(0.03f, 0.61f, -0.65f, -41.88f, 1.88f, 0f)
    		.setDuration(5f)
    		.setEasing("ease_out"));

    	//MELEE - left uppercut
    	ANIMATIONS.put("uppercut_left", new ArmAnimationDataProcedure("uppercut_left")
    		.setArms(true, true)
    		.setForceVisible(true)
    		// right arm
    		.setRightStartPose(0.61f, 0.48f, -0.98f, -16.25f, 52.19f, 0f)
    		.setRightEndPose(-0.03f, 0.61f, -0.65f, -41.88f, -1.88f, 0f)
    		// left arm
    		.setLeftStartPose(-0.40f, 0.25f, -0.27f, -18.75f, 26.25f, 0f)
    		.setLeftEndPose(-0.38f, -0.29f, -0.20f, 43.44f, -41.88f, 0f)
    		.setDuration(5f)
    		.setEasing("ease_out"));
    }

    public static ArmAnimationDataProcedure getAnimation(String name) {
        return ANIMATIONS.get(name);
    }

    public static void registerAnimation(String name, ArmAnimationDataProcedure animation) {
        ANIMATIONS.put(name, animation);
    }
}