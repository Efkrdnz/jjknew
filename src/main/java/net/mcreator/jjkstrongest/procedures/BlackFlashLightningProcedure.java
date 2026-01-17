package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class BlackFlashLightningProcedure {
	// trigger lightning effect around target
	public static void execute(Level world, Entity target) {
		if (target == null || !world.isClientSide)
			return;
		Minecraft mc = Minecraft.getInstance();
		RandomSource random = RandomSource.create();
		Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
		// schedule render for next frame
		mc.execute(() -> {
			PoseStack poseStack = new PoseStack();
			MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
			// render 4-6 lightning branches
			int branches = 4 + random.nextInt(3);
			for (int i = 0; i < branches; i++) {
				renderLightningBranch(poseStack, bufferSource, targetPos, random, target.getBbWidth());
			}
			bufferSource.endBatch();
		});
	}

	// render single lightning branch
	private static void renderLightningBranch(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 center, RandomSource random, float radius) {
		// start position near target
		float angle = random.nextFloat() * 6.28f;
		float dist = radius * 0.3f;
		Vec3 start = center.add(Math.cos(angle) * dist, (random.nextFloat() - 0.5f) * radius, Math.sin(angle) * dist);
		// end position further out
		angle += (random.nextFloat() - 0.5f) * 1.5f;
		dist = radius * (1.2f + random.nextFloat() * 0.8f);
		Vec3 end = center.add(Math.cos(angle) * dist, (random.nextFloat() - 0.5f) * radius * 1.5f, Math.sin(angle) * dist);
		// draw lightning with segments
		int segments = 5 + random.nextInt(4);
		Vec3 current = start;
		for (int i = 0; i < segments; i++) {
			float progress = (float) i / segments;
			Vec3 next;
			if (i == segments - 1) {
				next = end;
			} else {
				// interpolate with random offset
				next = start.lerp(end, progress + 1.0f / segments);
				next = next.add((random.nextFloat() - 0.5f) * radius * 0.4f, (random.nextFloat() - 0.5f) * radius * 0.4f, (random.nextFloat() - 0.5f) * radius * 0.4f);
			}
			// draw red outline (thicker)
			drawLightningSegment(poseStack, bufferSource, current, next, 0.15f, 0.8f, 0.1f, 0.1f);
			// draw black core (thinner)
			drawLightningSegment(poseStack, bufferSource, current, next, 0.08f, 0.05f, 0.05f, 0.05f);
			current = next;
		}
	}

	// draw single lightning segment
	private static void drawLightningSegment(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 start, Vec3 end, float width, float r, float g, float b) {
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.lightning());
		Matrix4f matrix = poseStack.last().pose();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 offset = start.subtract(camera);
		Vec3 dir = end.subtract(start).normalize();
		Vec3 side = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);
		// draw quad
		addVertex(buffer, matrix, start.add(side), r, g, b);
		addVertex(buffer, matrix, end.add(side), r, g, b);
		addVertex(buffer, matrix, end.subtract(side), r, g, b);
		addVertex(buffer, matrix, start.subtract(side), r, g, b);
	}

	// add vertex to buffer
	private static void addVertex(VertexConsumer buffer, Matrix4f matrix, Vec3 pos, float r, float g, float b) {
		buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).color(r, g, b, 1.0f).endVertex();
	}
}
