package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;
import org.joml.Matrix3f;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.Random;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class DomainUVLinesClientRenderer {
	private static final Random random = new Random();
	private static final ResourceLocation LINE_TEXTURE = ResourceLocation.parse("jjk_strongest:textures/entities/lightning_bolt.png");
	/** Half the old count. The beat is a burst, not a blizzard. */
	private static final int RAY_COUNT = 48;
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
		// NeoForge 1.21.1 passes a null PoseStack for AFTER_TRANSLUCENT_BLOCKS; the stages
		// that do supply one supply a plain identity stack, so build that
		PoseStack poseStack = event.getPoseStack();
		if (poseStack == null)
			poseStack = new PoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		Vec3 cameraPos = event.getCamera().getPosition();
		List<DomainUVEntity> domains = DomainRegistry.voidsIn(mc.level);
		if (domains.isEmpty())
			return;
		poseStack.pushPose();
		poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		for (DomainUVEntity domain : domains) {
			if (!domain.isAlive())
				continue;
			// The rays belong to the beat between the shell closing and the domain
			// turning hostile. This used to be inferred from the entity's own tick
			// count, which the client had no way to reconcile with the server.
			if (domain.getPhase() == DomainPhase.SETTLING)
				renderDomainLines(domain, poseStack, bufferSource, event.getPartialTick().getGameTimeDeltaPartialTick(false), cameraPos);
		}
		poseStack.popPose();
		bufferSource.endBatch(LINE_RENDER_TYPE);
	}

	/**
	 * The burst that writes the domain into the world, at the settling flash.
	 *
	 * <p>What this replaces was 140 rays whose colour was {@code random.nextInt(3)} between
	 * red, purple and pink — the exact opposite of a monochrome interior — seeded from
	 * {@code seed + domain.tickCount}, so the entire set reshuffled twenty times a second.
	 * That reads as television static, not as an event.
	 *
	 * <p>Now: half as many rays, seeded from the domain's UUID and nothing else, so each
	 * ray is a fixed line in space for the whole beat. What changes is their length, driven
	 * off the phase with an ease-out, so the burst throws outward and retracts. Bone-white
	 * core, cold blue glow — the same two colours as everything else in here.
	 */
	private static void renderDomainLines(DomainUVEntity domain, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos) {
		float progress = domain.getPhaseProgress();
		float alpha = 1.0f;
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
		// Seeded once per draw from the domain alone. The tick count deliberately does not
		// go in here: that is what made every ray a different ray every frame.
		random.setSeed(seed);
		// Throw out fast, ease to a stop, then draw back in over the tail of the phase.
		float eased = 1.0f - (1.0f - Math.min(1.0f, progress / 0.6f)) * (1.0f - Math.min(1.0f, progress / 0.6f));
		float retract = progress > 0.8f ? 1.0f - (progress - 0.8f) / 0.2f : 1.0f;
		float reach = Math.max(4.0f, domain.getShellRadius() * 1.2f) * eased * retract;
		if (reach <= 0.05f) {
			poseStack.popPose();
			return;
		}
		for (int i = 0; i < RAY_COUNT; i++) {
			Vec3 dir = randomUnitDirection(random);
			// A little variety in length, fixed per ray because the seed is fixed.
			float scale = 0.55f + random.nextFloat() * 0.45f;
			Vec3 start = dir.scale(1.2);
			Vec3 end = dir.scale(Math.max(1.4f, reach * scale));
			// Cold blue glow around a bone-white core: the shell's palette, not a third one.
			renderBillboardQuadColor(poseStack, buffer, start, end, OUTER_WIDTH, 0.22f, 0.30f, 0.52f, 0.10f, 0.16f, 0.34f, alpha * 0.65f, cameraPos, domain.position());
			renderBillboardQuadColor(poseStack, buffer, start, end, INNER_WIDTH, 0.92f, 0.94f, 0.98f, 0.62f, 0.72f, 0.92f, alpha, cameraPos, domain.position());
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
		PoseStack.Pose normal = poseStack.last();
		Vec3 normalVec = toCamera;
		Vec3 v1 = start.subtract(perpendicular);
		Vec3 v2 = start.add(perpendicular);
		Vec3 v3 = end.add(perpendicular);
		Vec3 v4 = end.subtract(perpendicular);
		int light = LightTexture.FULL_BRIGHT;
		buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r1, g1, b1, alpha).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z);
		buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r1, g1, b1, alpha).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z);
		buffer.addVertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z).setColor(r2, g2, b2, alpha).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z);
		buffer.addVertex(matrix, (float) v4.x, (float) v4.y, (float) v4.z).setColor(r2, g2, b2, alpha).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z);
	}
}
