package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;

import net.mcreator.jjkstrongest.entity.HollowPurpleBigEntity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class RenderHollowPurpleSphereProcedure {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/purple_concrete.png");

	// renders simple solid purple sphere
	public static void renderPurpleSphere(Entity entity, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
		if (!(entity instanceof HollowPurpleBigEntity))
			return;
		float scale = (float) ReturnPurpleSizeProcedure.execute(entity);
		poseStack.pushPose();
		// 2x size
		renderSolidSphere(poseStack, bufferSource, scale * 2.0f);
		poseStack.popPose();
	}

	// renders single solid sphere
	private static void renderSolidSphere(PoseStack poseStack, MultiBufferSource bufferSource, float radius) {
		VertexConsumer vertex = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
		int segments = 20;
		for (int lat = 0; lat < segments; lat++) {
			float theta1 = (float) (lat * Math.PI / segments);
			float theta2 = (float) ((lat + 1) * Math.PI / segments);
			for (int lon = 0; lon < segments; lon++) {
				float phi1 = (float) (lon * 2 * Math.PI / segments);
				float phi2 = (float) ((lon + 1) * 2 * Math.PI / segments);
				// vertex 1
				float x1 = radius * Mth.sin(theta1) * Mth.cos(phi1);
				float y1 = radius * Mth.cos(theta1);
				float z1 = radius * Mth.sin(theta1) * Mth.sin(phi1);
				// vertex 2
				float x2 = radius * Mth.sin(theta1) * Mth.cos(phi2);
				float y2 = radius * Mth.cos(theta1);
				float z2 = radius * Mth.sin(theta1) * Mth.sin(phi2);
				// vertex 3
				float x3 = radius * Mth.sin(theta2) * Mth.cos(phi2);
				float y3 = radius * Mth.cos(theta2);
				float z3 = radius * Mth.sin(theta2) * Mth.sin(phi2);
				// vertex 4
				float x4 = radius * Mth.sin(theta2) * Mth.cos(phi1);
				float y4 = radius * Mth.cos(theta2);
				float z4 = radius * Mth.sin(theta2) * Mth.sin(phi1);
				// texture coordinates
				float u1 = (float) lon / segments;
				float u2 = (float) (lon + 1) / segments;
				float v1 = (float) lat / segments;
				float v2 = (float) (lat + 1) / segments;
				// normals for proper shading
				float nx1 = Mth.sin(theta1) * Mth.cos(phi1);
				float ny1 = Mth.cos(theta1);
				float nz1 = Mth.sin(theta1) * Mth.sin(phi1);
				float nx2 = Mth.sin(theta1) * Mth.cos(phi2);
				float ny2 = Mth.cos(theta1);
				float nz2 = Mth.sin(theta1) * Mth.sin(phi2);
				float nx3 = Mth.sin(theta2) * Mth.cos(phi2);
				float ny3 = Mth.cos(theta2);
				float nz3 = Mth.sin(theta2) * Mth.sin(phi2);
				float nx4 = Mth.sin(theta2) * Mth.cos(phi1);
				float ny4 = Mth.cos(theta2);
				float nz4 = Mth.sin(theta2) * Mth.sin(phi1);
				// bright purple color tint (RGB: 200, 120, 255)
				int r = 255;
				int g = 0;
				int b = 255;
				int a = 255;
				// render quad with bright purple tint
				vertex.vertex(poseStack.last().pose(), x1, y1, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(poseStack.last().normal(), nx1, ny1, nz1).endVertex();
				vertex.vertex(poseStack.last().pose(), x2, y2, z2).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(poseStack.last().normal(), nx2, ny2, nz2).endVertex();
				vertex.vertex(poseStack.last().pose(), x3, y3, z3).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(poseStack.last().normal(), nx3, ny3, nz3).endVertex();
				vertex.vertex(poseStack.last().pose(), x4, y4, z4).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(poseStack.last().normal(), nx4, ny4, nz4).endVertex();
			}
		}
	}
}
