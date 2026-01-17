package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.entity.DomainUVEntity;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DomainSkyboxRenderProcedure {
	private static VertexBuffer skyboxBuffer = null;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		// render after blocks but before entities
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}
		Level level = mc.level;
		Player player = mc.player;
		Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
		DomainUVEntity nearest = null;
		double best = Double.MAX_VALUE;
		for (DomainUVEntity d : level.getEntitiesOfClass(DomainUVEntity.class, new AABB(playerPos, playerPos).inflate(64), e -> true)) {
			double ds = d.distanceToSqr(player);
			if (ds < best) {
				best = ds;
				nearest = d;
			}
		}
		if (nearest != null) {
			double radius = 30.0;
			double dx = player.getX() - nearest.getX();
			double dy = player.getY() - nearest.getY();
			double dz = player.getZ() - nearest.getZ();
			if ((dx * dx + dy * dy + dz * dz) <= radius * radius) {
				if (nearest.tickCount >= 80) {
					renderDomainSkybox(event.getPoseStack(), event.getProjectionMatrix());
				}
			}
		}
	}

	private static void renderDomainSkybox(PoseStack poseStack, Matrix4f projectionMatrix) {
		// disable depth so skybox renders over blocks
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, new ResourceLocation("jjk_strongest", "textures/unlimited_void3.png"));
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		if (skyboxBuffer == null) {
			BufferBuilder builder = Tesselator.getInstance().getBuilder();
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			buildSkyboxGeometry(builder);
			skyboxBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
			skyboxBuffer.bind();
			skyboxBuffer.upload(builder.end());
		} else {
			skyboxBuffer.bind();
		}
		Minecraft mc = Minecraft.getInstance();
		float size = mc.options.getEffectiveRenderDistance() * 16 * 4;
		poseStack.pushPose();
		poseStack.scale(size, size, size);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		skyboxBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionTexShader());
		VertexBuffer.unbind();
		poseStack.popPose();
		// IMPORTANT: Re-enable depth test so entities render normally
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}

	private static void buildSkyboxGeometry(BufferBuilder builder) {
		// bottom face
		builder.vertex(-0.5F, -0.5F, -0.5F).uv(0.0F, 0.0F).endVertex();
		builder.vertex(-0.5F, -0.5F, 0.5F).uv(0.0F, 0.5F).endVertex();
		builder.vertex(0.5F, -0.5F, 0.5F).uv(0.333F, 0.5F).endVertex();
		builder.vertex(0.5F, -0.5F, -0.5F).uv(0.333F, 0.0F).endVertex();
		// top face
		builder.vertex(-0.5F, 0.5F, 0.5F).uv(0.333F, 0.0F).endVertex();
		builder.vertex(-0.5F, 0.5F, -0.5F).uv(0.333F, 0.5F).endVertex();
		builder.vertex(0.5F, 0.5F, -0.5F).uv(0.666F, 0.5F).endVertex();
		builder.vertex(0.5F, 0.5F, 0.5F).uv(0.666F, 0.0F).endVertex();
		// back face
		builder.vertex(0.5F, 0.5F, 0.5F).uv(0.666F, 0.0F).endVertex();
		builder.vertex(0.5F, -0.5F, 0.5F).uv(0.666F, 0.5F).endVertex();
		builder.vertex(-0.5F, -0.5F, 0.5F).uv(1.0F, 0.5F).endVertex();
		builder.vertex(-0.5F, 0.5F, 0.5F).uv(1.0F, 0.0F).endVertex();
		// front face
		builder.vertex(-0.5F, 0.5F, 0.5F).uv(0.0F, 0.5F).endVertex();
		builder.vertex(-0.5F, -0.5F, 0.5F).uv(0.0F, 1.0F).endVertex();
		builder.vertex(-0.5F, -0.5F, -0.5F).uv(0.333F, 1.0F).endVertex();
		builder.vertex(-0.5F, 0.5F, -0.5F).uv(0.333F, 0.5F).endVertex();
		// left face  
		builder.vertex(-0.5F, 0.5F, -0.5F).uv(0.333F, 0.5F).endVertex();
		builder.vertex(-0.5F, -0.5F, -0.5F).uv(0.333F, 1.0F).endVertex();
		builder.vertex(0.5F, -0.5F, -0.5F).uv(0.666F, 1.0F).endVertex();
		builder.vertex(0.5F, 0.5F, -0.5F).uv(0.666F, 0.5F).endVertex();
		// right face
		builder.vertex(0.5F, 0.5F, -0.5F).uv(0.666F, 0.5F).endVertex();
		builder.vertex(0.5F, -0.5F, -0.5F).uv(0.666F, 1.0F).endVertex();
		builder.vertex(0.5F, -0.5F, 0.5F).uv(1.0F, 1.0F).endVertex();
		builder.vertex(0.5F, 0.5F, 0.5F).uv(1.0F, 0.5F).endVertex();
	}
}
