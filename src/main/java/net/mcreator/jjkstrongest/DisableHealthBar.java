package net.mcreator.jjkstrongest;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(modid = "jjk_strongest", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DisableHealthBar {
	@SubscribeEvent
	public static void RenderHealthBar(RenderGuiOverlayEvent.Pre event) {
        Entity entity = Minecraft.getInstance().player;
        if (entity == null)
            return;
            
		if (VanillaGuiOverlay.PLAYER_HEALTH.type() == event.getOverlay()) {
                event.setCanceled(true);
        }
	}
}
