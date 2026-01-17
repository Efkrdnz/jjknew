package net.mcreator.jjkstrongest.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.mcreator.jjkstrongest.entity.LapseBlueEntity;

public class LapseBlueModel extends GeoModel<LapseBlueEntity> {
	@Override
	public ResourceLocation getAnimationResource(LapseBlueEntity entity) {
		return new ResourceLocation("jjk_strongest", "animations/lapseblue.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(LapseBlueEntity entity) {
		return new ResourceLocation("jjk_strongest", "geo/lapseblue.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(LapseBlueEntity entity) {
		return new ResourceLocation("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

}
