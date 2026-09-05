package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.client.DomainShellTexture;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;
import net.efkrdnz.jjkstrongest.client.model.Modelblank_entity;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

/**
 * Draws Unlimited Void: one shader, on one mesh, in one draw.
 *
 * <p>This used to be four world-space passes — the shell, a camera-facing black-hole
 * billboard at 0.9R, a flat star-shaped disc hanging at 0.6R, and fourteen brush-stroke
 * quads. From inside a sphere there is exactly one shell fragment per pixel, so each of
 * those extra passes was another near-fullscreen layer on top of it. The black hole is
 * part of the interior shader's view ray now, which also makes it a real point in the
 * world: it parallaxes as you walk, cannot clip through the shell, and cannot show you its
 * own back face — three bugs the billboard had, all structurally impossible now. The rift
 * disc is gone entirely; it was a different technique's iconography.
 *
 * <p>What is left is the shell mesh and, when you are inside it, the ink cards.
 */
public class DomainUVRenderer extends MobRenderer<DomainUVEntity, Modelblank_entity<DomainUVEntity>> {

	/**
	 * 32 x 64. The fragment stage reconstructs its view ray from {@code localPos}, which is
	 * interpolated linearly across flat triangles — at 24 x 48 each quad spans about seven
	 * degrees, and that faceting is visible both in the noise and on the silhouette of a
	 * near-black sphere against the sky.
	 */
	private static final int LAT_SEGMENTS = 32;
	private static final int LON_SEGMENTS = 64;
	/** Unit sphere, wound inward, as (x, y, z, u, v) per vertex. */
	private static final float[] UNIT_SPHERE = buildUnitSphere();

	/** Ink splatter cards drifting in the volume. Hard cap; they are real geometry. */
	private static final int INK_COUNT = 20;

	/** Black hole placement, as fractions of the radius: centred, lifted into the dome. */
	private static final double BH_HEIGHT = 0.35;
	private static final double BH_RADIUS = 0.10;

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

		DomainPhase phase = entity.getPhase();
		float progress = entity.getPhaseProgress();
		if (phase == DomainPhase.COLLAPSING) {
			// The phase is synced once a tick and its clock finishes long before the entity
			// does — it sits pinned at exactly 1.0 while the terrain restore catches up,
			// which routinely runs for seconds. Drawing anything then would leave a field of
			// glass hanging motionless in the air over ground that has already come back.
			progress = Math.min(1.0f, progress + partialTick / Math.max(1, entity.definition().collapseTicks()));
			if (progress >= 1.0f)
				return;
		}

		Vec3 camOffset = this.entityRenderDispatcher.camera.getPosition().subtract(entity.getPosition(partialTick));
		boolean inside = camOffset.lengthSqr() < (radius * radius);

