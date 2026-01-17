package net.mcreator.jjkstrongest.procedures;

public class CleaveDistortionStateProcedure {
	public static final CleaveDistortionStateProcedure INSTANCE = new CleaveDistortionStateProcedure();
	public volatile boolean active = false;
	public volatile int remainingTicks = 0;
	public volatile int totalDuration = 0;
	public volatile float intensity = 1.0f;
	// slash data
	public volatile float[] slash1 = new float[4];
	public volatile float[] slash2 = new float[4];
	public volatile float[] slash3 = new float[4];
	public volatile float[] slash4 = new float[4];
	public volatile int slashCount = 0;
	private float[][] pendingSlashes = new float[4][4];
	private int[] slashActivationTick = new int[4];
	private boolean[] slashShakeTriggered = new boolean[4]; // NEW: track if shake triggered
	private int currentTick = 0;
	private boolean startFlashTriggered = false;
	private boolean endFlashTriggered = false;
	private boolean endShakeTriggered = false; // NEW: track end shake
	// trigger with random slashes

	public void triggerRandom(int durationTicks, float intensity, int numSlashes) {
		this.active = true;
		this.remainingTicks = durationTicks;
		this.totalDuration = durationTicks;
		this.intensity = intensity;
		this.slashCount = Math.min(numSlashes, 4);
		this.currentTick = 0;
		this.startFlashTriggered = false;
		this.endFlashTriggered = false;
		this.endShakeTriggered = false;
		// reset shake triggers
		for (int i = 0; i < 4; i++) {
			slashShakeTriggered[i] = false;
		}
		// generate slashes
		for (int i = 0; i < slashCount; i++) {
			pendingSlashes[i][0] = (float) (0.2 + Math.random() * 0.6);
			pendingSlashes[i][1] = (float) (0.2 + Math.random() * 0.6);
			pendingSlashes[i][2] = (float) (Math.random() * Math.PI);
			pendingSlashes[i][3] = (float) (0.8 + Math.random() * 0.4);
			if (i == 0) {
				slashActivationTick[i] = 0;
			} else {
				float interval = (float) durationTicks / (float) slashCount;
				slashActivationTick[i] = (int) (interval * i);
			}
		}
		copySlashData(0, slash1);
		clearSlash(slash2);
		clearSlash(slash3);
		clearSlash(slash4);
	}

	private void copySlashData(int slashIndex, float[] target) {
		if (slashIndex < slashCount) {
			System.arraycopy(pendingSlashes[slashIndex], 0, target, 0, 4);
		}
	}

	private void clearSlash(float[] slash) {
		slash[0] = 0;
		slash[1] = 0;
		slash[2] = 0;
		slash[3] = 0;
	}

	private float[] getSlashArray(int index) {
		switch (index) {
			case 0 :
				return slash1;
			case 1 :
				return slash2;
			case 2 :
				return slash3;
			case 3 :
				return slash4;
			default :
				return slash1;
		}
	}

	public int getActiveSlashCount() {
		int count = 0;
		if (slash1[3] > 0)
			count++;
		if (slash2[3] > 0)
			count++;
		if (slash3[3] > 0)
			count++;
		if (slash4[3] > 0)
			count++;
		return count;
	}

	public boolean shouldTriggerStartFlash() {
		return !startFlashTriggered && currentTick == 0;
	}

	public boolean shouldTriggerEndFlash() {
		return !endFlashTriggered && remainingTicks <= 0;
	}

	public boolean shouldTriggerEndShake() {
		return !endShakeTriggered && remainingTicks <= 0;
	}

	public void markStartFlashTriggered() {
		startFlashTriggered = true;
	}

	public void markEndFlashTriggered() {
		endFlashTriggered = true;
	}

	public void markEndShakeTriggered() {
		endShakeTriggered = true;
	}

	// check if a slash just activated (for shake trigger)
	public int getNewlyActivatedSlash() {
		for (int i = 0; i < slashCount; i++) {
			if (currentTick >= slashActivationTick[i] && !slashShakeTriggered[i]) {
				slashShakeTriggered[i] = true;
				return i;
			}
		}
		return -1; // no new slash
	}

	// update each tick
	public void tick() {
		if (remainingTicks > 0) {
			currentTick = totalDuration - remainingTicks;
			// activate slashes progressively
			for (int i = 0; i < slashCount; i++) {
				if (currentTick >= slashActivationTick[i]) {
					float[] targetSlash = getSlashArray(i);
					if (targetSlash[3] == 0) {
						copySlashData(i, targetSlash);
					}
				}
			}
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
		startFlashTriggered = false;
		endFlashTriggered = false;
		endShakeTriggered = false;
		for (int i = 0; i < 4; i++) {
			slashShakeTriggered[i] = false;
		}
	}
}
