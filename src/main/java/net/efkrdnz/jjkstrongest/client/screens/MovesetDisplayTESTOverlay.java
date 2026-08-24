
package net.efkrdnz.jjkstrongest.client.screens;


import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.procedures.ReturnMovesetTESTProcedure;
import net.efkrdnz.jjkstrongest.procedures.ReturnChantTESTProcedure;

@EventBusSubscriber({Dist.CLIENT})
public class MovesetDisplayTESTOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getGuiGraphics().guiWidth();
		int h = event.getGuiGraphics().guiHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		if (true) {
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					ReturnMovesetTESTProcedure.execute(entity), w / 2 + -39, 6, -1, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					ReturnChantTESTProcedure.execute(entity), w / 2 + -39, 18, -1, false);
		}
	}
}
