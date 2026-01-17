package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;
import org.joml.Matrix3f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.renderer.PurpleChargeLightningRenderer;

import java.util.Random;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class PurpleChargeLightningRenderer {
	private static final Random random = new Random();
	private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("jjk_strongest:textures/entities/lightning_bolt.png");
	// ADJUSTABLE SETTINGS - MUCH THINNER
	private static final float LIGHTNING_WIDTH = 0.025f; // Very thin
	// Custom render type for purple lightning
	private static final RenderType PURPLE_LIGHTNING_RENDER_TYPE = RenderType.create("purple_charge_lightning", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
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
		// find all players near camera with purple charge flag
		double renderDistance = 64.0;
		AABB searchBox = new AABB(cameraPos.x - renderDistance, cameraPos.y - renderDistance, cameraPos.z - renderDistance, cameraPos.x + renderDistance, cameraPos.y + renderDistance, cameraPos.z + renderDistance);
		List<Player> players = mc.level.getEntitiesOfClass(Player.class, searchBox);
		poseStack.pushPose();
		poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		for (Player player : players) {
			int chargeTicks = player.getPersistentData().getInt("purple_charge_lightning");
			if (chargeTicks > 0) {
				renderPurpleLightning(player, poseStack, bufferSource, event.getPartialTick(), cameraPos, chargeTicks);
				// decrement counter (client-side visual only, server handles actual countdown)
				if (mc.level.getGameTime() % 20 == 0) {
					player.getPersistentData().putInt("purple_charge_lightning", Math.max(0, chargeTicks - 1));
				}
			}
		}
		poseStack.popPose();
		bufferSource.endBatch(PURPLE_LIGHTNING_RENDER_TYPE);
	}

	// render purple lightning at player
	private static void renderPurpleLightning(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos, int ticksRemaining) {
		int maxTicks = 10;
		int age = maxTicks - ticksRemaining;
		// position at player's chest level
		Vec3 playerPos = player.position().add(0, player.getBbHeight() * 0.6, 0);
		poseStack.pushPose();
		poseStack.translate(playerPos.x, playerPos.y, playerPos.z);
		VertexConsumer buffer = bufferSource.getBuffer(PURPLE_LIGHTNING_RENDER_TYPE);
		// USE GAME TIME + PLAYER UUID for truly random pattern each burst
		long gameTime = Minecraft.getInstance().level.getGameTime();
		long seed = player.getUUID().getMostSignificantBits() + gameTime + age;
		random.setSeed(seed);
		// 5-7 converging arcs
		int arcCount = 5 + random.nextInt(3);
		for (int arc = 0; arc < arcCount; arc++) {
			renderConvergingArc(poseStack, buffer, arc, arcCount, cameraPos, playerPos);
		}
		poseStack.popPose();
	}

	// render single arc converging toward player
	private static void renderConvergingArc(PoseStack poseStack, VertexConsumer buffer, int arcIndex, int totalArcs, Vec3 cameraPos, Vec3 playerPos) {
		// random starting direction (arcs start far, end at center)
		float angleH = (float) arcIndex / totalArcs * (float) Math.PI * 2 + random.nextFloat() * 0.5f;
		float angleV = (random.nextFloat() - 0.5f) * (float) Math.PI * 0.6f;
		// arc length (2-4 blocks)
		float arcLength = 2.0f + random.nextFloat() * 2.0f;
		// start point (far from player)
		Vec3 start = new Vec3(Math.cos(angleH) * Math.cos(angleV) * arcLength, Math.sin(angleV) * arcLength, Math.sin(angleH) * Math.cos(angleV) * arcLength);
		// end point (at player center)
		Vec3 end = Vec3.ZERO;
		// draw jagged converging path
		int segments = 8 + random.nextInt(6);
		float maxDeviation = 0.4f;
		Vec3 dir = end.subtract(start).normalize();
		// perpendicular vectors for zigzag
		Vec3 perp1 = dir.cross(new Vec3(0, 1, 0)).normalize();
		if (perp1.length() < 0.1) {
			perp1 = dir.cross(new Vec3(1, 0, 0)).normalize();
		}
		Vec3 perp2 = dir.cross(perp1).normalize();
		// generate path points
		Vec3[] pathPoints = new Vec3[segments + 1];
		for (int i = 0; i <= segments; i++) {
			float progress = (float) i / segments;
			float devStrength = (float) Math.sin(progress * Math.PI) * maxDeviation;
			float rand1 = (random.nextFloat() - 0.5f) * 2 * devStrength;
			float rand2 = (random.nextFloat() - 0.5f) * 2 * devStrength;
			Vec3 offset = perp1.scale(rand1).add(perp2.scale(rand2));
			pathPoints[i] = start.add(end.subtract(start).scale(progress)).add(offset);
		}
		// endpoints should not deviate
		pathPoints[0] = start;
		pathPoints[segments] = end;
		// SINGLE LAYER: BRIGHT PURPLE - NO FADE, FULLBRIGHT
		for (int i = 0; i < segments; i++) {
			Vec3 p1 = pathPoints[i];
			Vec3 p2 = pathPoints[i + 1];
			float progress1 = (float) i / segments;
			float progress2 = (float) (i + 1) / segments;
			// brightness increases as it gets closer to player (still vibrant)
			float brightness1 = 0.8f + progress1 * 0.2f; // 0.8 to 1.0
			float brightness2 = 0.8f + progress2 * 0.2f;
			// BRIGHT PURPLE - OPAQUE (alpha = 1.0)
			renderBillboardQuad(poseStack, buffer, p1, p2, LIGHTNING_WIDTH, 0.7f * brightness1, 0.0f, 1.0f * brightness1, // brighter purple
					0.7f * brightness2, 0.0f, 1.0f * brightness2, 1.0f, cameraPos, playerPos); // OPAQUE ALPHA
		}
	}

	// render billboard quad
	private static void renderBillboardQuad(PoseStack poseStack, VertexConsumer buffer, Vec3 start, Vec3 end, float width, float r1, float g1, float b1, float r2, float g2, float b2, float alpha, Vec3 cameraPos, Vec3 playerPos) {
		Vec3 worldStart = playerPos.add(start);
		Vec3 worldEnd = playerPos.add(end);
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
		int light = LightTexture.FULL_BRIGHT; // ALWAYS FULLBRIGHT
		buffer.vertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).color(r1, g1, b1, alpha).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
		buffer.vertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).color(r1, g1, b1, alpha).uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
		buffer.vertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z).color(r2, g2, b2, alpha).uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
		buffer.vertex(matrix, (float) v4.x, (float) v4.y, (float) v4.z).color(r2, g2, b2, alpha).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
				.endVertex();
	}
}
