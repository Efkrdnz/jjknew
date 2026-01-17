package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.m;

import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import net.mcreator.jjkstrongest.entity.DismantleProjectileEntity;
import net.mcreator.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class DismantleProjectileRenderer extends EntityRenderer<DismantleProjectileEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("jjk_strongest", "textures/entities/invis.png");

	public DismantleProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(DismantleProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (JjkShaderManager.DISMANTLE_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		// prepare shader with entity parameters
		if (!JjkShaderManager.beginFrameCaptureDismantle(timeSeconds, entity.getSlashStyle(), entity.getSlashSeed(), entity.getSlashLength(), entity.getSlashWidth(), entity.getColorR(), entity.getColorG(), entity.getColorB()))
			return;
		poseStack.pushPose();
		// center on entity
		poseStack.translate(0.0, entity.getBbHeight() * 0.5, 0.0);
		// get normalized direction vector
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
		// rotate quad to face slash direction
		float yaw = (float) Mth.atan2((double) dx, (double) dz);
		float horiz = Mth.sqrt(dx * dx + dz * dz);
		float pitch = (float) (-Mth.atan2((double) dy, (double) horiz));
		poseStack.mulPose(Axis.YP.rotation(yaw));
		poseStack.mulPose(Axis.XP.rotation(pitch));
		poseStack.mulPose(Axis.ZP.rotation(entity.getSlashRoll()));
		// EXPANSION ANIMATION
		float age = entity.tickCount + partialTick;
		float maxAge = 12.0f; // entity lifetime
		float expandDuration = 3.0f; // expand over 3 ticks
		float expandProgress = Math.min(age / expandDuration, 1.0f);
		// ease-out for snappy feel
		expandProgress = 1.0f - (1.0f - expandProgress) * (1.0f - expandProgress);
		// FADE OUT in last 4 ticks
		float fadeStartAge = 8.0f;
		float fadeAlpha = 1.0f;
		if (age > fadeStartAge) {
			float fadeProgress = (age - fadeStartAge) / (maxAge - fadeStartAge);
			fadeAlpha = 1.0f - fadeProgress; // linear fade from 1.0 to 0.0
		}
		// scale quad (length expands, width stays constant)
		float L = entity.getSlashLength() * expandProgress;
		float W = entity.getSlashWidth();
		poseStack.scale(L, W, 1.0f);
		// render quad with fade alpha
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.DISMANTLE_RENDER_TYPE);
		Matrix4f m = poseStack.last().pose();
		// Apply fade alpha to all vertices
		int alpha = (int) (fadeAlpha * 255);
		int color = (alpha << 24) | 0xFFFFFF; // ARGB format
		vc.vertex(m, -0.5f, -0.5f, 0.0f).uv(0.0f, 1.0f).endVertex();
		vc.vertex(m, 0.5f, -0.5f, 0.0f).uv(1.0f, 1.0f).endVertex();
		vc.vertex(m, 0.5f, 0.5f, 0.0f).uv(1.0f, 0.0f).endVertex();
		vc.vertex(m, -0.5f, 0.5f, 0.0f).uv(0.0f, 0.0f).endVertex();
		poseStack.popPose();
		// flush buffer
		if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(JjkShaderManager.DISMANTLE_RENDER_TYPE);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(DismantleProjectileEntity entity) {
		return TEXTURE;
	}
}
