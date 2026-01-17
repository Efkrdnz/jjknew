package net.mcreator.jjkstrongest.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.mcreator.jjkstrongest.entity.HollowPurpleChargeEntity;

public class HollowPurpleChargeModel extends GeoModel<HollowPurpleChargeEntity> {
	@Override
	public ResourceLocation getAnimationResource(HollowPurpleChargeEntity entity) {
		return new ResourceLocation("jjk_strongest", "animations/hollow_purple.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(HollowPurpleChargeEntity entity) {
		return new ResourceLocation("jjk_strongest", "geo/hollow_purple.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(HollowPurpleChargeEntity entity) {
		return new ResourceLocation("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

}
