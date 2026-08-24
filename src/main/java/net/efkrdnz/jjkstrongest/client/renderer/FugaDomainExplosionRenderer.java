package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;


import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.entity.FugaDomainExplosionEntity;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class FugaDomainExplosionRenderer extends EntityRenderer<FugaDomainExplosionEntity> {
	private static final float radius = 100f;
	private static final float durationTicks = 30f;

	public FugaDomainExplosionRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	private static float clamp01(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}

	private static float smoothstep(float a, float b, float x) {
		float t = clamp01((x - a) / (b - a));
		return t * t * (3f - 2f * t);
	}

	@Override
	public void render(FugaDomainExplosionEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		float life = entity.tickCount + partialTicks;
		float p = clamp01(life / durationTicks);
		float fade = 1f - smoothstep(0.78f, 1.0f, p);
		float timeSeconds = (entity.tickCount + partialTicks) / 20f;
		if (fade <= 0.01f)
			return;
		if (JjkShaderManager.FUGA_DOMAIN_EXPLOSION_RENDER_TYPE == null)
			return;
		if (!JjkShaderManager.beginFugaDomainExplosionEffect(timeSeconds, fade, p))
			return;
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.FUGA_DOMAIN_EXPLOSION_RENDER_TYPE);
		int light = LightTexture.FULL_BRIGHT;
		// ground disc
		poseStack.pushPose();
		poseStack.translate(0, 0.05f, 0);
		poseStack.mulPose(Axis.XP.rotationDegrees(90f));
		poseStack.scale(radius, radius, radius);
		drawQuad(poseStack, vc, light);
		poseStack.popPose();
		// vertical billboard so it looks big from the side too
		Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 pos = entity.position();
		Vec3 toCam = cam.subtract(pos).normalize();
		float yaw = (float) Math.toDegrees(Math.atan2(toCam.x, toCam.z));
		float pitch = (float) Math.toDegrees(Math.asin(-toCam.y));
		poseStack.pushPose();
		poseStack.translate(0, 18f, 0);
		poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
		poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
		poseStack.scale(radius, radius, radius);
		drawQuad(poseStack, vc, light);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
	}

	private static void drawQuad(PoseStack poseStack, VertexConsumer vc, int light) {
		Matrix4f m = poseStack.last().pose();
		vc.vertex(m, -1f, -1f, 0).color(1f, 1f, 1f, 1f).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
		vc.vertex(m, 1f, -1f, 0).color(1f, 1f, 1f, 1f).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
		vc.vertex(m, 1f, 1f, 0).color(1f, 1f, 1f, 1f).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
		vc.vertex(m, -1f, 1f, 0).color(1f, 1f, 1f, 1f).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
	}

	@Override
	public boolean shouldRender(FugaDomainExplosionEntity entity, Frustum frustum, double x, double y, double z) {
		return true;
	}

	@Override
	public ResourceLocation getTextureLocation(FugaDomainExplosionEntity entity) {
		return ResourceLocation.parse("jjk_strongest:textures/entities/invis.png");
	}
}
