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
public class BlackFlashClientSyncProcedure {
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
		if (player.getPersistentData().contains("blackflash_shader_trigger")) {
			long triggerTime = player.getPersistentData().getLong("blackflash_shader_trigger");
			if (triggerTime != lastTriggerTime) {
				lastTriggerTime = triggerTime;
				int duration = player.getPersistentData().getInt("blackflash_shader_ticks");
				float intensity = player.getPersistentData().getFloat("blackflash_shader_intensity");
				// SEQUENCE 1: Impact frame (1 tick) - Black/Red theme
				ImpactFrameStateProcedure.INSTANCE.triggerCharged(1, 1.0f, 2.0f, 2.5f);
				// SEQUENCE 2: Screen shake
				TriggerScreenShakeProcedure.execute(world, player, 8, 4.0f);
				// SEQUENCE 3: Schedule shader after impact frame (1 tick delay)
				scheduleShader(duration, intensity);
				// clear NBT
				player.getPersistentData().remove("blackflash_shader_trigger");
				player.getPersistentData().remove("blackflash_shader_ticks");
				player.getPersistentData().remove("blackflash_shader_intensity");
			}
		}
	}

	private static void scheduleShader(int duration, float intensity) {
		// schedule shader to start after 1 tick (after impact frame)
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				Minecraft.getInstance().execute(() -> {
					BlackFlashShaderStateProcedure.INSTANCE.trigger(duration, intensity);
				});
			}
		}, 50); // 50ms = 1 tick
	}
}
