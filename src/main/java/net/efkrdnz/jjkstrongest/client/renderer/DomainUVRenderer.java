package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;

import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.client.model.Modelblank_entity;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class DomainUVRenderer extends MobRenderer<DomainUVEntity, Modelblank_entity<DomainUVEntity>> {
	public DomainUVRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	public boolean shouldRender(DomainUVEntity entity, Frustum frustum, double x, double y, double z) {
		return true;
	}

	@Override
	public void render(DomainUVEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		if (entity.tickCount >= 80) {
			renderWhiteBrushes(entity, partialTick, poseStack, bufferSource);
			renderRift(entity, partialTick, poseStack, bufferSource);
			renderBlackHole(entity, partialTick, poseStack, bufferSource);
		}
	}

	private void renderBlackHole(DomainUVEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.VOID_BLACKHOLE_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		float intensity = 1.0f;
		if (!JjkShaderManager.beginVoidBlackholeEffect(timeSeconds, intensity))
			return;
		com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
		poseStack.pushPose();
		poseStack.translate(18.0, 7.0, 0.0);
		poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));
		float size = 72.0f;
		poseStack.scale(size, size, size);
		renderCircularQuad(poseStack, bufferSource, JjkShaderManager.VOID_BLACKHOLE_RENDER_TYPE);
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.VOID_BLACKHOLE_RENDER_TYPE);
		}
		com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
	}

	private void renderRift(DomainUVEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.VOID_RIFT_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		float intensity = 1.0f;
		if (!JjkShaderManager.beginVoidRiftEffect(timeSeconds, intensity))
			return;
		com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
		poseStack.pushPose();
		poseStack.translate(0.0, 18.0, 0.0);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
		float size = 44.0f;
		poseStack.scale(size, size, size);
		renderCircularQuad(poseStack, bufferSource, JjkShaderManager.VOID_RIFT_RENDER_TYPE);
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.VOID_RIFT_RENDER_TYPE);
		}
		com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
	}

	private void renderWhiteBrushes(DomainUVEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.VOID_BRUSH_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		float brushSeed = 1.0f;
		float intensity = 0.8f;
		JjkShaderManager.beginVoidBrushEffect(timeSeconds, brushSeed, intensity);
		com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
		poseStack.pushPose();
		poseStack.translate(0.0, 0.0, 0.0);
		float sphereRadius = 25.2f;
		renderInvertedSphere(poseStack, bufferSource, sphereRadius);
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.VOID_BRUSH_RENDER_TYPE);
		}
		com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
	}

	private void renderInvertedSphere(PoseStack poseStack, MultiBufferSource bufferSource, float radius) {
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.VOID_BRUSH_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		int latSegments = 20;
		int lonSegments = 32;
		for (int lat = 0; lat < latSegments; lat++) {
			float theta1 = (lat / (float) latSegments) * (float) Math.PI;
			float theta2 = ((lat + 1) / (float) latSegments) * (float) Math.PI;
			for (int lon = 0; lon < lonSegments; lon++) {
				float phi1 = (lon / (float) lonSegments) * 2.0f * (float) Math.PI;
				float phi2 = ((lon + 1) / (float) lonSegments) * 2.0f * (float) Math.PI;
				float x1 = radius * (float) (Math.sin(theta1) * Math.cos(phi1));
				float y1 = radius * (float) (Math.cos(theta1));
				float z1 = radius * (float) (Math.sin(theta1) * Math.sin(phi1));
				float x2 = radius * (float) (Math.sin(theta1) * Math.cos(phi2));
				float y2 = radius * (float) (Math.cos(theta1));
				float z2 = radius * (float) (Math.sin(theta1) * Math.sin(phi2));
				float x3 = radius * (float) (Math.sin(theta2) * Math.cos(phi2));
				float y3 = radius * (float) (Math.cos(theta2));
				float z3 = radius * (float) (Math.sin(theta2) * Math.sin(phi2));
				float x4 = radius * (float) (Math.sin(theta2) * Math.cos(phi1));
				float y4 = radius * (float) (Math.cos(theta2));
				float z4 = radius * (float) (Math.sin(theta2) * Math.sin(phi1));
				float u1 = lon / (float) lonSegments;
				float u2 = (lon + 1) / (float) lonSegments;
				float v1 = lat / (float) latSegments;
				float v2 = (lat + 1) / (float) latSegments;
				vc.vertex(matrix, x1, y1, z1).uv(u1, v1).endVertex();
				vc.vertex(matrix, x4, y4, z4).uv(u1, v2).endVertex();
				vc.vertex(matrix, x3, y3, z3).uv(u2, v2).endVertex();
				vc.vertex(matrix, x2, y2, z2).uv(u2, v1).endVertex();
			}
		}
	}

	private void renderCircularQuad(PoseStack poseStack, MultiBufferSource bufferSource, net.minecraft.client.renderer.RenderType renderType) {
		VertexConsumer vc = bufferSource.getBuffer(renderType);
		Matrix4f m = poseStack.last().pose();
		int segments = 32;
		float angleStep = (float) (2 * Math.PI / segments);
		for (int i = 0; i < segments; i++) {
			float angle1 = i * angleStep;
			float angle2 = (i + 1) * angleStep;
			float x1 = (float) Math.cos(angle1) * 0.5f;
			float y1 = (float) Math.sin(angle1) * 0.5f;
			float x2 = (float) Math.cos(angle2) * 0.5f;
			float y2 = (float) Math.sin(angle2) * 0.5f;
			vc.vertex(m, 0, 0, 0).uv(0.5f, 0.5f).endVertex();
			vc.vertex(m, x1, y1, 0).uv(x1 + 0.5f, y1 + 0.5f).endVertex();
			vc.vertex(m, x2, y2, 0).uv(x2 + 0.5f, y2 + 0.5f).endVertex();
			vc.vertex(m, 0, 0, 0).uv(0.5f, 0.5f).endVertex();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(DomainUVEntity entity) {
		return ResourceLocation.parse("jjk_strongest:textures/entities/invis.png");
	}
}
