package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

import net.mcreator.jjkstrongest.entity.FlameArrowEntity;
import net.mcreator.jjkstrongest.client.model.Modelfire_arrow_Converted_Converted;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class FlameArrowRenderer extends EntityRenderer<FlameArrowEntity> {
	private static final ResourceLocation texture = new ResourceLocation("jjk_strongest:textures/entities/fire_arrow.png");
	private final Modelfire_arrow_Converted_Converted model;

	public FlameArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new Modelfire_arrow_Converted_Converted(context.bakeLayer(Modelfire_arrow_Converted_Converted.LAYER_LOCATION));
	}

	@Override
	public void render(FlameArrowEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
		VertexConsumer vb = bufferIn.getBuffer(RenderType.entityCutout(this.getTextureLocation(entityIn)));
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90));
		poseStack.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
		model.renderToBuffer(poseStack, vb, packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
		poseStack.popPose();
		super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(FlameArrowEntity entity) {
		return texture;
	}
}
