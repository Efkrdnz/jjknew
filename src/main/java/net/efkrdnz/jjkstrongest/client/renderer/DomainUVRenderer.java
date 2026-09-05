package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.client.JjkShaderManager;
import net.efkrdnz.jjkstrongest.client.model.Modelblank_entity;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

/**
 * Draws the domain interior.
 *
 * <p>Three things changed here, and they are the reason the sphere now reads as a
 * place rather than an effect:
 *
 * <ul>
 * <li>The radius comes from the entity's synced shape, so what you see is exactly
 *     what you collide with. It used to be a hard-coded 25.2 against a 30-block
 *     barrier and a 28.5-block dome.</li>
 * <li>Depth testing is left on. Every draw here used to be wrapped in
 *     {@code disableDepthTest()} + {@code depthMask(false)}, so the interior painted
 *     over everything and entity visibility inside came down to render order.</li>
 * <li>The mesh is built once. It used to be rebuilt every frame, per domain —
 *     2560 sine and cosine calls a frame for a shape that never changes.</li>
 * </ul>
 */
public class DomainUVRenderer extends MobRenderer<DomainUVEntity, Modelblank_entity<DomainUVEntity>> {

	private static final int LAT_SEGMENTS = 24;
	private static final int LON_SEGMENTS = 48;
	/** Unit sphere, wound inward, as (x, y, z, u, v) per vertex. */
	private static final float[] UNIT_SPHERE = buildUnitSphere();

	public DomainUVRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	public boolean shouldRender(DomainUVEntity entity, Frustum frustum, double x, double y, double z) {
		// The anchor is a 0.1-block entity carrying a 30-block sphere, so the usual
		// frustum test would cull it while the shell still fills the screen.
		return true;
	}

	@Override
	public void render(DomainUVEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

		float radius = entity.getShellRadius();
		if (radius <= 0.01f)
			return;

		renderShell(entity, radius, partialTick, poseStack, bufferSource);
		// The inner flourishes only belong to a domain that has finished opening.
		if (entity.getPhase() == DomainPhase.ACTIVE || entity.getPhase() == DomainPhase.SETTLING) {
			renderRift(entity, radius, partialTick, poseStack, bufferSource);
			renderBlackHole(entity, radius, partialTick, poseStack, bufferSource);
		}
	}

	private void renderShell(DomainUVEntity entity, float radius, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.VOID_BRUSH_RENDER_TYPE == null)
			return;

		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		Vec3 camera = this.entityRenderDispatcher.camera.getPosition();
		Vec3 center = entity.getPosition(partialTick);
		Vec3 camOffset = camera.subtract(center);

		if (!JjkShaderManager.beginVoidBrushEffect(timeSeconds, entity.getShellSeed() * 0.001f + 1.0f, 0.9f, radius, entity.getPhaseProgress(), entity.getPhase().ordinal(), (float) camOffset.x,
				(float) camOffset.y, (float) camOffset.z))
			return;

