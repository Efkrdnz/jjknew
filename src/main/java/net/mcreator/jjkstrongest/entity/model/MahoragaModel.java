package net.mcreator.jjkstrongest.entity.model;

import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.constant.DataTickets;

import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaModel extends GeoModel<MahoragaEntity> {
	@Override
	public ResourceLocation getAnimationResource(MahoragaEntity entity) {
		return new ResourceLocation("jjk_strongest", "animations/mahoraga.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(MahoragaEntity entity) {
		return new ResourceLocation("jjk_strongest", "geo/mahoraga.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(MahoragaEntity entity) {
		return new ResourceLocation("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

	@Override
	public void setCustomAnimations(MahoragaEntity animatable, long instanceId, AnimationState animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("Head");
		if (head != null) {
			EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
			head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
			head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
		}

	}
}
