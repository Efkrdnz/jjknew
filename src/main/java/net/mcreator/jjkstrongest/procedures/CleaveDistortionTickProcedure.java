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
		// trigger read
		if (player.getPersistentData().contains("cleave_distortion_trigger")) {
			long triggerTime = player.getPersistentData().getLong("cleave_distortion_trigger");
			if (triggerTime != lastTriggerTime) {
				lastTriggerTime = triggerTime;
				int duration = player.getPersistentData().getInt("cleave_distortion_ticks");
				float intensity = player.getPersistentData().getFloat("cleave_distortion_intensity");
				int slashes = player.getPersistentData().getInt("cleave_distortion_slashes");
				// start flash
				ImpactFrameStateProcedure.INSTANCE.triggerCharged(1, 1.0f, 2.0f, 2.5f);
				// schedule distortion start after 1 tick (no Timer threads)
				CleaveDistortionStateProcedure.INSTANCE.scheduleTrigger(1, duration, intensity, slashes);
				player.getPersistentData().remove("cleave_distortion_trigger");
				player.getPersistentData().remove("cleave_distortion_ticks");
				player.getPersistentData().remove("cleave_distortion_intensity");
				player.getPersistentData().remove("cleave_distortion_slashes");
			}
		}
		var state = CleaveDistortionStateProcedure.INSTANCE;
		int newSlashIndex = state.getNewlyActivatedSlash();
		if (newSlashIndex >= 0) {
			TriggerScreenShakeProcedure.execute(world, player, 5, 2.0f);
		}
		state.tick();
		if (state.shouldTriggerEndFlash()) {
			ImpactFrameStateProcedure.INSTANCE.triggerCharged(5, 1.0f, 2.0f, 2.5f);
			state.markEndFlashTriggered();
		}
		if (state.shouldTriggerEndShake()) {
			TriggerScreenShakeProcedure.execute(world, player, 10, 5.0f);
			state.markEndShakeTriggered();
		}
	}
}
