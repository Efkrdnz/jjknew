package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;

import net.mcreator.jjkstrongest.entity.GojoEntity;

public class GojoRenderer extends HumanoidMobRenderer<GojoEntity, HumanoidModel<GojoEntity>> {
	public GojoRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer<>(this,
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(GojoEntity entity) {
		// Place your Gojo texture at:
		// src/main/resources/assets/jjk_strongest/textures/entities/gojo.png
		return new ResourceLocation("jjk_strongest:textures/entities/gojoskin.png");
	}
}
