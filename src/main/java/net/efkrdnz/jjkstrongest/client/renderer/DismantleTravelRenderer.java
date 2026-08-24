package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;


import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import net.efkrdnz.jjkstrongest.entity.DismantleTravelEntity;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class DismantleTravelRenderer extends EntityRenderer<DismantleTravelEntity> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("jjk_strongest", "textures/entities/invis.png");

	public DismantleTravelRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(DismantleTravelEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (JjkShaderManager.DISMANTLE_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		if (!JjkShaderManager.beginFrameCaptureDismantle(timeSeconds, entity.getSlashStyle(), entity.getSlashSeed(), entity.getSlashLength(), entity.getSlashWidth(), entity.getColorR(), entity.getColorG(), entity.getColorB()))
			return;
		poseStack.pushPose();
		poseStack.translate(0.0, entity.getBbHeight() * 0.5, 0.0);
		float dx = entity.getDirX();
		float dy = entity.getDirY();
		float dz = entity.getDirZ();
		float dlen = Mth.sqrt(dx * dx + dy * dy + dz * dz);
		if (dlen < 1.0e-6f) {
			dx = 0;
			dy = 0;
			dz = 1;
			dlen = 1;
		}
		dx /= dlen;
		dy /= dlen;
		dz /= dlen;
		float yaw = (float) Mth.atan2((double) dx, (double) dz);
		float horiz = Mth.sqrt(dx * dx + dz * dz);
		float pitch = (float) (-Mth.atan2((double) dy, (double) horiz));
		poseStack.mulPose(Axis.YP.rotation(yaw));
		poseStack.mulPose(Axis.XP.rotation(pitch));
		poseStack.mulPose(Axis.ZP.rotation(entity.getSlashRoll()));
		float L = entity.getSlashLength();
		float W = entity.getSlashWidth();
		poseStack.scale(L, W, 1.0f);
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.DISMANTLE_RENDER_TYPE);
		Matrix4f m = poseStack.last().pose();
		vc.vertex(m, -0.5f, -0.5f, 0.0f).uv(0.0f, 1.0f).endVertex();
		vc.vertex(m, 0.5f, -0.5f, 0.0f).uv(1.0f, 1.0f).endVertex();
		vc.vertex(m, 0.5f, 0.5f, 0.0f).uv(1.0f, 0.0f).endVertex();
		vc.vertex(m, -0.5f, 0.5f, 0.0f).uv(0.0f, 0.0f).endVertex();
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.DISMANTLE_RENDER_TYPE);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(DismantleTravelEntity entity) {
		return TEXTURE;
	}
}
