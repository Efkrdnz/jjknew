package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.mcreator.jjkstrongest.entity.BlueVortexEntity;

import com.mojang.blaze3d.vertex.PoseStack;

public class BlueVortexRenderer extends EntityRenderer<BlueVortexEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("jjk_strongest:textures/entities/invis.png");

	public BlueVortexRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(BlueVortexEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
	}

	@Override
	public ResourceLocation getTextureLocation(BlueVortexEntity entity) {
		return TEXTURE;
	}
}
