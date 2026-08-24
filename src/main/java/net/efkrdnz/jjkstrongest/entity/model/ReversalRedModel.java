package net.efkrdnz.jjkstrongest.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.entity.ReversalRedEntity;

public class ReversalRedModel extends GeoModel<ReversalRedEntity> {
	@Override
	public ResourceLocation getAnimationResource(ReversalRedEntity entity) {
		return ResourceLocation.fromNamespaceAndPath("jjk_strongest", "animations/reversalred.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(ReversalRedEntity entity) {
		return ResourceLocation.fromNamespaceAndPath("jjk_strongest", "geo/reversalred.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(ReversalRedEntity entity) {
		return ResourceLocation.fromNamespaceAndPath("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

}
