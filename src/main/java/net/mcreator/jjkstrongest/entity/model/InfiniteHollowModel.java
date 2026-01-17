package net.mcreator.jjkstrongest.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.mcreator.jjkstrongest.entity.InfiniteHollowEntity;

public class InfiniteHollowModel extends GeoModel<InfiniteHollowEntity> {
	@Override
	public ResourceLocation getAnimationResource(InfiniteHollowEntity entity) {
		return new ResourceLocation("jjk_strongest", "animations/infinitehollow.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(InfiniteHollowEntity entity) {
		return new ResourceLocation("jjk_strongest", "geo/infinitehollow.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(InfiniteHollowEntity entity) {
		return new ResourceLocation("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

}