		renderInterior(entity, radius, partialTick, progress, camOffset, inside, poseStack, bufferSource);
		if (phase == DomainPhase.COLLAPSING)
			renderShards(entity, radius, partialTick, progress, camOffset, poseStack, bufferSource);
		if (inside && phase != DomainPhase.EXPANDING)
			renderInk(entity, radius, partialTick, progress, phase, poseStack, bufferSource);
	}

	/**
	 * The shell, broken.
	 *
	 * <p>The same mesh the wall was drawn with, so at the moment it breaks the pieces are
	 * exactly the wall that was there a frame earlier — which is the one thing a shatter has
	 * to get right, and the reason this is not a separate set of scattered cards. Every quad
	 * carries its own <em>centre</em> uv here rather than its corner, because that is the only
	 * value all four corners of a quad agree on: it is what lets the vertex shader work out
	 * which shard a vertex belongs to and tear the sphere along shard boundaries instead of
	 * along quad boundaries.
	 *
	 * <p>All the motion — the stagger, the outward burst, gravity, the tumble — happens in
	 * the vertex shader. Doing it here would mean uploading a transform per shard every frame
	 * and reimplementing the shader's clustering in Java so the two agreed on which vertices
	 * move together.
	 */
	private void renderShards(DomainUVEntity entity, float radius, float partialTick, float progress, Vec3 camOffset, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.UV_SHARDS_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		int shellTexture = DomainShellTexture.upload(entity.shell());
		DomainShell shell = entity.shell();
		Vec3 breakDir = shell == null ? new Vec3(0.0, 1.0, 0.0) : shell.weakestDirection();

		if (!JjkShaderManager.beginUvShards(timeSeconds, entity.getShellSeed() * 0.001f + 1.0f, 0.9f, radius, progress, entity.definition().collapseTicks() / 20.0f, (float) breakDir.x,
				(float) breakDir.y, (float) breakDir.z, (float) camOffset.x, (float) camOffset.y, (float) camOffset.z, entity.getShellIntegrity(), shellTexture))
			return;

		// Deliberately NOT scaled by radius: the vertex shader applies it itself, so that the
		// gravity and burst terms stay in blocks instead of being multiplied by the model
		// matrix along with everything else.
		poseStack.pushPose();
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.UV_SHARDS_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		for (int i = 0; i < UNIT_SPHERE.length; i += 5) {
			int quad = i / 20;
			int lat = quad / LON_SEGMENTS;
			int lon = quad % LON_SEGMENTS;
			// The +0.5 is what makes this unambiguous: a corner uv sits exactly on an integer
			// boundary once scaled, so flooring it could land on either neighbouring quad.
			float centreU = (lon + 0.5f) / LON_SEGMENTS;
			float centreV = (lat + 0.5f) / LAT_SEGMENTS;
			vc.addVertex(matrix, UNIT_SPHERE[i], UNIT_SPHERE[i + 1], UNIT_SPHERE[i + 2]).setUv(centreU, centreV);
		}
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(JjkShaderManager.UV_SHARDS_RENDER_TYPE);
	}

	private void renderInterior(DomainUVEntity entity, float radius, float partialTick, float progress, Vec3 camOffset, boolean inside, PoseStack poseStack, MultiBufferSource bufferSource) {
		boolean collapsing = entity.getPhase() == DomainPhase.COLLAPSING;
		// Colour only while collapsing. The ordinary interior writes depth — that is what
		// makes it hide the world outside — and a dome fading to nothing must not, or it
		// punches an opaque hole in everything behind it right up until it disappears.
		RenderType renderType = collapsing ? JjkShaderManager.UV_INTERIOR_COLLAPSE_RENDER_TYPE : JjkShaderManager.UV_INTERIOR_RENDER_TYPE;
		if (renderType == null)
			return;

		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		int shellTexture = DomainShellTexture.upload(entity.shell());

		// The hole sits on the sphere's axis. Its direction and apparent size are worked
		// out here rather than per fragment: it costs a normalize and a length once instead
		// of once per pixel, and it means the placement can be tuned without touching GLSL.
		Vec3 hole = new Vec3(0.0, radius * BH_HEIGHT, 0.0);
		Vec3 toHole = hole.subtract(camOffset);
		double holeDistance = Math.max(0.001, toHole.length());
		Vec3 holeDir = toHole.scale(1.0 / holeDistance);
		double holeWorldRadius = radius * BH_RADIUS;
		float holeAngle = (float) Math.atan2(holeWorldRadius, holeDistance);
		// The disc plane precesses, so the domain never looks like a still image.
		double spin = timeSeconds * 0.05;
		Vec3 axis = new Vec3(Math.sin(spin) * 0.35, 1.0, Math.cos(spin) * 0.35).normalize();

		// The black hole is the one thing that should implode rather than fade: the disc goes
		// first, then the horizon contracts to a point, and it is gone before the shards have
		// spread far enough to see through.
		float discStrength = 1.0f;
		if (collapsing) {
			holeAngle *= 1.0f - smoothstep(0.10f, 0.55f, progress);
			discStrength = 1.0f - smoothstep(0.0f, 0.30f, progress);
		}

		if (!JjkShaderManager.beginUvInterior(timeSeconds, entity.getShellSeed() * 0.001f + 1.0f, 0.9f, radius, progress, entity.getPhase().ordinal(), (float) camOffset.x,
				(float) camOffset.y, (float) camOffset.z, entity.getFloorOffset(), inside, (float) holeDir.x, (float) holeDir.y, (float) holeDir.z, holeAngle, (float) holeDistance, (float) axis.x,
				(float) axis.y, (float) axis.z, discStrength, entity.getShellIntegrity(), shellTexture))
			return;

		poseStack.pushPose();
		poseStack.scale(radius, radius, radius);

		VertexConsumer vc = bufferSource.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		for (int i = 0; i < UNIT_SPHERE.length; i += 5)
			vc.addVertex(matrix, UNIT_SPHERE[i], UNIT_SPHERE[i + 1], UNIT_SPHERE[i + 2]).setUv(UNIT_SPHERE[i + 3], UNIT_SPHERE[i + 4]);

		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(renderType);
	}

	/**
	 * Ink splatter suspended in the volume.
	 *
	 * <p>These are geometry rather than another shader layer for one reason: they have to
	 * pass in front of and behind the people in the room. Nothing painted onto the shell
	 * surface can do that, however much parallax you fake into it.
	 *
	 * <p>All twenty go out in one draw call. The card index rides in the V channel
	 * ({@code v = id + sv * 0.5}) rather than in a uniform, so the shader can vary every
	 * card without the renderer flushing a batch per card.
	 */
	private void renderInk(DomainUVEntity entity, float radius, float partialTick, float progress, DomainPhase phase, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (JjkShaderManager.UV_INK_RENDER_TYPE == null)
			return;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		int seed = entity.getShellSeed();
		// Held back while the shell settles, so the volume fills after the walls arrive, and
		// taken away early in a collapse so twenty white blots do not hang in open air.
		float alpha = 0.9f;
		if (phase == DomainPhase.SETTLING)
			alpha *= progress;
		else if (phase == DomainPhase.COLLAPSING)
			alpha *= 1.0f - smoothstep(0.0f, 0.35f, progress);
		if (alpha <= 0.01f)
			return;
		if (!JjkShaderManager.beginUvInk(timeSeconds, seed * 0.001f + 1.0f, alpha, radius * 2.0f))
			return;

		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.UV_INK_RENDER_TYPE);
		for (int i = 0; i < INK_COUNT; i++) {
			float h1 = hash(seed, i, 0);
			float h2 = hash(seed, i, 1);
			float h3 = hash(seed, i, 2);
			float h4 = hash(seed, i, 3);
			float h5 = hash(seed, i, 4);
			float h6 = hash(seed, i, 5);

			// Uniform on the sphere of directions, then pulled well inside so no card ever
			// reaches the wall it is supposed to be floating in front of.
			double azimuth = h1 * Math.PI * 2.0 + timeSeconds * (0.008 + h6 * 0.016);
			double polar = Math.acos(1.0 - 2.0 * h2);
			double dist = radius * (0.20 + 0.55 * h3);
			double sinPolar = Math.sin(polar);
			double bob = Math.sin(timeSeconds * (0.12 + h6 * 0.18) + i) * radius * 0.05;

			// Mostly small, a few large. A field of identically-sized blots reads as a
			// pattern; the spread is what makes it read as depth.
			float size = radius * (h4 < 0.8f ? 0.05f + 0.07f * h4 : 0.16f + 0.10f * h4);

			poseStack.pushPose();
			poseStack.translate(dist * sinPolar * Math.cos(azimuth), dist * Math.cos(polar) + bob, dist * sinPolar * Math.sin(azimuth));
			poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			poseStack.mulPose(Axis.ZP.rotationDegrees(h5 * 360.0f + timeSeconds * (h6 - 0.5f) * 4.0f));

			Matrix4f m = poseStack.last().pose();
			float half = size * 0.5f;
			float v0 = i;
			float v1 = i + 0.5f;
			vc.addVertex(m, -half, -half, 0.0f).setUv(0.0f, v0);
			vc.addVertex(m, -half, half, 0.0f).setUv(0.0f, v1);
			vc.addVertex(m, half, half, 0.0f).setUv(1.0f, v1);
			vc.addVertex(m, half, -half, 0.0f).setUv(1.0f, v0);
			poseStack.popPose();
		}
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(JjkShaderManager.UV_INK_RENDER_TYPE);
	}

	/** GLSL's smoothstep, for the collapse ramps. */
	private static float smoothstep(float edge0, float edge1, float x) {
		float t = Math.max(0.0f, Math.min(1.0f, (x - edge0) / (edge1 - edge0)));
		return t * t * (3.0f - 2.0f * t);
	}

	/** Stable per-domain, per-card noise, so the cards do not reshuffle every frame. */
	private static float hash(int seed, int index, int channel) {
		int h = seed * 374761393 + index * 668265263 + channel * 1442695041;
		h = (h ^ (h >>> 13)) * 1274126177;
		return ((h ^ (h >>> 16)) & 0x7fffffff) / (float) 0x7fffffff;
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

				// u and v are fractions of the segment counts, so the mapping the damage
				// grid is keyed on is unchanged by the resolution bump.
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

	@Override
	public ResourceLocation getTextureLocation(DomainUVEntity entity) {
		return ResourceLocation.parse("jjk_strongest:textures/entities/invis.png");
	}
}
