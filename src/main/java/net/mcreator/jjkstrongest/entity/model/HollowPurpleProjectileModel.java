package net.mcreator.jjkstrongest.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

import net.mcreator.jjkstrongest.entity.HollowPurpleProjectileEntity;

public class HollowPurpleProjectileModel extends GeoModel<HollowPurpleProjectileEntity> {
	@Override
	public ResourceLocation getAnimationResource(HollowPurpleProjectileEntity entity) {
		return new ResourceLocation("jjk_strongest", "animations/hollow_purple_attack.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(HollowPurpleProjectileEntity entity) {
		return new ResourceLocation("jjk_strongest", "geo/hollow_purple_attack.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(HollowPurpleProjectileEntity entity) {
		return new ResourceLocation("jjk_strongest", "textures/entities/" + entity.getTexture() + ".png");
	}

}
