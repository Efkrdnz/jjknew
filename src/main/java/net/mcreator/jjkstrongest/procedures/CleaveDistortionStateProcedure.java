package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.s;

public class CleaveDistortionStateProcedure {
	public static final CleaveDistortionStateProcedure INSTANCE = new CleaveDistortionStateProcedure();
	public volatile boolean active = false;
	public volatile int remainingTicks = 0;
	public volatile int totalDuration = 0;
	public volatile float intensity = 1.0f;
	public volatile float[] slash1 = new float[4];
	public volatile float[] slash2 = new float[4];
	public volatile float[] slash3 = new float[4];
	public volatile float[] slash4 = new float[4];
	public volatile float[] slash5 = new float[4];
	public volatile float[] slash6 = new float[4];
	public volatile float[] slash7 = new float[4];
	public volatile float[] slash8 = new float[4];
	public volatile int slashCount = 0;
	private final float[][] pendingSlashes = new float[8][4];
	private final float[] baseStrength = new float[8];
	private final int[] slashActivationTick = new int[8];
	private final boolean[] slashShakeTriggered = new boolean[8];
	private int currentTick = 0;
	private boolean startFlashTriggered = false;
	private boolean endFlashTriggered = false;
	private boolean endShakeTriggered = false;
	// 1-tick delay start (replaces Timer)
	public volatile int pendingStartDelayTicks = 0;
	private int pendingDuration = 0;
	private float pendingIntensity = 1.0f;
	private int pendingSlashesCount = 0;

	public void scheduleTrigger(int delayTicks, int durationTicks, float intensity, int numSlashes) {
		this.pendingStartDelayTicks = Math.max(0, delayTicks);
		this.pendingDuration = durationTicks;
		this.pendingIntensity = intensity;
		this.pendingSlashesCount = numSlashes;
	}

	public void triggerRandom(int durationTicks, float intensity, int numSlashes) {
		this.active = true;
		this.remainingTicks = Math.max(1, durationTicks);
		this.totalDuration = Math.max(1, durationTicks);
		this.intensity = intensity;
		this.slashCount = Math.min(Math.max(1, numSlashes), 8);
		this.currentTick = 0;
		this.startFlashTriggered = false;
		this.endFlashTriggered = false;
		this.endShakeTriggered = false;
		for (int i = 0; i < 8; i++) {
			slashShakeTriggered[i] = false;
			baseStrength[i] = 0.0f;
		}
		// irregular activation feels better than evenly spaced
		for (int i = 0; i < slashCount; i++) {
			pendingSlashes[i][0] = (float) (0.18 + Math.random() * 0.64);
			pendingSlashes[i][1] = (float) (0.18 + Math.random() * 0.64);
			pendingSlashes[i][2] = (float) (Math.random() * Math.PI);
			baseStrength[i] = (float) (0.85 + Math.random() * 0.55);
			if (slashCount == 1) {
				slashActivationTick[i] = 0;
			} else {
				float t = (float) i / (float) (slashCount - 1);
				// bias toward mid burst + late finisher
				float shaped = (float) (0.15 + 0.85 * (t * t));
				slashActivationTick[i] = (int) (shaped * (float) durationTicks);
			}
			// strength starts at 0, grows when activated
			pendingSlashes[i][3] = 0.0f;
		}
		clearAllSlashArrays();
	}

	private void clearAllSlashArrays() {
		clearSlash(slash1);
		clearSlash(slash2);
		clearSlash(slash3);
		clearSlash(slash4);
		clearSlash(slash5);
		clearSlash(slash6);
		clearSlash(slash7);
		clearSlash(slash8);
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
			case 4 :
				return slash5;
			case 5 :
				return slash6;
			case 6 :
				return slash7;
			case 7 :
				return slash8;
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
		if (slash5[3] > 0)
			count++;
		if (slash6[3] > 0)
			count++;
		if (slash7[3] > 0)
			count++;
		if (slash8[3] > 0)
			count++;
		return count;
	}

	public float getProgress01() {
		if (totalDuration <= 0)
			return 0.0f;
		return Math.min(1.0f, Math.max(0.0f, (float) currentTick / (float) totalDuration));
	}

	public boolean shouldTriggerStartFlash() {
		return !startFlashTriggered && active && currentTick == 0;
	}

	public boolean shouldTriggerEndFlash() {
		return !endFlashTriggered && !active && remainingTicks <= 0;
	}

	public boolean shouldTriggerEndShake() {
		return !endShakeTriggered && !active && remainingTicks <= 0;
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

	public int getNewlyActivatedSlash() {
		for (int i = 0; i < slashCount; i++) {
			if (currentTick >= slashActivationTick[i] && !slashShakeTriggered[i]) {
				slashShakeTriggered[i] = true;
				return i;
			}
		}
		return -1;
	}

	private static float smooth01(float t) {
		if (t <= 0)
			return 0;
		if (t >= 1)
			return 1;
		return t * t * (3f - 2f * t);
	}

	public void tick() {
		// handle delayed start (no Timer threads)
		if (pendingStartDelayTicks > 0) {
			pendingStartDelayTicks--;
			if (pendingStartDelayTicks <= 0) {
				triggerRandom(pendingDuration, pendingIntensity, pendingSlashesCount);
			}
		}
		if (!active)
			return;
		if (remainingTicks > 0) {
			currentTick = totalDuration - remainingTicks;
			// intensity curve: hard hit then settle
			float p = getProgress01();
			float spike = 1.0f + (1.35f * (1.0f - smooth01(p * 1.4f)));
			intensity = Math.max(0.05f, intensity * (0.75f + 0.25f * spike));
			// update slashes
			for (int i = 0; i < slashCount; i++) {
				if (currentTick >= slashActivationTick[i]) {
					float[] arr = getSlashArray(i);
					// copy position/angle once
					if (arr[3] == 0.0f && pendingSlashes[i][0] != 0.0f) {
						arr[0] = pendingSlashes[i][0];
						arr[1] = pendingSlashes[i][1];
						arr[2] = pendingSlashes[i][2];
					}
					// fade in/out strength (stored in w)
					int localTick = currentTick - slashActivationTick[i];
					float life01 = totalDuration <= 0 ? 0.0f : Math.min(1.0f, Math.max(0.0f, (float) localTick / (float) totalDuration));
					float inT = smooth01(Math.min(1.0f, life01 * 6.0f));
					float outT = 1.0f - smooth01(Math.min(1.0f, Math.max(0.0f, (life01 - 0.55f) * 3.0f)));
					float s = baseStrength[i] * inT * outT;
					arr[3] = s;
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
		totalDuration = 0;
		currentTick = 0;
		startFlashTriggered = false;
		endFlashTriggered = false;
		endShakeTriggered = false;
		pendingStartDelayTicks = 0;
		for (int i = 0; i < 8; i++) {
			slashShakeTriggered[i] = false;
			baseStrength[i] = 0.0f;
		}
		clearAllSlashArrays();
	}
}
