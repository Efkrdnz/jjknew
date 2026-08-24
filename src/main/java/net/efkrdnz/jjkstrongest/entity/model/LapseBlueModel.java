package net.efkrdnz.jjkstrongest.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.entity.LapseBlueEntity;

public class LapseBlueModel extends GeoModel<LapseBlueEntity> {
	@Override
	public ResourceLocation getAnimationResource(LapseBlueEntity entity) {
		return ResourceLocation.fromNamespaceAndPath("jjk_strongest", "animations/lapseblue.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(LapseBlueEntity entity) {
		return ResourceLocation.fromNamespaceAndPath("jjk_strongest", "geo/lapseblue.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(LapseBlueEntity entity) {
		return ResourceLocation.fromNamespaceAndPath("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

}
