
package net.mcreator.jjkstrongest.client.renderer;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

import net.mcreator.jjkstrongest.procedures.ReturnPurpleSizeProcedure;
import net.mcreator.jjkstrongest.entity.model.HollowPurpleProjectileModel;
import net.mcreator.jjkstrongest.entity.layer.HollowPurpleProjectileLayer;
import net.mcreator.jjkstrongest.entity.HollowPurpleProjectileEntity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class HollowPurpleProjectileRenderer extends GeoEntityRenderer<HollowPurpleProjectileEntity> {
	public HollowPurpleProjectileRenderer(EntityRendererProvider.Context renderManager) {
		super(renderManager, new HollowPurpleProjectileModel());
		this.shadowRadius = 0.5f;
		this.addRenderLayer(new HollowPurpleProjectileLayer(this));
	}

	@Override
	public RenderType getRenderType(HollowPurpleProjectileEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}

	@Override
	public void preRender(PoseStack poseStack, HollowPurpleProjectileEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		Level world = entity.level();
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		float scale = (float) ReturnPurpleSizeProcedure.execute(entity);
		this.scaleHeight = scale;
		this.scaleWidth = scale;
		super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
