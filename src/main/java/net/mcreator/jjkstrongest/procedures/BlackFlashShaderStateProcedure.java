package net.mcreator.jjkstrongest.procedures;

public class BlackFlashShaderStateProcedure {
	public static final BlackFlashShaderStateProcedure INSTANCE = new BlackFlashShaderStateProcedure();
	public volatile boolean active = false;
	public volatile int remainingTicks = 0;
	public volatile int totalDuration = 0;
	public volatile float baseIntensity = 1.0f;
	public volatile float intensity = 1.0f;
	public volatile float time = 0.0f;
	private int currentTick = 0;

	// trigger shader
	public void trigger(int durationTicks, float intensity) {
		this.active = true;
		this.remainingTicks = durationTicks;
		this.totalDuration = durationTicks;
		this.baseIntensity = intensity;
		this.intensity = intensity;
		this.currentTick = 0;
		this.time = 0.0f;
	}

	// update each tick
	public void tick() {
		if (remainingTicks > 0) {
			currentTick = totalDuration - remainingTicks;
			// calculate time progress (0.0 to 1.0)
			float progress = (float) currentTick / (float) totalDuration;
			this.time = progress;
			// fade intensity: sharp start, gradual fade
			// peaks at 20% progress, then fades out
			float fadeMultiplier;
			if (progress < 0.2f) {
				// fast rise to peak
				fadeMultiplier = progress / 0.2f;
			} else {
				// slow fade out
				fadeMultiplier = 1.0f - ((progress - 0.2f) / 0.8f);
			}
			// apply base intensity with fade curve
			this.intensity = baseIntensity * fadeMultiplier;
			remainingTicks--;
			if (remainingTicks <= 0) {
				active = false;
			}
		}
	}

	public void stop() {
		active = false;
		remainingTicks = 0;
		currentTick = 0;
		time = 0.0f;
		intensity = 0.0f;
		baseIntensity = 1.0f;
	}
}
