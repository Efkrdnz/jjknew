package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.client.DomainFloorRipples;
import net.efkrdnz.jjkstrongest.client.DomainShellTexture;
import net.efkrdnz.jjkstrongest.client.JjkShaderManager;
import net.efkrdnz.jjkstrongest.client.model.Modelblank_entity;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.domain.DomainSource;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.domain.RippleField;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

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
 * <p>What is left is the shell mesh, the floor, and, when you are inside, the reflections.
 * The ink cards that used to drift in the volume are gone too: twenty white splashes at ten
 * blocks were the loudest thing saying "room" in a place that is meant to read as space.
 *
 * <p>The floor is real geometry: a disc at the plane, drawn with depth, over a ball the
 * carve has emptied down to bedrock and beyond. That is what makes a mirror possible. The
 * order of the passes is the whole trick and is worth stating once:
 * <ol>
 * <li><b>dome</b> — paints the lower hemisphere in ink over the pit walls, so nothing of the
 * world shows through the translucent floor;</li>
 * <li><b>mirrored entities</b> — everything in the room drawn again, upside down under the
 * plane;</li>
 * <li><b>floor</b> — the sea, translucent, with depth: it dims the reflections under it and
 * hides anything that is actually down there;</li>
 * <li>during a collapse, the <b>shards</b>.</li>
 * </ol>
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

	/** The floor disc: a fan of this many wedges, in this many rings, on the unit circle. */
	private static final int DISC_SEGMENTS = 64;
	private static final int DISC_RINGS = 4;
	/** Unit disc, as (x, z, u, v) per vertex, wound to face upward. */
	private static final float[] UNIT_DISC = buildUnitDisc();
	/**
	 * The disc sits this far above the plane. The plane is the top of the block the caster
	 * stood on, and for the first ticks of the carve those blocks are still there; coplanar
	 * with them the sea would z-fight the grass.
	 */
	private static final float FLOOR_LIFT = 0.02f;

	/** Reused every frame for the ripple uniform, so the floor allocates nothing. */
	private static final float[] RIPPLE_SCRATCH = new float[RippleField.FLOATS];

	/**
	 * Set the first time the mirror pass throws. Re-entering the entity dispatcher from
	 * inside a renderer is not something vanilla does, so a renderer somewhere that cannot
	 * cope is a real possibility; when one turns up the reflection goes, not the game.
	 */
	private static boolean mirrorDisabled;

	/**
	 * Ticks after the domain turns hostile for the whole arrival to finish. The void itself
	 * fades in over the first quarter of it; the splashes land across the rest.
	 */
	private static final float REVEAL_TICKS = 80.0f;

	/**
	 * The black hole is at infinity: a direction and an angular size, nothing else. Ahead of
	 * the caster (the entity's synced yaw) and up; the shadow alone spans 35 degrees.
	 *
	 * <p>Elevation was 20 degrees, which put the bottom of a 17.5-degree shadow within a
	 * couple of degrees of the horizon — the giant sat on the sea rather than over it. At 35
	 * it clears the water by about the shadow's own radius. Put 20 back to undo.
	 */
	private static final float HOLE_ELEVATION_DEG = 35.0f;
	private static final float SHADOW_RADIUS = 0.305f;

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
		boolean collapsing = phase == DomainPhase.COLLAPSING;

		// Order matters here; the class comment says why.
		renderInterior(entity, radius, partialTick, progress, camOffset, inside, poseStack, bufferSource);
		if (inside) {
			if (!collapsing)
				renderMirroredEntities(entity, partialTick, poseStack, bufferSource, packedLight);
			renderFloor(entity, radius, partialTick, progress, camOffset, poseStack, bufferSource);
		}
		if (collapsing)
			renderShards(entity, radius, partialTick, progress, camOffset, poseStack, bufferSource);
	}

	/**
	 * The sea.
	 *
	 * <p>A disc where the floor plane cuts the sphere, drawn with depth so it hides the pit
	 * and is hidden by whatever stands on it, and translucent so the reflections drawn a
	 * moment earlier show through it, dimmed. Everything about how it looks is in the
	 * shader's floor path; this only puts the geometry where the plane is and hands over the
	 * ripples.
	 */
	private void renderFloor(DomainUVEntity entity, float radius, float partialTick, float progress, Vec3 camOffset, PoseStack poseStack, MultiBufferSource bufferSource) {
		boolean collapsing = entity.getPhase() == DomainPhase.COLLAPSING;
		// While the shell breaks the floor fades, and a fading surface must not write depth
		// or it hides the terrain coming back up through it.
		RenderType renderType = collapsing ? JjkShaderManager.UV_INTERIOR_COLLAPSE_RENDER_TYPE : JjkShaderManager.UV_FLOOR_RENDER_TYPE;
		if (renderType == null)
			return;
		float floorY = entity.getFloorOffset();
		float discSq = radius * radius - floorY * floorY;
		if (discSq <= 0.01f)
			return;
		float discR = (float) Math.sqrt(discSq);

		float[] ripples = null;
		RippleField field = DomainFloorRipples.ripplesFor(entity.getUUID());
		if (field != null && field.pack(RIPPLE_SCRATCH, entity.tickCount) > 0)
			ripples = RIPPLE_SCRATCH;

		if (!beginInteriorUniforms(entity, radius, partialTick, progress, camOffset, true, 1.0f, ripples))
			return;

		poseStack.pushPose();
		VertexConsumer vc = bufferSource.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		float y = floorY + FLOOR_LIFT;
		for (int i = 0; i < UNIT_DISC.length; i += 4)
			vc.addVertex(matrix, UNIT_DISC[i] * discR, y, UNIT_DISC[i + 1] * discR).setUv(UNIT_DISC[i + 2], UNIT_DISC[i + 3]);
		poseStack.popPose();
		if (bufferSource instanceof MultiBufferSource.BufferSource bs)
			bs.endBatch(renderType);
	}

	/**
	 * Everything in the room, drawn again upside down under the plane.
	 *
	 * <p>A mirror is a transform with a negative determinant, which turns every triangle
	 * inside out; with back-face culling on, vanilla's entity render types would then draw
	 * the insides of the models. So the pending batches are flushed, the winding convention
	 * is flipped for the duration of ours, and flipped back. Vanilla never touches
	 * {@code glFrontFace}, so CCW is the right thing to restore.
	 *
	 * <p>A fresh {@link PoseStack} rather than the caller's: if a renderer throws halfway
	 * through, the caller's stack is left exactly as it was found, whatever depth the
	 * dispatcher had pushed to.
	 *
	 * <p>The local player is included on purpose. In first person you do not see yourself,
	 * but you should see yourself in the water.
	 */
	private void renderMirroredEntities(DomainUVEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (mirrorDisabled || !(bufferSource instanceof MultiBufferSource.BufferSource bs))
			return;
		DomainSphere sphere = entity.sphere();
		if (!sphere.isUsable())
			return;
		List<Entity> room = entity.level().getEntities(entity, sphere.bounds(), e -> !(e instanceof DomainSource) && !e.isSpectator() && !e.isInvisible() && sphere.contains(e.position()));
		if (room.isEmpty())
			return;

		EntityRenderDispatcher dispatcher = this.entityRenderDispatcher;
		Vec3 origin = entity.getPosition(partialTick);
		double floorY = entity.getFloorOffset();
		PoseStack mirror = new PoseStack();
		mirror.mulPose(poseStack.last().pose());
		mirror.translate(0.0, 2.0 * floorY, 0.0);
		mirror.scale(1.0f, -1.0f, 1.0f);

		try {
			bs.endBatch();
			dispatcher.setRenderShadow(false);
			GL11.glFrontFace(GL11.GL_CW);
			for (Entity other : room) {
				Vec3 rel = other.getPosition(partialTick).subtract(origin);
				float yaw = Mth.lerp(partialTick, other.yRotO, other.getYRot());
				dispatcher.render(other, rel.x, rel.y, rel.z, yaw, partialTick, mirror, bufferSource, packedLight);
			}
			bs.endBatch();
		} catch (Exception broken) {
			mirrorDisabled = true;
			System.err.println("[JJK Strongest] the Void's floor has stopped reflecting entities: a renderer could not be run mirrored. " + broken);
		} finally {
			GL11.glFrontFace(GL11.GL_CCW);
			dispatcher.setRenderShadow(true);
		}
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
		if (!beginInteriorUniforms(entity, radius, partialTick, progress, camOffset, inside, 0.0f, null))
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
	 * The uniforms the dome and the floor share — everything about the room except which
	 * surface is being drawn. The black hole's placement is worked out here once per pass
	 * rather than per fragment.
	 */
	private boolean beginInteriorUniforms(DomainUVEntity entity, float radius, float partialTick, float progress, Vec3 camOffset, boolean inside, float surface, float[] ripples) {
		boolean collapsing = entity.getPhase() == DomainPhase.COLLAPSING;
		float timeSeconds = (entity.tickCount + partialTick) / 20.0f;
		int shellTexture = DomainShellTexture.upload(entity.shell());

		// The hole is a direction, not a point: the same from every eye in the room, so it
		// never parallaxes and reads as something impossibly far. Vanilla's own yaw/pitch to
		// vector, so it lands exactly where the caster was looking.
		Vec3 holeDir = Vec3.directionFromRotation(-HOLE_ELEVATION_DEG, entity.getHoleYaw());
		float holeAngle = SHADOW_RADIUS;
		// The disc is fixed, not precessing — a thing that size does not wobble on a timer.
		// Nearly edge-on, tipped toward the viewer so the top of the disc shows over the
		// shadow, and rolled a few degrees so it is not perfectly level.
		Vec3 axis = rotateAbout(new Vec3(0.0, 1.0, 0.0).add(holeDir.scale(0.28)).normalize(), holeDir, Math.toRadians(8.0));
		// Was the hole's distance; the hole has none now. Kept so the uniform layout stands.
		double holeDistance = 0.0;

		// The black hole is the one thing that should implode rather than fade: the disc goes
		// first, then the horizon contracts to a point, and it is gone before the shards have
		// spread far enough to see through.
		float discStrength = 1.0f;
		if (collapsing) {
			holeAngle *= 1.0f - smoothstep(0.10f, 0.55f, progress);
			discStrength = 1.0f - smoothstep(0.0f, 0.30f, progress);
		}

		return JjkShaderManager.beginUvInterior(timeSeconds, entity.getShellSeed() * 0.001f + 1.0f, 0.9f, radius, progress, entity.getPhase().ordinal(), (float) camOffset.x, (float) camOffset.y,
				(float) camOffset.z, entity.getFloorOffset(), inside, (float) holeDir.x, (float) holeDir.y, (float) holeDir.z, holeAngle, (float) holeDistance, (float) axis.x, (float) axis.y,
				(float) axis.z, discStrength, entity.getShellIntegrity(), shellTexture, surface, ripples, reveal(entity, partialTick));
	}

	/**
	 * How far the domain has finished arriving, 0..1.
	 *
	 * <p>Zero for the whole forming beat, so the room is black while the shell closes and the
	 * rays burst; then climbing once it turns hostile, which is what brings the void in and
	 * lands the splashes one at a time. One during the collapse — everything is already up by
	 * then, and the phase fade is what takes it away.
	 *
	 * <p>Worked out here rather than in the shader so the GLSL never needs to know a tick
	 * count or a phase duration.
	 */
	private static float reveal(DomainUVEntity entity, float partialTick) {
		DomainPhase phase = entity.getPhase();
		if (phase == DomainPhase.EXPANDING || phase == DomainPhase.SETTLING)
			return 0.0f;
		if (phase == DomainPhase.COLLAPSING)
			return 1.0f;
		// ACTIVE: phase progress is the fraction of the hostile duration elapsed, so this
		// recovers the ticks since it went hostile without the shader knowing either number.
		float activeTicks = entity.getPhaseProgress() * entity.definition().durationTicks() + partialTick;
		return Math.min(1.0f, Math.max(0.0f, activeTicks / REVEAL_TICKS));
	}

	/** Rodrigues' rotation of v about the unit axis k by angle radians. */
	private static Vec3 rotateAbout(Vec3 v, Vec3 k, double angle) {
		double c = Math.cos(angle);
		double sn = Math.sin(angle);
		return v.scale(c).add(k.cross(v).scale(sn)).add(k.scale(k.dot(v) * (1.0 - c)));
	}

	/** GLSL's smoothstep, for the collapse ramps. */
	private static float smoothstep(float edge0, float edge1, float x) {
		float t = Math.max(0.0f, Math.min(1.0f, (x - edge0) / (edge1 - edge0)));
		return t * t * (3.0f - 2.0f * t);
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

	/**
	 * Upward-facing unit disc as quads, generated once at class load. Rings rather than one
	 * fan, so the triangles near the middle are not a hundred blocks long — the fragment
	 * stage reads the interpolated position, and a long thin triangle interpolates badly.
	 */
	private static float[] buildUnitDisc() {
		float[] data = new float[DISC_RINGS * DISC_SEGMENTS * 4 * 4];
		int i = 0;
		for (int ring = 0; ring < DISC_RINGS; ring++) {
			float r1 = ring / (float) DISC_RINGS;
			float r2 = (ring + 1) / (float) DISC_RINGS;
			for (int seg = 0; seg < DISC_SEGMENTS; seg++) {
				double a1 = (seg / (double) DISC_SEGMENTS) * Math.PI * 2.0;
				double a2 = ((seg + 1) / (double) DISC_SEGMENTS) * Math.PI * 2.0;
				i = putDisc(data, i, r1, a1);
				i = putDisc(data, i, r1, a2);
				i = putDisc(data, i, r2, a2);
				i = putDisc(data, i, r2, a1);
			}
		}
		return data;
	}

	private static int putDisc(float[] data, int i, float r, double angle) {
		float x = (float) (Math.cos(angle) * r);
		float z = (float) (Math.sin(angle) * r);
		data[i++] = x;
		data[i++] = z;
		data[i++] = x;
		data[i++] = z;
		return i;
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
