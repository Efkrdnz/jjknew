
package net.mcreator.jjkstrongest.client.screens;

import org.checkerframework.checker.units.qual.h;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.procedures.ReturnMovesetTESTProcedure;
import net.mcreator.jjkstrongest.procedures.ReturnChantTESTProcedure;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class MovesetDisplayTESTOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getWindow().getGuiScaledWidth();
		int h = event.getWindow().getGuiScaledHeight();
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
