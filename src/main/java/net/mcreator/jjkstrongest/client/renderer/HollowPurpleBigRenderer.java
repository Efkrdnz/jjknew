package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.m;

import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;

import net.mcreator.jjkstrongest.procedures.ReturnPurpleSizeProcedure;
import net.mcreator.jjkstrongest.entity.HollowPurpleBigEntity;
import net.mcreator.jjkstrongest.client.model.Modelblank_entity;
import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class HollowPurpleBigRenderer extends MobRenderer<HollowPurpleBigEntity, Modelblank_entity<HollowPurpleBigEntity>> {
	public HollowPurpleBigRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	protected void scale(HollowPurpleBigEntity entity, PoseStack poseStack, float f) {
		Level world = entity.level();
		float scale = (float) ReturnPurpleSizeProcedure.execute(entity);
		poseStack.scale(scale, scale, scale);
	}

	@Override
	public void render(HollowPurpleBigEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		float scale = (float) ReturnPurpleSizeProcedure.execute(entity);
		renderHollowPurple(entity, partialTick, poseStack, bufferSource, scale);
	}

	// renders hollow purple with shader
	private void renderHollowPurple(HollowPurpleBigEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, float scale) {
		if (JjkShaderManager.HOLLOW_PURPLE_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		float intensity = 1.0f;
		float radius = 0.4f;
		float distortStrength = 0.3f * scale;
		if (!JjkShaderManager.beginFrameCaptureHollowPurple(timeSeconds, intensity, radius, distortStrength))
			return;
		poseStack.pushPose();
		poseStack.translate(0.0, entity.getBbHeight() * 0.5, 0.0);
		// billboard facing camera
		poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));
		// large circular quad
		float size = 10.0f * scale;
		poseStack.scale(size, size, size);
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.HOLLOW_PURPLE_RENDER_TYPE);
		Matrix4f m = poseStack.last().pose();
		// render as circle with triangle fan
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
			bs.endBatch(JjkShaderManager.HOLLOW_PURPLE_RENDER_TYPE);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(HollowPurpleBigEntity entity) {
		return new ResourceLocation("jjk_strongest:textures/entities/invis.png");
	}
}
