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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.entity.DomainUVEntity;
import net.mcreator.jjkstrongest.client.renderer.DomainUVLinesClientRenderer;

import java.util.Random;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class DomainUVLinesClientRenderer {
	private static final Random random = new Random();
	private static final ResourceLocation LINE_TEXTURE = new ResourceLocation("jjk_strongest:textures/entities/lightning_bolt.png");
	private static final float OUTER_WIDTH = 0.045f;
	private static final float INNER_WIDTH = 0.018f;
	private static final RenderType LINE_RENDER_TYPE = RenderType.create("domain_uv_lines", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader)).setTextureState(new RenderStateShard.TextureStateShard(LINE_TEXTURE, false, false))
					.setTransparencyState(new RenderStateShard.TransparencyStateShard("domain_uv_lines_transparency", () -> {
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
		double renderDistance = 35.0;
		AABB searchBox = new AABB(cameraPos.x - renderDistance, cameraPos.y - renderDistance, cameraPos.z - renderDistance, cameraPos.x + renderDistance, cameraPos.y + renderDistance, cameraPos.z + renderDistance);
		List<DomainUVEntity> domains = mc.level.getEntitiesOfClass(DomainUVEntity.class, searchBox);
		if (domains.isEmpty())
			return;
		poseStack.pushPose();
		poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		for (DomainUVEntity domain : domains) {
			if (!domain.isAlive())
				continue;
			int age = domain.tickCount;
			// lines window: 40 ticks right after barrier is done
			if (age >= 40 && age < 80) {
				renderDomainLines(domain, poseStack, bufferSource, event.getPartialTick(), cameraPos);
			}
		}
		poseStack.popPose();
		bufferSource.endBatch(LINE_RENDER_TYPE);
	}

	private static void renderDomainLines(DomainUVEntity domain, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos) {
		// renders rays for the domain post phase
		float progress = (domain.tickCount - 40 + partialTick) / 40.0f;
		float alpha = 1.0f;
		// small fade in/out
		if (progress < 0.15f)
			alpha *= (progress / 0.15f);
		if (progress > 0.85f)
			alpha *= (1.0f - (progress - 0.85f) / 0.15f);
		if (alpha <= 0.01f)
			return;
		VertexConsumer buffer = bufferSource.getBuffer(LINE_RENDER_TYPE);
		poseStack.pushPose();
		poseStack.translate(domain.getX(), domain.getY(), domain.getZ());
		long seed = domain.getUUID().getMostSignificantBits() ^ domain.getUUID().getLeastSignificantBits();
		random.setSeed(seed + domain.tickCount);
		int rays = 140;
		float length = 36.0f;
		for (int i = 0; i < rays; i++) {
			Vec3 dir = randomUnitDirection(random);
			Vec3 start = dir.scale(1.2);
			Vec3 end = dir.scale(length);
			int mode = random.nextInt(3);
			float r1, g1, b1;
			float r2, g2, b2;
			if (mode == 0) { // red
				r1 = 1.0f;
				g1 = 0.05f;
				b1 = 0.05f;
				r2 = 1.0f;
				g2 = 0.0f;
				b2 = 0.0f;
			} else if (mode == 1) { // purple
				r1 = 0.75f;
				g1 = 0.0f;
				b1 = 1.0f;
				r2 = 0.55f;
				g2 = 0.0f;
				b2 = 0.95f;
			} else { // pink
				r1 = 1.0f;
				g1 = 0.2f;
				b1 = 0.9f;
				r2 = 0.9f;
				g2 = 0.05f;
				b2 = 0.75f;
			}
			// outer glow
			renderBillboardQuadColor(poseStack, buffer, start, end, OUTER_WIDTH, r1 * 0.2f, g1 * 0.2f, b1 * 0.2f, r2 * 0.2f, g2 * 0.2f, b2 * 0.2f, alpha * 0.75f, cameraPos, domain.position());
			// inner core
			renderBillboardQuadColor(poseStack, buffer, start, end, INNER_WIDTH, r1, g1, b1, r2, g2, b2, alpha, cameraPos, domain.position());
		}
		poseStack.popPose();
	}

	private static Vec3 randomUnitDirection(Random r) {
		double x = r.nextDouble() * 2.0 - 1.0;
		double y = r.nextDouble() * 2.0 - 1.0;
		double z = r.nextDouble() * 2.0 - 1.0;
		Vec3 v = new Vec3(x, y, z);
		double len = v.length();
		if (len < 0.0001)
			return new Vec3(0, 1, 0);
		return v.scale(1.0 / len);
	}

	private static void renderBillboardQuadColor(PoseStack poseStack, VertexConsumer buffer, Vec3 start, Vec3 end, float width, float r1, float g1, float b1, float r2, float g2, float b2, float alpha, Vec3 cameraPos, Vec3 entityPos) {
		Vec3 worldStart = entityPos.add(start);
		Vec3 worldEnd = entityPos.add(end);
		Vec3 toCamera = cameraPos.subtract(worldStart.add(worldEnd).scale(0.5)).normalize();
		Vec3 lineDir = end.subtract(start).normalize();
		Vec3 perpendicular = lineDir.cross(toCamera).normalize().scale(width);
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
}