		poseStack.pushPose();
		poseStack.scale(radius, radius, radius);

		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.VOID_BRUSH_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		for (int i = 0; i < UNIT_SPHERE.length; i += 5)
			vc.addVertex(matrix, UNIT_SPHERE[i], UNIT_SPHERE[i + 1], UNIT_SPHERE[i + 2]).setUv(UNIT_SPHERE[i + 3], UNIT_SPHERE[i + 4]);

		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(JjkShaderManager.VOID_BRUSH_RENDER_TYPE);
	}

	private void renderBlackHole(DomainUVEntity entity, float radius, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.VOID_BLACKHOLE_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		if (!JjkShaderManager.beginVoidBlackholeEffect(timeSeconds, 1.0f))
			return;
		poseStack.pushPose();
		// offsets are fractions of the real radius now, not fixed block counts
		poseStack.translate(radius * 0.6, radius * 0.23, 0.0);
		poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));
		poseStack.scale(radius * 2.4f, radius * 2.4f, radius * 2.4f);
		renderCircularQuad(poseStack, bufferSource, JjkShaderManager.VOID_BLACKHOLE_RENDER_TYPE);
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(JjkShaderManager.VOID_BLACKHOLE_RENDER_TYPE);
	}

	private void renderRift(DomainUVEntity entity, float radius, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.VOID_RIFT_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		if (!JjkShaderManager.beginVoidRiftEffect(timeSeconds, 1.0f))
			return;
		poseStack.pushPose();
		poseStack.translate(0.0, radius * 0.6, 0.0);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
		poseStack.scale(radius * 1.45f, radius * 1.45f, radius * 1.45f);
		renderCircularQuad(poseStack, bufferSource, JjkShaderManager.VOID_RIFT_RENDER_TYPE);
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(JjkShaderManager.VOID_RIFT_RENDER_TYPE);
	}

	/** Inward-wound unit sphere, generated once at class load. */
	private static float[] buildUnitSphere() {
		float[] data = new float[LAT_SEGMENTS * LON_SEGMENTS * 4 * 5];
		int i = 0;
		for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
			float theta1 = (lat / (float) LAT_SEGMENTS) * (float) Math.PI;
			float theta2 = ((lat + 1) / (float) LAT_SEGMENTS) * (float) Math.PI;
			for (int lon = 0; lon < LON_SEGMENTS; lon++) {
				float phi1 = (lon / (float) LON_SEGMENTS) * 2.0f * (float) Math.PI;
				float phi2 = ((lon + 1) / (float) LON_SEGMENTS) * 2.0f * (float) Math.PI;

				float u1 = lon / (float) LON_SEGMENTS;
				float u2 = (lon + 1) / (float) LON_SEGMENTS;
				float v1 = lat / (float) LAT_SEGMENTS;
				float v2 = (lat + 1) / (float) LAT_SEGMENTS;

				// wound v1, v4, v3, v2 so the faces look inward
				i = put(data, i, theta1, phi1, u1, v1);
				i = put(data, i, theta2, phi1, u1, v2);
				i = put(data, i, theta2, phi2, u2, v2);
				i = put(data, i, theta1, phi2, u2, v1);
			}
		}
		return data;
	}

	private static int put(float[] data, int i, float theta, float phi, float u, float v) {
		data[i++] = (float) (Math.sin(theta) * Math.cos(phi));
		data[i++] = (float) Math.cos(theta);
		data[i++] = (float) (Math.sin(theta) * Math.sin(phi));
		data[i++] = u;
		data[i++] = v;
		return i;
	}

	private void renderCircularQuad(PoseStack poseStack, MultiBufferSource bufferSource, net.minecraft.client.renderer.RenderType renderType) {
		VertexConsumer vc = bufferSource.getBuffer(renderType);
		Matrix4f m = poseStack.last().pose();
		int segments = 32;
		float angleStep = (float) (2 * Math.PI / segments);
		for (int i = 0; i < segments; i++) {
			float angle1 = i * angleStep;
			float angle2 = (i + 1) * angleStep;
			float x1 = (float) Math.cos(angle1) * 0.5f;
			float y1 = (float) Math.sin(angle1) * 0.5f;
			float x2 = (float) Math.cos(angle2) * 0.5f;
			float y2 = (float) Math.sin(angle2) * 0.5f;
			vc.addVertex(m, 0, 0, 0).setUv(0.5f, 0.5f);
			vc.addVertex(m, x1, y1, 0).setUv(x1 + 0.5f, y1 + 0.5f);
			vc.addVertex(m, x2, y2, 0).setUv(x2 + 0.5f, y2 + 0.5f);
			vc.addVertex(m, 0, 0, 0).setUv(0.5f, 0.5f);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(DomainUVEntity entity) {
		return ResourceLocation.parse("jjk_strongest:textures/entities/invis.png");
	}
}
