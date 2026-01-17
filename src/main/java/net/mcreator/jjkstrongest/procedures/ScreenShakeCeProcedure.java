package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ScreenShakeCeProcedure {
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		// tick down shake timer
		ScreenShakeStateProcedure.INSTANCE.tick();
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onCameraShake(ViewportEvent.ComputeCameraAngles event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		var shakeState = ScreenShakeStateProcedure.INSTANCE;
		if (!shakeState.active)
			return;
		float intensity = shakeState.getCurrentIntensity();
		// random shake direction
		if (Math.random() < 0.5) {
			event.setPitch((float) (event.getPitch() + (Math.random() * intensity)));
			event.setRoll((float) (event.getRoll() + (Math.random() * intensity)));
			event.setYaw((float) (event.getYaw() + (Math.random() * intensity)));
		} else {
			event.setPitch((float) (event.getPitch() - (Math.random() * intensity)));
			event.setRoll((float) (event.getRoll() - (Math.random() * intensity)));
			event.setYaw((float) (event.getYaw() - (Math.random() * intensity)));
		}
	}
}
