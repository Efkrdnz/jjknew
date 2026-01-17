
package net.mcreator.jjkstrongest.client.renderer;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

import net.mcreator.jjkstrongest.entity.model.LapseBlueModel;
import net.mcreator.jjkstrongest.entity.layer.LapseBlueLayer;
import net.mcreator.jjkstrongest.entity.LapseBlueEntity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class LapseBlueRenderer extends GeoEntityRenderer<LapseBlueEntity> {
	public LapseBlueRenderer(EntityRendererProvider.Context renderManager) {
		super(renderManager, new LapseBlueModel());
		this.shadowRadius = 0.5f;
		this.addRenderLayer(new LapseBlueLayer(this));
	}

	@Override
	public RenderType getRenderType(LapseBlueEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}

	@Override
	public void preRender(PoseStack poseStack, LapseBlueEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green,
			float blue, float alpha) {
		float scale = 0.1f;
		this.scaleHeight = scale;
		this.scaleWidth = scale;
		super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
