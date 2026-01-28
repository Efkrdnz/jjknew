package net.mcreator.jjkstrongest.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.mcreator.jjkstrongest.client.MalevolentShrineSlashManager;
import net.mcreator.jjkstrongest.client.MalevolentShrineClientTicker;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MalevolentShrineClientTicker {
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		// tick all slashes
		MalevolentShrineSlashManager.tick();
	}
}
