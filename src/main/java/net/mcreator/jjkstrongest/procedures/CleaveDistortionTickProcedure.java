package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CleaveDistortionTickProcedure {
	private static long lastTriggerTime = 0;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		final Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		Player player = mc.player;
		Level world = mc.level;
		// check for server trigger
		if (player.getPersistentData().contains("cleave_distortion_trigger")) {
			long triggerTime = player.getPersistentData().getLong("cleave_distortion_trigger");
			if (triggerTime != lastTriggerTime) {
				lastTriggerTime = triggerTime;
				int duration = player.getPersistentData().getInt("cleave_distortion_ticks");
				float intensity = player.getPersistentData().getFloat("cleave_distortion_intensity");
				int slashes = player.getPersistentData().getInt("cleave_distortion_slashes");
				// SEQUENCE 1: Start flash (1 tick)
				ImpactFrameStateProcedure.INSTANCE.triggerCharged(1, 1.0f, 2.0f, 2.5f);
				// SEQUENCE 2: Schedule distortion after 1 tick
				scheduleDistortion(duration, intensity, slashes);
				// clear
				player.getPersistentData().remove("cleave_distortion_trigger");
				player.getPersistentData().remove("cleave_distortion_ticks");
				player.getPersistentData().remove("cleave_distortion_intensity");
				player.getPersistentData().remove("cleave_distortion_slashes");
			}
		}
		var state = CleaveDistortionStateProcedure.INSTANCE;
		// CHECK FOR NEWLY ACTIVATED SLASH (trigger small shake)
		int newSlashIndex = state.getNewlyActivatedSlash();
		if (newSlashIndex >= 0) {
			System.out.println("[Cleave Distortion] Slash " + (newSlashIndex + 1) + " activated - small shake");
			TriggerScreenShakeProcedure.execute(world, player, 5, 2.0f);
		}
		// tick distortion
		CleaveDistortionStateProcedure.INSTANCE.tick();
		// check if distortion just ended
		if (state.shouldTriggerEndFlash()) {
			// SEQUENCE 3: End flash (2 ticks)
			ImpactFrameStateProcedure.INSTANCE.triggerCharged(5, 1.0f, 2.0f, 2.5f);
			state.markEndFlashTriggered();
		}
		// BIG SHAKE at the end
		if (state.shouldTriggerEndShake()) {
			System.out.println("[Cleave Distortion] Effect ending - BIG SHAKE");
			TriggerScreenShakeProcedure.execute(world, player, 10, 5.0f);
			state.markEndShakeTriggered();
		}
	}

	private static void scheduleDistortion(int duration, float intensity, int slashes) {
		// schedule distortion to start after 1 tick (after start flash)
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				Minecraft.getInstance().execute(() -> {
					CleaveDistortionStateProcedure.INSTANCE.triggerRandom(duration, intensity, slashes);
				});
			}
		}, 50); // 50ms = 1 tick
	}
}
