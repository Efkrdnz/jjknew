package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class WCSClientSideLineRendererProcedure {
	@SubscribeEvent
	public static void renderLevel(RenderLevelStageEvent event) {
		// render after particles so it appears on top of world
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			Minecraft minecraft = Minecraft.getInstance();
			ClientLevel level = minecraft.level;
			Entity entity = minecraft.gameRenderer.getMainCamera().getEntity();
			if (level != null && entity != null) {
				renderWCSLine(event, entity);
			}
		}
	}

	private static void renderWCSLine(RenderLevelStageEvent event, Entity entity) {
		if (entity == null)
			return;
		// check if holding button (indicated by WorldSlashHolding flag)
		if (!entity.getPersistentData().getBoolean("WorldSlashHolding"))
			return;
		// get coordinates
		double x1 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_x1;
		double y1 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_y1;
		double z1 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_z1;
		// calculate current aim point
		Vec3 lookVec = entity.getLookAngle();
		Vec3 eyePos = entity.getEyePosition(event.getPartialTick());
		double x2 = eyePos.x + lookVec.x * 25;
		double y2 = eyePos.y + lookVec.y * 25;
		double z2 = eyePos.z + lookVec.z * 25;
		// player position (point 3)
		double x3 = eyePos.x;
		double y3 = eyePos.y;
		double z3 = eyePos.z;
		PoseStack poseStack = event.getPoseStack();
		Matrix4f matrix = poseStack.last().pose();
		Matrix4f projectionMatrix = event.getProjectionMatrix();
		// get camera position for world-space rendering
		Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		// setup rendering state
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest(); // render through walls
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.lineWidth(3.0F); // constant line width
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		poseStack.pushPose();
		// draw triangle edges
		buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
		// line from point 1 to point 2 (press to current aim)
		drawLine(buffer, matrix, x1 - camPos.x, y1 - camPos.y, z1 - camPos.z, x2 - camPos.x, y2 - camPos.y, z2 - camPos.z, 1.0f, 1.0f, 1.0f, 1.0f); // white
		// line from point 2 to point 3 (current aim to player)
		drawLine(buffer, matrix, x2 - camPos.x, y2 - camPos.y, z2 - camPos.z, x3 - camPos.x, y3 - camPos.y, z3 - camPos.z, 1.0f, 1.0f, 1.0f, 1.0f); // white
		// line from point 3 to point 1 (player to press)
		drawLine(buffer, matrix, x3 - camPos.x, y3 - camPos.y, z3 - camPos.z, x1 - camPos.x, y1 - camPos.y, z1 - camPos.z, 1.0f, 1.0f, 1.0f, 1.0f); // white
		tesselator.end();
		// draw points at vertices
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		// point 1 (red)
		drawPoint(buffer, matrix, x1 - camPos.x, y1 - camPos.y, z1 - camPos.z, 1.0f, 0.0f, 0.0f, 1.0f, 0.3f);
		// point 2 (green)
		drawPoint(buffer, matrix, x2 - camPos.x, y2 - camPos.y, z2 - camPos.z, 0.0f, 1.0f, 0.0f, 1.0f, 0.3f);
		// point 3 (blue)
		drawPoint(buffer, matrix, x3 - camPos.x, y3 - camPos.y, z3 - camPos.z, 0.0f, 0.5f, 1.0f, 1.0f, 0.3f);
		tesselator.end();
		poseStack.popPose();
		// restore rendering state
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1.0F);
	}

	// draws line with multiple segments for better visibility
	private static void drawLine(BufferBuilder buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
		int segments = 20;
		for (int i = 0; i < segments; i++) {
			double t1 = (double) i / segments;
			double t2 = (double) (i + 1) / segments;
			double sx1 = x1 + (x2 - x1) * t1;
			double sy1 = y1 + (y2 - y1) * t1;
			double sz1 = z1 + (z2 - z1) * t1;
			double sx2 = x1 + (x2 - x1) * t2;
			double sy2 = y1 + (y2 - y1) * t2;
			double sz2 = z1 + (z2 - z1) * t2;
			buffer.vertex(matrix, (float) sx1, (float) sy1, (float) sz1).color(r, g, b, a).endVertex();
			buffer.vertex(matrix, (float) sx2, (float) sy2, (float) sz2).color(r, g, b, a).endVertex();
		}
	}

	// draws billboard point that always faces camera
	private static void drawPoint(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z, float r, float g, float b, float a, float size) {
		// get camera's right and up vectors
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 pointPos = new Vec3(x + cameraPos.x, y + cameraPos.y, z + cameraPos.z);
		Vec3 toCamera = cameraPos.subtract(pointPos).normalize();
		// create perpendicular vectors for billboard
		Vec3 right = new Vec3(-toCamera.z, 0, toCamera.x).normalize().scale(size);
		Vec3 up = new Vec3(0, 1, 0).scale(size);
		// draw quad facing camera
		buffer.vertex(matrix, (float) (x - right.x - up.x), (float) (y - right.y - up.y), (float) (z - right.z - up.z)).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, (float) (x - right.x + up.x), (float) (y - right.y + up.y), (float) (z - right.z + up.z)).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, (float) (x + right.x + up.x), (float) (y + right.y + up.y), (float) (z + right.z + up.z)).color(r, g, b, a).endVertex();
		buffer.vertex(matrix, (float) (x + right.x - up.x), (float) (y + right.y - up.y), (float) (z + right.z - up.z)).color(r, g, b, a).endVertex();
	}
}
