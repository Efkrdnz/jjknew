package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class BlackFlashQTEStateProcedure {
	public static final BlackFlashQTEStateProcedure INSTANCE = new BlackFlashQTEStateProcedure();
	private boolean active = false;
	private long startTime = 0;
	private float successZoneStart = 0;
	private float successZoneSize = 30.0f; // 30 degree arc (medium difficulty)
	private float rotationSpeed = 360.0f; // 1 second per full rotation
	private boolean hasTimedOut = false;

	private BlackFlashQTEStateProcedure() {
	}

	// start qte with random success zone between 90-360 degrees
	public void startQTE() {
		this.active = true;
		this.startTime = System.currentTimeMillis();
		this.hasTimedOut = false;
		// random zone between 90° (3 o'clock) and 360° (12 o'clock)
		this.successZoneStart = 90.0f + (float) (Math.random() * 270.0f);
	}

	public boolean isActive() {
		return active;
	}

	// get current line rotation (0-360 degrees, 0 = 12 o'clock, clockwise)
	public float getCurrentRotation() {
		if (!active)
			return 0;
		long elapsed = System.currentTimeMillis() - startTime;
		float seconds = elapsed / 1000.0f;
		return (seconds * rotationSpeed) % 360.0f;
	}

	public float getSuccessZoneStart() {
		return successZoneStart;
	}

	public float getSuccessZoneEnd() {
		return (successZoneStart + successZoneSize) % 360.0f;
	}

	// check if current rotation is in success zone
	public boolean isInSuccessZone() {
		if (!active)
			return false;
		float current = getCurrentRotation();
		float start = successZoneStart;
		float end = getSuccessZoneEnd();
		// handle wraparound (zone crossing 0 degrees)
		if (end < start) {
			boolean result = current >= start || current <= end;
			return result;
		}
		boolean result = current >= start && current <= end;
		return result;
	}

	// check if qte has timed out (more than 1 full rotation = 1 second)
	public boolean hasTimedOut() {
		if (!active)
			return false;
		long elapsed = System.currentTimeMillis() - startTime;
		return elapsed > 1000; // timeout after 1 second
	}

	// end qte and return success status
	public boolean endQTE() {
		boolean success = isInSuccessZone() && !hasTimedOut();
		float currentRot = getCurrentRotation();
		//System.out.println("[QTE State] End QTE - Current rotation: " + currentRot + " | Zone: " + successZoneStart + "-" + getSuccessZoneEnd() + " | In zone: " + isInSuccessZone() + " | Timed out: " + hasTimedOut() + " | SUCCESS: " + success);
		this.active = false;
		return success;
	}

	// force end qte (for timeout/miss)
	public void cancelQTE() {
		this.active = false;
		this.hasTimedOut = true;
	}
}
