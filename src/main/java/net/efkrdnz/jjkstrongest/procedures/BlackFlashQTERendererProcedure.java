package net.efkrdnz.jjkstrongest.procedures;

import org.joml.Matrix4f;


import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

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

@EventBusSubscriber(value = Dist.CLIENT)
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
		float cx = screenWidth / 2.0f;
		float cy = screenHeight / 2.0f;
		float radius = 54.0f;
		float ringThickness = 4.0f;
		PoseStack ps = guiGraphics.pose();
		ps.pushPose();
		ps.translate(0, 0, 250);
		// interior: white -> grey radial fade
		renderFilledCircleGradient(ps, cx, cy, radius - 6.0f, 0.95f, 0.95f, 0.95f, 0.55f, // center (white)
				1f, 1f, 1f, 0.35f // edge (grey)
		);
		// outer ring: black outline + white ring
		renderRing(ps, cx, cy, radius, ringThickness + 2.0f, 0.25f, 0.25f, 0.25f, 0.95f);
		renderRing(ps, cx, cy, radius, ringThickness, 1f, 1f, 1f, 0.95f);
		// success zone: black outline + red arc
		float zoneStart = BlackFlashQTEStateProcedure.INSTANCE.getSuccessZoneStart();
		float zoneEnd = BlackFlashQTEStateProcedure.INSTANCE.getSuccessZoneEnd();
		renderArc(ps, cx, cy, radius, zoneStart, zoneEnd, ringThickness + 3.0f, 0f, 0f, 0f, 0.95f);
		renderArc(ps, cx, cy, radius, zoneStart, zoneEnd, ringThickness + 1.0f, 0.95f, 0.15f, 0.15f, 0.90f);
		// indicator: make it longer + slightly outside the ring
		float rot = BlackFlashQTEStateProcedure.INSTANCE.getCurrentRotation();
		// outline tick
		renderPerimeterTick(ps, cx, cy, radius, rot, 14.0f, 2.4f, // tickLen, halfWidth
				6.0f, // outwardOffset
				0f, 0f, 0f, 0.95f);
		// inner tick
		renderPerimeterTick(ps, cx, cy, radius, rot, 14.0f, 1.7f, 6.0f, 1.0f, 0.35f, 0.35f, 1.0f);
		ps.popPose();
	}

	// filled disc with center->edge gradient
	private static void renderFilledCircleGradient(PoseStack ps, float cx, float cy, float radius, float cr, float cg, float cb, float ca, float er, float eg, float eb, float ea) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		Matrix4f m = ps.last().pose();
		BufferBuilder buf = Tesselator.getInstance().getBuilder();
		buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		// center vertex = center color
		buf.vertex(m, cx, cy, 0).color(cr, cg, cb, ca).endVertex();
		// edge vertices = edge color
		int seg = 72;
		for (int i = 0; i <= seg; i++) {
			float ang = (float) (2 * Math.PI * i / seg);
			float x = cx + (float) Math.cos(ang) * radius;
			float y = cy + (float) Math.sin(ang) * radius;
			buf.vertex(m, x, y, 0).color(er, eg, eb, ea).endVertex();
		}
		BufferUploader.drawWithShader(buf.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void renderRing(PoseStack ps, float cx, float cy, float radius, float thickness, float r, float g, float b, float a) {
		float outerR = radius + (thickness * 0.5f);
		float innerR = radius - (thickness * 0.5f);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		Matrix4f m = ps.last().pose();
		BufferBuilder buf = Tesselator.getInstance().getBuilder();
		buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
		int seg = 96;
		for (int i = 0; i <= seg; i++) {
			float ang = (float) (2 * Math.PI * i / seg);
			float cos = (float) Math.cos(ang);
			float sin = (float) Math.sin(ang);
			buf.vertex(m, cx + cos * outerR, cy + sin * outerR, 0).color(r, g, b, a).endVertex();
			buf.vertex(m, cx + cos * innerR, cy + sin * innerR, 0).color(r, g, b, a).endVertex();
		}
		BufferUploader.drawWithShader(buf.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void renderArc(PoseStack ps, float cx, float cy, float radius, float startDeg, float endDeg, float thickness, float r, float g, float b, float a) {
		float outerR = radius + (thickness * 0.5f);
		float innerR = radius - (thickness * 0.5f);
		float startRad = (float) Math.toRadians(startDeg - 90);
		float endRad = (float) Math.toRadians(endDeg - 90);
		if (endRad < startRad)
			endRad += 2 * Math.PI;
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		Matrix4f m = ps.last().pose();
		BufferBuilder buf = Tesselator.getInstance().getBuilder();
		buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
		int seg = 72;
		for (int i = 0; i <= seg; i++) {
			float t = (float) i / seg;
			float ang = startRad + (endRad - startRad) * t;
			float cos = (float) Math.cos(ang);
			float sin = (float) Math.sin(ang);
			buf.vertex(m, cx + cos * outerR, cy + sin * outerR, 0).color(r, g, b, a).endVertex();
			buf.vertex(m, cx + cos * innerR, cy + sin * innerR, 0).color(r, g, b, a).endVertex();
		}
		BufferUploader.drawWithShader(buf.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	// short tick around perimeter (quad oriented radially)
	private static void renderPerimeterTick(PoseStack ps, float cx, float cy, float radius, float angleDeg, float tickLen, float halfWidth, float outwardOffset, float r, float g, float b, float a) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		float ang = (float) Math.toRadians(angleDeg - 90);
		float cos = (float) Math.cos(ang);
		float sin = (float) Math.sin(ang);
		float outerX = cx + cos * (radius + outwardOffset);
		float outerY = cy + sin * (radius + outwardOffset);
		float innerX = cx + cos * (radius + outwardOffset - tickLen);
		float innerY = cy + sin * (radius + outwardOffset - tickLen);
		float px = -sin * halfWidth;
		float py = cos * halfWidth;
		Matrix4f m = ps.last().pose();
		BufferBuilder buf = Tesselator.getInstance().getBuilder();
		buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buf.vertex(m, innerX - px, innerY - py, 0).color(r, g, b, a).endVertex();
		buf.vertex(m, innerX + px, innerY + py, 0).color(r, g, b, a).endVertex();
		buf.vertex(m, outerX + px, outerY + py, 0).color(r, g, b, a).endVertex();
		buf.vertex(m, outerX - px, outerY - py, 0).color(r, g, b, a).endVertex();
		BufferUploader.drawWithShader(buf.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}
}
