package net.efkrdnz.jjkstrongest;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(modid = "jjk_strongest", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class DisableHealthBar {
	@SubscribeEvent
	public static void RenderHealthBar(RenderGuiLayerEvent.Pre event) {
        Entity entity = Minecraft.getInstance().player;
        if (entity == null)
            return;
            
		if (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
                event.setCanceled(true);
        }
	}
}
