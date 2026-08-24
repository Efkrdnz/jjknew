package net.efkrdnz.jjkstrongest.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.efkrdnz.jjkstrongest.client.MalevolentShrineSlashManager;
import net.efkrdnz.jjkstrongest.client.MalevolentShrineClientTicker;

@EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MalevolentShrineClientTicker {
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		// tick all slashes
		MalevolentShrineSlashManager.tick();
	}
}
