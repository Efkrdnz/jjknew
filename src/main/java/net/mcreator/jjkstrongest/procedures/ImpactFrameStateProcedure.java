package net.mcreator.jjkstrongest.procedures;

public class ImpactFrameStateProcedure {
	public static final ImpactFrameStateProcedure INSTANCE = new ImpactFrameStateProcedure();
	public volatile boolean active = false;
	public volatile int remainingTicks = 0;
	// keep old fields (do not remove, other code may read them)
	public volatile float desaturateAmount = 1.0f;
	public volatile float gammaBoost = 1.2f;
	public volatile float contrast = 6.0f;
	public volatile float redTint = 1.8f;
	public volatile float saturation = 2.5f;
	// extra field for animation, does not break anything
	public volatile int totalTicks = 0;

	// full trigger (keep this)
	public void triggerCharged(int durationTicks, float desaturate, float gamma, float contrast, float red, float sat) {
		this.active = true;
		this.totalTicks = Math.max(1, durationTicks);
		this.remainingTicks = this.totalTicks;
		this.desaturateAmount = desaturate;
		this.gammaBoost = gamma;
		this.contrast = contrast;
		this.redTint = red;
		this.saturation = sat;
	}

	// convenience (keep this)
	public void triggerCharged(int durationTicks) {
		triggerCharged(durationTicks, 1.0f, 1.2f, 6.0f, 1.8f, 2.5f);
	}

	// IMPORTANT: this is the overload your mod calls everywhere (keep this EXACT signature)
	public void triggerCharged(int durationTicks, float desaturate, float gamma, float contrast) {
		triggerCharged(durationTicks, desaturate, gamma, contrast, 1.8f, 2.5f);
	}

	public float getProgress01() {
		int tt = totalTicks;
		if (tt <= 0)
			return 0.0f;
		float t = (float) (tt - remainingTicks) / (float) tt;
		if (t < 0)
			t = 0;
		if (t > 1)
			t = 1;
		return t;
	}

	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
			if (remainingTicks <= 0) {
				active = false;
			}
		}
	}

	public void stop() {
		active = false;
		remainingTicks = 0;
		totalTicks = 0;
	}
}
