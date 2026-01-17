package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModKeyMappings;

import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber
public class HandleRadialMenuProcedure {
	private static boolean wasPressed = false;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null)
				return;
			if (JjkStrongestModKeyMappings.ABILITYMENU == null) {
				return;
			}
			boolean isPressed = false;
			try {
				isPressed = JjkStrongestModKeyMappings.ABILITYMENU.isDown();
			} catch (Exception e) {
				return;
			}
			if (isPressed && !wasPressed) {
				Player player = mc.player;
				AtomicReference<String> character = new AtomicReference<>("");
				player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					character.set(capability.sorcerer);
				});
				String charName = character.get();
				if (charName.equals("gojo") || charName.equals("sukuna")) {
					mc.setScreen(new CharacterRadialScreenProcedure.CharacterRadialScreen(charName));
				}
			}
			wasPressed = isPressed;
		}
	}
}
