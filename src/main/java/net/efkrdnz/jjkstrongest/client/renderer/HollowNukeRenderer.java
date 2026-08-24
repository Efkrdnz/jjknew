package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;

import net.efkrdnz.jjkstrongest.entity.HollowNukeEntity;
import net.efkrdnz.jjkstrongest.client.model.Modelblank_entity;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class HollowNukeRenderer extends MobRenderer<HollowNukeEntity, Modelblank_entity<HollowNukeEntity>> {
	public HollowNukeRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public void render(HollowNukeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		renderHollowNuke(entity, partialTick, poseStack, bufferSource);
	}

	@Override
	public boolean shouldRender(HollowNukeEntity entity, Frustum frustum, double x, double y, double z) {
		return true;
	}

	private void renderHollowNuke(HollowNukeEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.HOLLOW_NUKE_RENDER_TYPE == null)
			return;
		float life = (float) entity.getPersistentData().getDouble("liife");
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		float seed = (entity.getId() * 0.17321f) % 1000.0f;
		float fade = (float) entity.getPersistentData().getDouble("fade");
		if (fade <= 0.001f)
			return;
		// pass fade by multiplying "life" and seed usage stays same; shader will compute fade from life too
		if (!JjkShaderManager.beginHollowNukeEffect(timeSeconds, life, seed))
			return;
		float rad = (float) entity.getPersistentData().getDouble("rad");
		if (rad <= 0.01f)
			rad = 12.0f;
		poseStack.pushPose();
		poseStack.translate(0.0, entity.getBbHeight() * 0.5, 0.0);
		poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));
		// slight fade shrink makes ending feel like energy evaporating
		float size = rad * 1.35f * (0.85f + 0.15f * fade);
		poseStack.scale(size, size, size);
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.HOLLOW_NUKE_RENDER_TYPE);
		Matrix4f m = poseStack.last().pose();
		int segments = 96;
		float step = (float) (2.0 * Math.PI / segments);
		for (int i = 0; i < segments; i++) {
			float a1 = i * step;
			float a2 = (i + 1) * step;
			float x1 = (float) Math.cos(a1) * 0.5f;
			float y1 = (float) Math.sin(a1) * 0.5f;
			float x2 = (float) Math.cos(a2) * 0.5f;
			float y2 = (float) Math.sin(a2) * 0.5f;
			vc.addVertex(m, 0, 0, 0).setUv(0.5f, 0.5f);
			vc.addVertex(m, x1, y1, 0).setUv(x1 + 0.5f, y1 + 0.5f);
			vc.addVertex(m, x2, y2, 0).setUv(x2 + 0.5f, y2 + 0.5f);
			vc.addVertex(m, 0, 0, 0).setUv(0.5f, 0.5f);
		}
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.HOLLOW_NUKE_RENDER_TYPE);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(HollowNukeEntity entity) {
		return ResourceLocation.parse("jjk_strongest:textures/entities/invis.png");
	}
}
