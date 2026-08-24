package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class ClientFullbrightGammaProcedure {
	// toggles client gamma without overriding real night vision visuals
	private static boolean applied = false;
	private static double savedGamma = 1.0;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null) {
			if (applied) {
				mc.options.gamma().set(savedGamma);
				applied = false;
			}
			return;
		}
		boolean full_bright = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES).fullbright;
		boolean hasNV = player.hasEffect(MobEffects.NIGHT_VISION);
		if (full_bright && !hasNV) {
			if (!applied) {
				savedGamma = mc.options.gamma().get();
				applied = true;
			}
			mc.options.gamma().set(15.0D);
		} else if (applied) {
			mc.options.gamma().set(savedGamma);
			applied = false;
		}
	}
}
