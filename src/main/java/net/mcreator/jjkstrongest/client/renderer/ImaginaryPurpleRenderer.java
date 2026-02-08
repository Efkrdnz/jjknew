package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.m;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;

import net.mcreator.jjkstrongest.entity.ImaginaryPurpleEntity;
import net.mcreator.jjkstrongest.client.model.Modelblank_entity;
import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class ImaginaryPurpleRenderer extends MobRenderer<ImaginaryPurpleEntity, Modelblank_entity<ImaginaryPurpleEntity>> {
	public ImaginaryPurpleRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	protected void scale(ImaginaryPurpleEntity entity, PoseStack poseStack, float f) {
		float scale = 1.0f;
		poseStack.scale(scale, scale, scale);
	}

	@Override
	public boolean shouldRender(ImaginaryPurpleEntity entity, Frustum frustum, double x, double y, double z) {
		// always render regardless of distance/frustum
		return true;
	}

	@Override
	public void render(ImaginaryPurpleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		renderImaginaryPurple(entity, partialTick, poseStack, bufferSource);
	}

	// render imaginary purple spark with shader
	private void renderImaginaryPurple(ImaginaryPurpleEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.IMAGINARY_PURPLE_PROJECTILE_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		float intensity = 1.0f;
		if (!JjkShaderManager.beginImaginaryPurpleProjectileEffect(timeSeconds, intensity))
			return;
		poseStack.pushPose();
		poseStack.translate(0.0, entity.getBbHeight() * 0.5, 0.0);
		// billboard facing camera
		poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));
		// smaller size than hollow purple - compact spark
		float size = 3.0f;
		poseStack.scale(size, size, size);
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.IMAGINARY_PURPLE_PROJECTILE_RENDER_TYPE);
		Matrix4f m = poseStack.last().pose();
		// render as circle
		int segments = 64;
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
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.IMAGINARY_PURPLE_PROJECTILE_RENDER_TYPE);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(ImaginaryPurpleEntity entity) {
		return new ResourceLocation("jjk_strongest:textures/entities/invis.png");
	}
}
