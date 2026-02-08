package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class BlackFlashQTEMasterProcedure {
	// Static key state tracker to prevent double-calls
	private static final java.util.Map<java.util.UUID, Boolean> keyPressed = new java.util.concurrent.ConcurrentHashMap<>();

	// Called when key is PRESSED
	public static void onKeyPress(Entity entity) {
		if (entity == null)
			return;
		java.util.UUID uuid = entity.getUUID();
		// Prevent double-press
		if (keyPressed.getOrDefault(uuid, false)) {
			System.out.println("[QTE] Already pressed, ignoring");
			return;
		}
		keyPressed.put(uuid, true);
		// Check cooldown
		double cooldown = entity.getPersistentData().getDouble("blackflash_qte_cooldown");
		if (cooldown > 0) {
			System.out.println("[QTE] BLOCKED - Cooldown: " + cooldown);
			return;
		}
		System.out.println("[QTE] Starting QTE");
		// Set chanting mode
		entity.getPersistentData().putString("chanting", "blackflash_qte");
		entity.getPersistentData().putDouble("ChantCounter", 0);
		// Apply slowness
		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, false));
		}
		// Start QTE on client
		if (entity.level().isClientSide()) {
			BlackFlashQTEStateProcedure.INSTANCE.startQTE();
		}
	}

	// Called when key is RELEASED
	public static void onKeyRelease(Entity entity) {
		if (entity == null)
			return;
		System.out.println("[QTE Master] Release called!");
		java.util.UUID uuid = entity.getUUID();
		// Prevent double-release
		Boolean wasPressed = keyPressed.get(uuid);
		if (wasPressed == null || !wasPressed) {
			System.out.println("[QTE Master] Key wasn't pressed, ignoring release");
			return;
		}
		// Clear key state
		keyPressed.put(uuid, false);
		System.out.println("[QTE Master] Key state cleared");
		String chanting = entity.getPersistentData().getString("chanting");
		double cooldown = entity.getPersistentData().getDouble("blackflash_qte_cooldown");
		System.out.println("[QTE Release] Called - Chanting: '" + chanting + "' | Cooldown: " + cooldown);
		// ONLY proceed if in QTE mode
		if (!chanting.equals("blackflash_qte")) {
			System.out.println("[QTE Release] BLOCKED - Not in QTE mode");
			return;
		}
		// Double-check cooldown
		if (cooldown > 0) {
			System.out.println("[QTE Release] BLOCKED - On cooldown");
			entity.getPersistentData().putString("chanting", "");
			return;
		}
		// Reset chanting
		entity.getPersistentData().putString("chanting", "");
		entity.getPersistentData().putDouble("ChantCounter", 0);
		LevelAccessor world = entity.level();
		// CLIENT: Check result and set flag
		if (world.isClientSide()) {
			boolean success = BlackFlashQTEStateProcedure.INSTANCE.endQTE();
			System.out.println("[QTE Client] Success: " + success);
			// Set flag if successful
			if (success) {
				entity.getPersistentData().putBoolean("guaranteed_blackflash", true);
				System.out.println("[QTE Client] Guaranteed Black Flash flag SET - NOW LEFT-CLICK TO PUNCH!");
			} else {
				// Apply cooldown only on miss
				entity.getPersistentData().putDouble("blackflash_qte_cooldown", 100);
				System.out.println("[QTE Client] Missed - cooldown applied");
			}
		}
	}
}

// Static holder for passing data from client to server
class BlackFlashQTEResultHolder {
	private static final java.util.Map<java.util.UUID, Boolean> results = new java.util.concurrent.ConcurrentHashMap<>();

	public static void setResult(java.util.UUID playerUUID, boolean success) {
		results.put(playerUUID, success);
	}

	public static Boolean getResult(java.util.UUID playerUUID) {
		return results.get(playerUUID);
	}

	public static void clearResult(java.util.UUID playerUUID) {
		results.remove(playerUUID);
	}
}
