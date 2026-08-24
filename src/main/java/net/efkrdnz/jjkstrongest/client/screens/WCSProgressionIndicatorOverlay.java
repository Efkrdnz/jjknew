
package net.efkrdnz.jjkstrongest.client.screens;


import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.procedures.WCSProgressionReturnProcedure;
import net.efkrdnz.jjkstrongest.procedures.WCSProgressionReturn3Procedure;
import net.efkrdnz.jjkstrongest.procedures.WCSProgressionReturn2Procedure;
import net.efkrdnz.jjkstrongest.procedures.WCSProgressionReturn1Procedure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

@EventBusSubscriber({Dist.CLIENT})
public class WCSProgressionIndicatorOverlay {
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
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if (WCSProgressionReturnProcedure.execute(entity)) {
			if (WCSProgressionReturn1Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("jjk_strongest:textures/screens/trianglef1.png"), w / 2 + -9, h / 2 + -22, 0, 0, 17, 16, 17, 16);
			}
			if (WCSProgressionReturn3Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("jjk_strongest:textures/screens/trianglef2.png"), w / 2 + 6, h / 2 + 4, 0, 0, 16, 16, 16, 16);
			}
			if (WCSProgressionReturn3Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("jjk_strongest:textures/screens/trianglef3.png"), w / 2 + -22, h / 2 + 4, 0, 0, 16, 16, 16, 16);
			}
			if (WCSProgressionReturn2Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("jjk_strongest:textures/screens/trianglef4.png"), w / 2 + -22, h / 2 + -8, 0, 0, 16, 17, 16, 17);
			}
			if (WCSProgressionReturn2Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("jjk_strongest:textures/screens/trianglef5.png"), w / 2 + 6, h / 2 + -8, 0, 0, 16, 17, 16, 17);
			}
		}
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}
