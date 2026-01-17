
package net.mcreator.jjkstrongest.client.screens;

import org.checkerframework.checker.units.qual.h;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.procedures.SparkRedReturnProcedure;
import net.mcreator.jjkstrongest.procedures.SparkRedReturn3Procedure;
import net.mcreator.jjkstrongest.procedures.SparkRedReturn2Procedure;
import net.mcreator.jjkstrongest.procedures.SparkPurpleReturnProcedure;
import net.mcreator.jjkstrongest.procedures.SparkPurpleReturn3Procedure;
import net.mcreator.jjkstrongest.procedures.SparkPurpleReturn2Procedure;
import net.mcreator.jjkstrongest.procedures.SparkBlueReturnProcedure;
import net.mcreator.jjkstrongest.procedures.SparkBlueReturn3Procedure;
import net.mcreator.jjkstrongest.procedures.SparkBlueReturn2Procedure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class LimitlessChantedColorsOverlay {
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
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if (true) {
			if (SparkBlueReturnProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_blue1.png"), 1, 1, 0, 0, 32, 32, 32, 32);
			}
			if (SparkBlueReturn2Procedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_blue1.png"), 34, 1, 0, 0, 32, 32, 32, 32);
			}
			if (SparkBlueReturn3Procedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_blue1.png"), 67, 1, 0, 0, 32, 32, 32, 32);
			}
			if (SparkRedReturnProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_red1.png"), w - 33, 1, 0, 0, 32, 32, 32, 32);
			}
			if (SparkRedReturn2Procedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_red1.png"), w - 66, 1, 0, 0, 32, 32, 32, 32);
			}
			if (SparkRedReturn3Procedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_red1.png"), w - 99, 1, 0, 0, 32, 32, 32, 32);
			}
			if (SparkPurpleReturnProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_purple1.png"), w / 2 + -26, h - 57, 0, 0, 16, 16, 16, 16);
			}
			if (SparkPurpleReturn2Procedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_purple1.png"), w / 2 + -8, h - 57, 0, 0, 16, 16, 16, 16);
			}
			if (SparkPurpleReturn3Procedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("jjk_strongest:textures/screens/spark_purple1.png"), w / 2 + 10, h - 57, 0, 0, 16, 16, 16, 16);
			}
		}
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}
