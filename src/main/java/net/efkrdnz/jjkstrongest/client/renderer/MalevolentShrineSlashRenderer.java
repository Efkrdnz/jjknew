package net.efkrdnz.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.client.JjkShaderManager;
import net.efkrdnz.jjkstrongest.client.MalevolentShrineSlashManager;
import net.efkrdnz.jjkstrongest.client.MalevolentShrineSlashManager.DomainSlash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

/**
 * Draws every live shrine slash in one batch.
 *
 * <p>Two things the version before this got wrong, both structural. It set the shader's
 * per-slash uniforms — style, seed, colour — for each slash and then flushed all of them
 * together, so every slash on screen wore the last one's settings; and it copied the whole
 * frame into the scene texture once <em>per slash</em>, some three hundred full-screen blits
 * a frame. Now the frame is copied once, and each slash carries its own four parameters in
 * the vertex colour, so one draw call is correct for all of them.
 *
 * <p>Each quad is a cylindrical billboard around the blade's own axis — its width axis is
 * perpendicular to both the blade and the line to the eye — so a slash is always seen
 * face-on like a beam and never thins to nothing edge-on, whatever its orientation.
 */
@EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MalevolentShrineSlashRenderer {

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;
		if (JjkShaderManager.SHRINE_CLEAVE_RENDER_TYPE == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		List<DomainSlash> slashes = MalevolentShrineSlashManager.getActiveSlashes();
		if (slashes.isEmpty())
			return;

		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		float timeSeconds = (mc.level.getGameTime() + partialTick) / 20.0f;
		if (!JjkShaderManager.beginShrineCleave(timeSeconds))
			return;

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.SHRINE_CLEAVE_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		for (DomainSlash slash : slashes)
			emit(vc, matrix, slash, cameraPos, partialTick);
		bufferSource.endBatch(JjkShaderManager.SHRINE_CLEAVE_RENDER_TYPE);
	}

	/**
	 * One quad, camera-relative, with the slash's state packed into its colour bytes. The
	 * layout is documented at the top of {@code shrine_cleave.fsh}, which decodes it.
	 */
	private static void emit(VertexConsumer vc, Matrix4f matrix, DomainSlash slash, Vec3 cameraPos, float partialTick) {
		Vec3 dir = slash.direction;
		Vec3 centre = slash.position.subtract(cameraPos);
		Vec3 half = dir.scale(slash.length * 0.5);
		Vec3 start = centre.subtract(half);
		Vec3 end = centre.add(half);

		// Width axis: across the blade and across the line of sight at once. When the blade
		// points straight at the eye there is no such axis; any perpendicular will do.
		Vec3 toEye = centre.scale(-1.0);
		Vec3 across = dir.cross(toEye);
		if (across.lengthSqr() < 1.0E-8)
			across = Math.abs(dir.y) < 0.9 ? dir.cross(new Vec3(0.0, 1.0, 0.0)) : dir.cross(new Vec3(1.0, 0.0, 0.0));
		across = across.normalize().scale(slash.width * 0.5);

		int life = (int) (slash.progress(partialTick) * 255.0f);
		int seed = (int) (Mth.frac(slash.seed * 0.01f) * 63.0f);
		int styleSeed = Mth.clamp(slash.style, 0, 3) * 64 + Mth.clamp(seed, 0, 63);
		int jitter = (int) (slash.jitter * 255.0f);
		int sweep = (int) (slash.sweep(partialTick) * 255.0f);

		put(vc, matrix, start.subtract(across), 0.0f, 0.0f, life, styleSeed, jitter, sweep);
		put(vc, matrix, start.add(across), 0.0f, 1.0f, life, styleSeed, jitter, sweep);
		put(vc, matrix, end.add(across), 1.0f, 1.0f, life, styleSeed, jitter, sweep);
		put(vc, matrix, end.subtract(across), 1.0f, 0.0f, life, styleSeed, jitter, sweep);
	}

	private static void put(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v, int r, int g, int b, int a) {
		vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z).setUv(u, v).setColor(r, g, b, a);
	}
}
