package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ChargeIndicatorOverlayProcedure {
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
		if (event.getOverlay().id().toString().equals("minecraft:crosshair")) {
			execute(event.getGuiGraphics());
		}
	}

	public static void execute(GuiGraphics guiGraphics) {
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		String chantingType = player.getPersistentData().getString("chanting");
		double chantCounter = player.getPersistentData().getDouble("ChantCounter");
		// only render if actively chanting
		if (chantingType.isEmpty() || chantCounter <= 0)
			return;
		int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		// render ring
		renderChargeRing(guiGraphics, screenWidth, screenHeight);
		// render charge text
		renderChargeText(guiGraphics, chantingType, chantCounter, screenWidth, screenHeight);
	}

	// render the circular charge ring
	private static void renderChargeRing(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
		VertexBuffer vertexBuffer = ChargeRingShapeProcedure.execute();
		if (vertexBuffer == null)
			return;
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		// position at center of screen, slightly below crosshair
		poseStack.translate(screenWidth / 2.0, screenHeight / 2.0 + 25, 0);
		double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
		poseStack.scale((float) (15 + 5 * guiScale), (float) (15 + 5 * guiScale), (float) (15 + 5 * guiScale));
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		vertexBuffer.bind();
		vertexBuffer.drawWithShader(matrix, RenderSystem.getProjectionMatrix(), GameRenderer.getPositionColorShader());
		VertexBuffer.unbind();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		poseStack.popPose();
	}

	// render charge level text
	private static void renderChargeText(GuiGraphics guiGraphics, String chantingType, double chantCounter, int screenWidth, int screenHeight) {
		String displayText = getChargeDisplayText(chantingType, chantCounter);
		int color = getTextColor(chantingType, chantCounter);
		if (!displayText.isEmpty()) {
			int textWidth = Minecraft.getInstance().font.width(displayText);
			int x = screenWidth / 2 - textWidth / 2;
			int y = screenHeight / 2 + 55;
			guiGraphics.drawString(Minecraft.getInstance().font, displayText, x, y, color, true);
		}
	}

	// get display text based on charge state
	private static String getChargeDisplayText(String chantingType, double chantCounter) {
		if (chantingType.equals("blue")) {
			if (chantCounter >= 30)
				return "EYES OF WISDOM";
			else if (chantCounter >= 20)
				return "TWILIGHT";
			else if (chantCounter >= 10)
				return "PHASE";
			else
				return "Charging Blue...";
		} else if (chantingType.equals("red")) {
			if (chantCounter >= 40)
				return "PILLARS OF LIGHT";
			else if (chantCounter >= 20)
				return "PARAMITA";
			else if (chantCounter >= 10)
				return "PHASE";
			else
				return "Charging Red...";
		} else if (chantingType.equals("purple")) {
			if (chantCounter >= 130)
				return "BETWEEN FRONT AND BACK";
			else if (chantCounter >= 110)
				return "CROW AND DECLARATION";
			else if (chantCounter >= 90)
				return "POLARISED LIGHT";
			else if (chantCounter >= 70)
				return "NINE ROPES";
			else
				return "Charging Purple...";
		} else if (chantingType.equals("dismantle")) {
			if (chantCounter >= 30)
				return "TWIN METEORS";
			else if (chantCounter >= 20)
				return "REPULSION";
			else if (chantCounter >= 10)
				return "DRAGON SCALES";
			else
				return "Charging Dismantle...";
		}
		return "Charging...";
	}

	// get text color based on technique
	private static int getTextColor(String chantingType, double chantCounter) {
		if (chantingType.equals("blue")) {
			if (chantCounter >= 30)
				return 0x0064FF;
			else if (chantCounter >= 20)
				return 0x64C8FF;
			else
				return 0x969696;
		} else if (chantingType.equals("red")) {
			if (chantCounter >= 40)
				return 0xFF0000;
			else if (chantCounter >= 20)
				return 0xFF6400;
			else
				return 0x969696;
		} else if (chantingType.equals("purple")) {
			if (chantCounter >= 130)
				return 0xC800FF;
			else if (chantCounter >= 70)
				return 0xA050FF;
			else
				return 0x8078C8;
		} else if (chantingType.equals("dismantle")) {
			if (chantCounter >= 30)
				return 0xC80000;
			else if (chantCounter >= 20)
				return 0xFF6400;
			else
				return 0xFFB464;
		}
		return 0xFFFFFF;
	}
}
