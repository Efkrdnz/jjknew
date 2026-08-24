package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class BlackFlashQTEMasterProcedure {
	// Static key state tracker to prevent double-calls
	private static final java.util.Map<java.util.UUID, Boolean> keyPressed = new java.util.concurrent.ConcurrentHashMap<>();

	// deterministic zone start so client+server match without networking
	private static float computeZoneStart(Entity entity) {
		long tick = entity.level().getGameTime();
		long u1 = entity.getUUID().getMostSignificantBits();
		long u2 = entity.getUUID().getLeastSignificantBits();
		long seed = u1 ^ (u2 * 31L) ^ (tick * 17L);
		java.util.Random r = new java.util.Random(seed);
		return 90.0f + r.nextFloat() * 270.0f;
	}

	// Called when key is PRESSED
	public static void onKeyPress(Entity entity) {
		if (entity == null)
			return;
		// Check cooldown first
		double cooldown = entity.getPersistentData().getDouble("blackflash_qte_cooldown");
		if (cooldown > 0) {
			System.out.println("[QTE] BLOCKED - Cooldown: " + cooldown);
			return;
		}
		java.util.UUID uuid = entity.getUUID();
		// Prevent double-press
		if (keyPressed.getOrDefault(uuid, false)) {
			System.out.println("[QTE] Already pressed, ignoring");
			return;
		}
		keyPressed.put(uuid, true);
		System.out.println("[QTE] Starting QTE");
		entity.getPersistentData().putLong("blackflash_qte_start_tick", entity.level().getGameTime());
		// Set chanting mode
		entity.getPersistentData().putString("chanting", "blackflash_qte");
		entity.getPersistentData().putDouble("ChantCounter", 0);
		// Apply slowness (client-only so cooldown check matches)
		if (entity.level().isClientSide() && entity instanceof LivingEntity livingEntity) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, false));
		}
		// Start QTE on client with SAME zone as server
		if (entity.level().isClientSide()) {
			float zoneStart = computeZoneStart(entity);
			BlackFlashQTEStateProcedure.INSTANCE.startQTE(zoneStart);
		}
	}

	// Called when key is RELEASED
	public static void onKeyRelease(Entity entity) {
		if (entity == null)
			return;
		System.out.println("[QTE Master] Release called!");
		java.util.UUID uuid = entity.getUUID();
		Boolean wasPressed = keyPressed.get(uuid);
		if (wasPressed == null || !wasPressed) {
			System.out.println("[QTE Master] Key wasn't pressed, ignoring release");
			return;
		}
		keyPressed.put(uuid, false);
		System.out.println("[QTE Master] Key state cleared");
		String chanting = entity.getPersistentData().getString("chanting");
		double cooldown = entity.getPersistentData().getDouble("blackflash_qte_cooldown");
		System.out.println("[QTE Release] Called - Chanting: '" + chanting + "' | Cooldown: " + cooldown);
		if (!chanting.equals("blackflash_qte")) {
			System.out.println("[QTE Release] BLOCKED - Not in QTE mode");
			return;
		}
		if (cooldown > 0) {
			System.out.println("[QTE Release] BLOCKED - On cooldown");
			entity.getPersistentData().putString("chanting", "");
			return;
		}
		entity.getPersistentData().putString("chanting", "");
		entity.getPersistentData().putDouble("ChantCounter", 0);
		LevelAccessor world = entity.level();
		// client: end qte + do punch using real attack packet
		if (world.isClientSide()) {
			boolean success = BlackFlashQTEStateProcedure.INSTANCE.endQTE();
			System.out.println("[QTE Client] Success: " + success);
			// punch now (this actually hits because it uses normal attack packet)
			ClientLookPunchProcedure.execute(entity);
			// cooldown only on miss (client-side to stop the slowness issue)
			if (!success) {
				entity.getPersistentData().putDouble("blackflash_qte_cooldown", 100);
			}
			return;
		}
		// server: keep your server cooldown logic if you want it here, but do not punch here
		long startTick = entity.getPersistentData().getLong("blackflash_qte_start_tick");
		long nowTick = entity.level().getGameTime();
		float rotation = (float) (((nowTick - startTick) * 18.0f) % 360.0f);
		float zoneStart = computeZoneStart(entity);
		float zoneEnd = (zoneStart + 30.0f) % 360.0f;
		boolean inZone = (zoneEnd < zoneStart) ? (rotation >= zoneStart || rotation <= zoneEnd) : (rotation >= zoneStart && rotation <= zoneEnd);
		boolean timedOut = (nowTick - startTick) > 20;
		boolean success = inZone && !timedOut;
		System.out.println("[QTE Server] success: " + success);
		if (!success) {
			entity.getPersistentData().putDouble("blackflash_qte_cooldown", 100);
		}
	}
}
