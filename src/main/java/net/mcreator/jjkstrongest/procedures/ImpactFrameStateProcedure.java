package net.mcreator.jjkstrongest.procedures;

public class ImpactFrameStateProcedure {
	public static final ImpactFrameStateProcedure INSTANCE = new ImpactFrameStateProcedure();
	public volatile boolean active = false;
	public volatile int remainingTicks = 0;
	public volatile float desaturateAmount = 1.0f;
	public volatile float gammaBoost = 1.2f;
	public volatile float contrast = 6.0f;
	public volatile float redTint = 1.8f;
	public volatile float saturation = 2.5f;

	// REMOVED: invert field
	// trigger with all parameters
	public void triggerCharged(int durationTicks, float desaturate, float gamma, float contrast, float red, float sat) {
		this.active = true;
		this.remainingTicks = durationTicks;
		this.desaturateAmount = desaturate;
		this.gammaBoost = gamma;
		this.contrast = contrast;
		this.redTint = red;
		this.saturation = sat;
	}

	// convenience with black/red settings
	public void triggerCharged(int durationTicks) {
		triggerCharged(durationTicks, 1.0f, 1.2f, 6.0f, 1.8f, 2.5f);
	}

	// custom settings
	public void triggerCharged(int durationTicks, float desaturate, float gamma, float contrast) {
		triggerCharged(durationTicks, desaturate, gamma, contrast, 1.8f, 2.5f);
	}

	// update each tick
	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
			if (remainingTicks <= 0) {
				active = false;
			}
		}
	}

	// force stop
	public void stop() {
		active = false;
		remainingTicks = 0;
	}
}
