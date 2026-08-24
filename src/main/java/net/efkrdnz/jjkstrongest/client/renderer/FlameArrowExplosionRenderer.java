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

import net.efkrdnz.jjkstrongest.entity.FlameArrowExplosionEntity;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class FlameArrowExplosionRenderer extends EntityRenderer<FlameArrowExplosionEntity> {
	public FlameArrowExplosionRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(FlameArrowExplosionEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		double life = entity.getPersistentData().getDouble("life");
		float totalLife = (float) life + partialTicks;
		if (totalLife < 0.1f)
			return;
		float timeSeconds = entity.tickCount / 20.0f + partialTicks / 20.0f;
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 entityPos = entity.position();
		// === PERSISTENT GROUND CORE - ALWAYS VISIBLE ===
		if (totalLife < 28.0f) { // render almost entire duration
			renderGroundCore(poseStack, bufferSource, totalLife, timeSeconds, cameraPos, entityPos);
		}
		// PHASE 1: Flash warning (0-3 ticks) - bright expanding flash
		if (totalLife < 3.0f) {
			renderFlashCore(poseStack, bufferSource, totalLife, timeSeconds, cameraPos, entityPos);
		}
		// PHASE 2: MASSIVE fire pillar (3-25 ticks) - main spectacle
		if (totalLife >= 3.0f && totalLife < 25.0f) {
			renderMassiveFirePillar(poseStack, bufferSource, totalLife, timeSeconds, cameraPos, entityPos);
		}
		// PHASE 3: Fade (25-30 ticks) - lingering fire
		if (totalLife >= 25.0f && totalLife < 30.0f) {
			renderLingeringFire(poseStack, bufferSource, totalLife, timeSeconds, cameraPos, entityPos);
		}
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
	}

	// PERSISTENT ground core - bright glow that stays throughout entire explosion
	private void renderGroundCore(PoseStack poseStack, MultiBufferSource bufferSource, float life, float time, Vec3 cameraPos, Vec3 entityPos) {
		// calculate fade based on phase
		float fade;
		if (life < 3.0f) {
			// phase 1: fade in
			fade = life / 3.0f; // 0 -> 1
		} else if (life < 25.0f) {
			// phase 2: stay bright
			fade = 1.0f;
		} else {
			// phase 3: fade out
			fade = 1.0f - ((life - 25.0f) / 5.0f); // 1 -> 0 over 5 ticks
		}
		float scale = 40.0f; // BIG CONSTANT size - matches lingering cloud!
		Vec3 toCamera = cameraPos.subtract(entityPos).normalize();
		poseStack.pushPose();
		poseStack.translate(0, 0.5f, 0); // slightly above ground
		poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(toCamera.x, toCamera.z))));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Math.asin(-toCamera.y))));
		poseStack.scale(scale, scale, scale);
		if (JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE != null && JjkShaderManager.beginFlameArrowExplosionEffect(time, fade)) {
			VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE);
			Matrix4f matrix = poseStack.last().pose();
			int light = LightTexture.FULL_BRIGHT;
			// bright yellow-white core
			vc.addVertex(matrix, -1f, -1f, 0).setColor(1f, 1f, 0.9f, 1.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, 1f, -1f, 0).setColor(1f, 1f, 0.9f, 1.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, 1f, 1f, 0).setColor(1f, 1f, 0.9f, 1.0f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, -1f, 1f, 0).setColor(1f, 1f, 0.9f, 1.0f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
		}
		poseStack.popPose();
	}

	// bright expanding warning flash
	private void renderFlashCore(PoseStack poseStack, MultiBufferSource bufferSource, float life, float time, Vec3 cameraPos, Vec3 entityPos) {
		float progress = life / 3.0f;
		float scale = 20.0f + progress * 30.0f; // 20 -> 50 blocks (BIGGER flash!)
		float fade = 1.0f - progress * 0.3f; // fade as it expands
		Vec3 toCamera = cameraPos.subtract(entityPos).normalize();
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(toCamera.x, toCamera.z))));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Math.asin(-toCamera.y))));
		poseStack.scale(scale, scale, scale);
		if (JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE != null && JjkShaderManager.beginFlameArrowExplosionEffect(time, fade)) {
			VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE);
			Matrix4f matrix = poseStack.last().pose();
			int light = LightTexture.FULL_BRIGHT;
			vc.addVertex(matrix, -1f, -1f, 0).setColor(1f, 1f, 1f, 1.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, 1f, -1f, 0).setColor(1f, 1f, 1f, 1.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, 1f, 1f, 0).setColor(1f, 1f, 1f, 1.0f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, -1f, 1f, 0).setColor(1f, 1f, 1f, 1.0f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
		}
		poseStack.popPose();
	}

	// MASSIVE fire pillar - TALL rectangular tower that tapers
	private void renderMassiveFirePillar(PoseStack poseStack, MultiBufferSource bufferSource, float life, float time, Vec3 cameraPos, Vec3 entityPos) {
		float progress = (life - 3.0f) / 22.0f; // 0-1 over 22 ticks
		float height = 120.0f + progress * 60.0f; // 120 -> 180 blocks INSANELY TALL
		float baseWidth = 20.0f; // wide base
		float topWidth = 8.0f + progress * 4.0f; // tapers to narrower top (8-12 blocks)
		// SMOOTH FADE-IN at start, FADE-OUT at end
		float fade;
		if (life < 5.0f) {
			// fade in over first 2 ticks (ticks 3-5)
			fade = (life - 3.0f) / 2.0f; // 0 -> 1
		} else if (life < 23.0f) {
			// stay full opacity
			fade = 1.0f;
		} else {
			// fade out in last 2 ticks (23-25)
			fade = (25.0f - life) / 2.0f; // 1 -> 0
		}
		Vec3 toCamera = cameraPos.subtract(entityPos).normalize();
		float yaw = (float) Math.toDegrees(Math.atan2(toCamera.x, toCamera.z));
		// render 4 faces of rectangular tower (not cylinder)
		for (int i = 0; i < 4; i++) {
			poseStack.pushPose();
			// position at center of pillar height
			poseStack.translate(0, height / 2.0f, 0);
			// rotate 90 degrees for each face
			poseStack.mulPose(Axis.YP.rotationDegrees(yaw + i * 90f));
			// use different width for bottom and top to create taper
			float bottomWidth = baseWidth;
			float topWidth_actual = topWidth;
			// scale: width affects X, height affects Y
			poseStack.scale(bottomWidth, height, 1f);
			if (JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE != null && JjkShaderManager.beginFlameArrowExplosionEffect(time, fade)) {
				VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE);
				Matrix4f matrix = poseStack.last().pose();
				int light = LightTexture.FULL_BRIGHT;
				// create TAPERED rectangle - narrow at top, wide at bottom
				float taper = topWidth_actual / bottomWidth; // taper ratio
				// bottom vertices - full width
				float bottomLeft = -0.5f;
				float bottomRight = 0.5f;
				// top vertices - tapered (narrower)
				float topLeft = -0.5f * taper;
				float topRight = 0.5f * taper;
				// bright yellow-white at bottom, orange-red at top
				vc.addVertex(matrix, bottomLeft, -0.5f, 0).setColor(1f, 1f, 0.95f, 1.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
				vc.addVertex(matrix, bottomRight, -0.5f, 0).setColor(1f, 1f, 0.95f, 1.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
				vc.addVertex(matrix, topRight, 0.5f, 0).setColor(1f, 0.35f, 0.08f, 0.6f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
				vc.addVertex(matrix, topLeft, 0.5f, 0).setColor(1f, 0.35f, 0.08f, 0.6f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			}
			poseStack.popPose();
		}
	}

	// lingering fire cloud with smooth fade
	private void renderLingeringFire(PoseStack poseStack, MultiBufferSource bufferSource, float life, float time, Vec3 cameraPos, Vec3 entityPos) {
		float progress = (life - 25.0f) / 5.0f; // 0-1 over 5 ticks
		float scale = 40.0f; // CONSTANT 40 blocks - matches core!
		float fade = 1.0f - progress; // smooth fade 1.0 -> 0
		Vec3 toCamera = cameraPos.subtract(entityPos).normalize();
		poseStack.pushPose();
		poseStack.translate(0, 10.0f, 0); // higher up
		poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(toCamera.x, toCamera.z))));
		poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Math.asin(-toCamera.y))));
		poseStack.scale(scale, scale, scale);
		if (JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE != null && JjkShaderManager.beginFlameArrowExplosionEffect(time, fade)) {
			VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.FLAME_ARROW_EXPLOSION_RENDER_TYPE);
			Matrix4f matrix = poseStack.last().pose();
			int light = LightTexture.FULL_BRIGHT;
			vc.addVertex(matrix, -1f, -1f, 0).setColor(1f, 0.3f, 0.05f, 1.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, 1f, -1f, 0).setColor(1f, 0.3f, 0.05f, 1.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, 1f, 1f, 0).setColor(1f, 0.3f, 0.05f, 1.0f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
			vc.addVertex(matrix, -1f, 1f, 0).setColor(1f, 0.3f, 0.05f, 1.0f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
		}
		poseStack.popPose();
	}

	@Override
	public boolean shouldRender(FlameArrowExplosionEntity entity, Frustum frustum, double x, double y, double z) {
		return true; // always render regardless of distance
	}

	@Override
	public ResourceLocation getTextureLocation(FlameArrowExplosionEntity entity) {
		return ResourceLocation.parse("jjk_strongest:textures/entities/invis.png");
	}
}
