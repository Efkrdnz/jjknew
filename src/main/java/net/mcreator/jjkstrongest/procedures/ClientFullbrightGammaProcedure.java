package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class ClientFullbrightGammaProcedure {
	// toggles client gamma without overriding real night vision visuals
	private static boolean applied = false;
	private static double savedGamma = 1.0;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null) {
			if (applied) {
				mc.options.gamma().set(savedGamma);
				applied = false;
			}
			return;
		}
		boolean full_bright = player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables()).fullbright;
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
