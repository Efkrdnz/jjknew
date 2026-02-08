package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class BlackFlashQTERendererProcedure {
	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
		if (!BlackFlashQTEStateProcedure.INSTANCE.isActive())
			return;
		Minecraft mc = Minecraft.getInstance();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();
		// center position
		float centerX = screenWidth / 2.0f;
		float centerY = screenHeight / 2.0f;
		float radius = 50.0f; // circle radius
		// DEBUG: print every 10 frames to verify rendering
		if (mc.level.getGameTime() % 10 == 0) {
			float currentRotation = BlackFlashQTEStateProcedure.INSTANCE.getCurrentRotation();
			System.out.println("[QTE Renderer] Active! Rotation: " + currentRotation + " degrees");
		}
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		// render white circle
		renderCircle(poseStack, centerX, centerY, radius, 3.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		// render success zone (green arc)
		float zoneStart = BlackFlashQTEStateProcedure.INSTANCE.getSuccessZoneStart();
		float zoneEnd = BlackFlashQTEStateProcedure.INSTANCE.getSuccessZoneEnd();
		renderArc(poseStack, centerX, centerY, radius, zoneStart, zoneEnd, 4.0f, 0.0f, 1.0f, 0.0f, 0.8f);
		// render spinning red line indicator (thin and visible)
		float currentRotation = BlackFlashQTEStateProcedure.INSTANCE.getCurrentRotation();
		// render line from center to edge (much more visible than dot)
		renderLine(poseStack, centerX, centerY, radius, currentRotation, 1.5f, 1.0f, 0.0f, 0.0f, 1.0f);
		poseStack.popPose();
	}

	// render line from center to edge (spinning indicator)
	private static void renderLine(PoseStack poseStack, float centerX, float centerY, float radius, float angle, float thickness, float r, float g, float b, float a) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		// convert angle to radians and adjust (subtract 90 for 12 o'clock start)
		float angleRad = (float) Math.toRadians(angle - 90);
		float endX = centerX + (float) Math.cos(angleRad) * radius;
		float endY = centerY + (float) Math.sin(angleRad) * radius;
		// perpendicular for thickness
		float perpX = -(float) Math.sin(angleRad) * thickness;
		float perpY = (float) Math.cos(angleRad) * thickness;
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		// draw thick line as quad
		buffer.vertex(matrix, centerX - perpX, centerY - perpY, 100).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, centerX + perpX, centerY + perpY, 100).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, endX + perpX, endY + perpY, 100).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, endX - perpX, endY - perpY, 100).color(r, g, b, a).endVertex();
		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	// render circle outline
	private static void renderCircle(PoseStack poseStack, float centerX, float centerY, float radius, float thickness, float r, float g, float b, float a) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
		int segments = 60;
		for (int i = 0; i <= segments; i++) {
			float angle = (float) (2 * Math.PI * i / segments);
			float cos = (float) Math.cos(angle);
			float sin = (float) Math.sin(angle);
			// outer vertex
			float outerX = centerX + cos * (radius + thickness);
			float outerY = centerY + sin * (radius + thickness);
			buffer.vertex(matrix, outerX, outerY, 0).color(r, g, b, a).endVertex();
			// inner vertex
			float innerX = centerX + cos * radius;
			float innerY = centerY + sin * radius;
			buffer.vertex(matrix, innerX, innerY, 0).color(r, g, b, a).endVertex();
		}
		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	// render arc segment (for success zone)
	private static void renderArc(PoseStack poseStack, float centerX, float centerY, float radius, float startAngle, float endAngle, float thickness, float r, float g, float b, float a) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
		// convert to radians and adjust for minecraft coordinate system
		// subtract 90 to make 0 degrees = top (12 o'clock)
		float startRad = (float) Math.toRadians(startAngle - 90);
		float endRad = (float) Math.toRadians(endAngle - 90);
		// handle wraparound
		if (endRad < startRad) {
			endRad += 2 * Math.PI;
		}
		int segments = 30;
		for (int i = 0; i <= segments; i++) {
			float t = (float) i / segments;
			float angle = startRad + (endRad - startRad) * t;
			float cos = (float) Math.cos(angle);
			float sin = (float) Math.sin(angle);
			// outer vertex
			float outerX = centerX + cos * (radius + thickness);
			float outerY = centerY + sin * (radius + thickness);
			buffer.vertex(matrix, outerX, outerY, 0).color(r, g, b, a).endVertex();
			// inner vertex
			float innerX = centerX + cos * radius;
			float innerY = centerY + sin * radius;
			buffer.vertex(matrix, innerX, innerY, 0).color(r, g, b, a).endVertex();
		}
		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	// render spinning point on circle edge
	private static void renderPoint(PoseStack poseStack, float centerX, float centerY, float radius, float angle, float size, float r, float g, float b, float a) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		// convert angle to radians and adjust (subtract 90 for 12 o'clock start)
		float angleRad = (float) Math.toRadians(angle - 90);
		float pointX = centerX + (float) Math.cos(angleRad) * radius;
		float pointY = centerY + (float) Math.sin(angleRad) * radius;
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		// draw point as filled circle (with Z=100 to be on top)
		buffer.vertex(matrix, pointX, pointY, 100).color(r, g, b, a).endVertex();
		int segments = 16;
		for (int i = 0; i <= segments; i++) {
			float circleAngle = (float) (2 * Math.PI * i / segments);
			float offsetX = (float) Math.cos(circleAngle) * size;
			float offsetY = (float) Math.sin(circleAngle) * size;
			buffer.vertex(matrix, pointX + offsetX, pointY + offsetY, 100).color(r, g, b, a).endVertex();
		}
		BufferUploader.drawWithShader(buffer.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}
}
