package net.mcreator.jjkstrongest.procedures;

public class ScreenShakeStateProcedure {
	public static final ScreenShakeStateProcedure INSTANCE = new ScreenShakeStateProcedure();
	public volatile boolean active = false;
	public volatile int remainingTicks = 0;
	public volatile float intensity = 2.0f;
	public volatile float fadeOut = 1.0f; // multiplier that fades over time
	// trigger screen shake

	public void trigger(int durationTicks, float intensity) {
		this.active = true;
		this.remainingTicks = durationTicks;
		this.intensity = intensity;
		this.fadeOut = 1.0f;
	}

	// update each tick
	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
			// fade out effect over time (optional)
			fadeOut = (float) remainingTicks / (remainingTicks + 5.0f);
			if (remainingTicks <= 0) {
				active = false;
				fadeOut = 1.0f;
			}
		}
	}

	// get current intensity with fade
	public float getCurrentIntensity() {
		return active ? intensity * fadeOut : 0.0f;
	}

	// force stop
	public void stop() {
		active = false;
		remainingTicks = 0;
		fadeOut = 1.0f;
	}
}
