package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;
import org.joml.Matrix3f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.entity.BFEntityEntity;
import net.mcreator.jjkstrongest.client.renderer.BlackFlashLightningClientRenderer;

import java.util.Random;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class BlackFlashLightningClientRenderer {
	private static final Random random = new Random();
	private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("jjk_strongest:textures/entities/lightning_bolt.png");
	// ADJUSTABLE SETTINGS
	private static final float OUTER_BLACK_WIDTH = 0.12f;
	private static final float INNER_RED_WIDTH = 0.06f;
	private static final float BRANCH_BLACK_WIDTH = 0.08f;
	private static final float BRANCH_RED_WIDTH = 0.04f;
	// Custom render type for lightning with additive blending
	private static final RenderType LIGHTNING_RENDER_TYPE = RenderType.create("black_flash_lightning", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> net.minecraft.client.renderer.GameRenderer.getRendertypeEntityTranslucentShader()))
					.setTextureState(new RenderStateShard.TextureStateShard(LIGHTNING_TEXTURE, false, false)).setTransparencyState(new RenderStateShard.TransparencyStateShard("lightning_transparency", () -> {
						com.mojang.blaze3d.systems.RenderSystem.enableBlend();
						com.mojang.blaze3d.systems.RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
					}, () -> {
						com.mojang.blaze3d.systems.RenderSystem.disableBlend();
						com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
					})).setLightmapState(new RenderStateShard.LightmapStateShard(true)).setOverlayState(new RenderStateShard.OverlayStateShard(true)).setCullState(new RenderStateShard.CullStateShard(false))
					.setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false)).createCompositeState(false));

	@SubscribeEvent
	public static void onRenderWorld(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		Vec3 cameraPos = event.getCamera().getPosition();
		double renderDistance = 64.0;
		AABB searchBox = new AABB(cameraPos.x - renderDistance, cameraPos.y - renderDistance, cameraPos.z - renderDistance, cameraPos.x + renderDistance, cameraPos.y + renderDistance, cameraPos.z + renderDistance);
		List<BFEntityEntity> lightningEntities = mc.level.getEntitiesOfClass(BFEntityEntity.class, searchBox);
		if (lightningEntities.isEmpty())
			return;
		poseStack.pushPose();
		poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		for (BFEntityEntity entity : lightningEntities) {
			if (entity.isAlive()) {
				renderLightningEntity(entity, poseStack, bufferSource, event.getPartialTick(), cameraPos);
			}
		}
		poseStack.popPose();
		bufferSource.endBatch(LIGHTNING_RENDER_TYPE);
	}

	private static void renderLightningEntity(BFEntityEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos) {
		int age = entity.tickCount;
		int wildPhase = 12; // wild phase: 0-12 ticks
		int maxAge = 36; // total duration: 36 ticks
		float ageProgress = (age + partialTick) / maxAge;
		// fade out alpha over ENTIRE duration
		float alpha = 1.0f - ageProgress;
		if (alpha <= 0.0f)
			return;
		// calculate intensity reduction after wild phase
		float intensity = 1.0f;
		if (age > wildPhase) {
			// reduce intensity after wild phase (smooth transition)
			float calmProgress = ((age + partialTick) - wildPhase) / (maxAge - wildPhase);
			intensity = 1.0f - calmProgress * 0.7f; // reduce to 30% intensity
		}
		poseStack.pushPose();
		poseStack.translate(entity.getX(), entity.getY(), entity.getZ());
		VertexConsumer buffer = bufferSource.getBuffer(LIGHTNING_RENDER_TYPE);
		long entitySeed = entity.getUUID().getMostSignificantBits();
		random.setSeed(entitySeed + age);
		// arc count reduces after wild phase
		int baseArcCount = 8 + random.nextInt(5); // 8-12 arcs
		int arcCount = age <= wildPhase ? baseArcCount : Math.max(3, (int) (baseArcCount * intensity)); // reduce to 3-4 arcs
		for (int arc = 0; arc < arcCount; arc++) {
			renderLightningArcQuad(poseStack, buffer, alpha, arc, arcCount, intensity, cameraPos, entity.position());
		}
		poseStack.popPose();
	}

	// render lightning arc - now with intensity parameter
	private static void renderLightningArcQuad(PoseStack poseStack, VertexConsumer buffer, float alpha, int arcIndex, int totalArcs, float intensity, Vec3 cameraPos, Vec3 entityPos) {
		float angleH = (float) arcIndex / totalArcs * (float) Math.PI * 2 + random.nextFloat() * 0.5f;
		float angleV = (random.nextFloat() - 0.5f) * (float) Math.PI * 0.8f;
		// arc length reduces with intensity
		float baseArcLength = 3.0f + random.nextFloat() * 3.0f;
		float arcLength = baseArcLength * intensity;
		Vec3 end = new Vec3(Math.cos(angleH) * Math.cos(angleV) * arcLength, Math.sin(angleV) * arcLength, Math.sin(angleH) * Math.cos(angleV) * arcLength);
		int segments = 12 + random.nextInt(8);
		// deviation reduces with intensity (less jagged when calm)
		float maxDeviation = 0.6f * intensity;
		Vec3 start = Vec3.ZERO;
		Vec3 dir = end.subtract(start).normalize();
		Vec3 perp1 = dir.cross(new Vec3(0, 1, 0)).normalize();
		if (perp1.length() < 0.1) {
			perp1 = dir.cross(new Vec3(1, 0, 0)).normalize();
		}
		Vec3 perp2 = dir.cross(perp1).normalize();
		Vec3[] pathPoints = new Vec3[segments + 1];
		for (int i = 0; i <= segments; i++) {
			float progress = (float) i / segments;
			float devStrength = (float) Math.sin(progress * Math.PI) * maxDeviation;
			float rand1 = (random.nextFloat() - 0.5f) * 2 * devStrength;
			float rand2 = (random.nextFloat() - 0.5f) * 2 * devStrength;
			Vec3 offset = perp1.scale(rand1).add(perp2.scale(rand2));
			pathPoints[i] = start.add(end.subtract(start).scale(progress)).add(offset);
		}
		pathPoints[0] = start;
		pathPoints[segments] = end;
		// LAYER 1: BLACK OUTER LAYER
		for (int i = 0; i < segments; i++) {
			Vec3 p1 = pathPoints[i];
			Vec3 p2 = pathPoints[i + 1];
			float progress1 = (float) i / segments;
			float progress2 = (float) (i + 1) / segments;
			float brightness1 = 1.0f - progress1 * 0.2f;
			float brightness2 = 1.0f - progress2 * 0.2f;
			renderBillboardQuadColor(poseStack, buffer, p1, p2, OUTER_BLACK_WIDTH * intensity, 0.05f * brightness1, 0.05f * brightness1, 0.05f * brightness1, 0.05f * brightness2, 0.05f * brightness2, 0.05f * brightness2, alpha * 0.9f, cameraPos,
					entityPos);
		}
		// LAYER 2: RED CORE
		for (int i = 0; i < segments; i++) {
			Vec3 p1 = pathPoints[i];
			Vec3 p2 = pathPoints[i + 1];
			float progress1 = (float) i / segments;
			float progress2 = (float) (i + 1) / segments;
			float brightness1 = 1.0f - progress1 * 0.1f;
			float brightness2 = 1.0f - progress2 * 0.1f;
			renderBillboardQuadColor(poseStack, buffer, p1, p2, INNER_RED_WIDTH * intensity, 1.0f * brightness1, 0.1f * brightness1, 0.1f * brightness1, 1.0f * brightness2, 0.1f * brightness2, 0.1f * brightness2, alpha, cameraPos, entityPos);
		}
		// branches only during wild phase (intensity > 0.7)
		if (intensity > 0.7f && random.nextFloat() < 0.1f && segments > 8) {
			int branchPoint = 4 + random.nextInt(segments - 8);
			renderBranchQuad(poseStack, buffer, pathPoints[branchPoint], alpha, intensity, cameraPos, entityPos);
		}
	}

	private static void renderBillboardQuadColor(PoseStack poseStack, VertexConsumer buffer, Vec3 start, Vec3 end, float width, float r1, float g1, float b1, float r2, float g2, float b2, float alpha, Vec3 cameraPos, Vec3 entityPos) {
		Vec3 worldStart = entityPos.add(start);
		Vec3 worldEnd = entityPos.add(end);
		Vec3 toCamera = cameraPos.subtract(worldStart.add(worldEnd).scale(0.5)).normalize();
		Vec3 lightningDir = end.subtract(start).normalize();
		Vec3 perpendicular = lightningDir.cross(toCamera).normalize().scale(width);
		Matrix4f matrix = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		Vec3 normalVec = toCamera;
		Vec3 v1 = start.subtract(perpendicular);
		Vec3 v2 = start.add(perpendicular);
		Vec3 v3 = end.add(perpendicular);
		Vec3 v4 = end.subtract(perpendicular);
		int light = LightTexture.FULL_BRIGHT;
		buffer.vertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).color(r1, g1, b1, alpha).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
		buffer.vertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).color(r1, g1, b1, alpha).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
		buffer.vertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z).color(r2, g2, b2, alpha).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
		buffer.vertex(matrix, (float) v4.x, (float) v4.y, (float) v4.z).color(r2, g2, b2, alpha).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
	}

	private static void renderBranchQuad(PoseStack poseStack, VertexConsumer buffer, Vec3 start, float alpha, float intensity, Vec3 cameraPos, Vec3 entityPos) {
		float angleH = random.nextFloat() * (float) Math.PI * 2;
		float angleV = (random.nextFloat() - 0.5f) * (float) Math.PI * 0.5f;
		float branchLength = (1.0f + random.nextFloat() * 1.5f) * intensity;
		Vec3 end = start.add(new Vec3(Math.cos(angleH) * Math.cos(angleV) * branchLength, Math.sin(angleV) * branchLength, Math.sin(angleH) * Math.cos(angleV) * branchLength));
		renderBillboardQuadColor(poseStack, buffer, start, end, BRANCH_BLACK_WIDTH * intensity, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, alpha * 0.9f, cameraPos, entityPos);
		renderBillboardQuadColor(poseStack, buffer, start, end, BRANCH_RED_WIDTH * intensity, 1.0f, 0.1f, 0.1f, 0.8f, 0.08f, 0.08f, alpha, cameraPos, entityPos);
	}
}
