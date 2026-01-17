package net.mcreator.jjkstrongest.procedures;

public class ArmAnimationDataProcedure {
	public String animationName;
	public boolean useRightArm;
	public boolean useLeftArm;
	public boolean forceVisible;
	// right arm poses
	public float rightStartTransX, rightStartTransY, rightStartTransZ;
	public float rightStartRotX, rightStartRotY, rightStartRotZ;
	public float rightEndTransX, rightEndTransY, rightEndTransZ;
	public float rightEndRotX, rightEndRotY, rightEndRotZ;
	// left arm poses
	public float leftStartTransX, leftStartTransY, leftStartTransZ;
	public float leftStartRotX, leftStartRotY, leftStartRotZ;
	public float leftEndTransX, leftEndTransY, leftEndTransZ;
	public float leftEndRotX, leftEndRotY, leftEndRotZ;
	// animation properties
	public float duration;
	public String easingType;

	public ArmAnimationDataProcedure(String name) {
		this.animationName = name;
		this.useRightArm = true;
		this.useLeftArm = false;
		this.forceVisible = false;
		this.duration = 10.0f;
		this.easingType = "ease_out";
	}

	// set which arms to animate
	public ArmAnimationDataProcedure setArms(boolean right, boolean left) {
		this.useRightArm = right;
		this.useLeftArm = left;
		return this;
	}

	// force arms visible
	public ArmAnimationDataProcedure setForceVisible(boolean force) {
		this.forceVisible = force;
		return this;
	}

	// set right arm start pose
	public ArmAnimationDataProcedure setRightStartPose(float tx, float ty, float tz, float rx, float ry, float rz) {
		this.rightStartTransX = tx;
		this.rightStartTransY = ty;
		this.rightStartTransZ = tz;
		this.rightStartRotX = rx;
		this.rightStartRotY = ry;
		this.rightStartRotZ = rz;
		return this;
	}

	// set right arm end pose
	public ArmAnimationDataProcedure setRightEndPose(float tx, float ty, float tz, float rx, float ry, float rz) {
		this.rightEndTransX = tx;
		this.rightEndTransY = ty;
		this.rightEndTransZ = tz;
		this.rightEndRotX = rx;
		this.rightEndRotY = ry;
		this.rightEndRotZ = rz;
		return this;
	}

	// set left arm start pose
	public ArmAnimationDataProcedure setLeftStartPose(float tx, float ty, float tz, float rx, float ry, float rz) {
		this.leftStartTransX = tx;
		this.leftStartTransY = ty;
		this.leftStartTransZ = tz;
		this.leftStartRotX = rx;
		this.leftStartRotY = ry;
		this.leftStartRotZ = rz;
		return this;
	}

	// set left arm end pose
	public ArmAnimationDataProcedure setLeftEndPose(float tx, float ty, float tz, float rx, float ry, float rz) {
		this.leftEndTransX = tx;
		this.leftEndTransY = ty;
		this.leftEndTransZ = tz;
		this.leftEndRotX = rx;
		this.leftEndRotY = ry;
		this.leftEndRotZ = rz;
		return this;
	}

	// mirror right arm pose to left arm automatically
	public ArmAnimationDataProcedure mirrorToLeft() {
		this.leftStartTransX = -this.rightStartTransX;
		this.leftStartTransY = this.rightStartTransY;
		this.leftStartTransZ = this.rightStartTransZ;
		this.leftStartRotX = this.rightStartRotX;
		this.leftStartRotY = -this.rightStartRotY;
		this.leftStartRotZ = -this.rightStartRotZ;
		this.leftEndTransX = -this.rightEndTransX;
		this.leftEndTransY = this.rightEndTransY;
		this.leftEndTransZ = this.rightEndTransZ;
		this.leftEndRotX = this.rightEndRotX;
		this.leftEndRotY = -this.rightEndRotY;
		this.leftEndRotZ = -this.rightEndRotZ;
		return this;
	}

	public ArmAnimationDataProcedure setDuration(float ticks) {
		this.duration = ticks;
		return this;
	}

	public ArmAnimationDataProcedure setEasing(String type) {
		this.easingType = type;
		return this;
	}
}
