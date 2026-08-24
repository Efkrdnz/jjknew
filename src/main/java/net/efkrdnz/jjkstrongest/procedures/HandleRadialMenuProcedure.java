package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModKeyMappings;

import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber
public class HandleRadialMenuProcedure {
	private static boolean wasPressed = false;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
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
			if (charName.equals("gojo") || charName.equals("sukuna") || charName.equals("yuji") || charName.equals("inumaki")) {
				mc.setScreen(new CharacterRadialScreenProcedure.CharacterRadialScreen(charName));
			}
		}
		wasPressed = isPressed;
	}
}
