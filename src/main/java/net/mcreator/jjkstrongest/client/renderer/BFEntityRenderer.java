
package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.mcreator.jjkstrongest.entity.BFEntityEntity;
import net.mcreator.jjkstrongest.client.model.Modelblank_entity;

public class BFEntityRenderer extends MobRenderer<BFEntityEntity, Modelblank_entity<BFEntityEntity>> {
	public BFEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	public ResourceLocation getTextureLocation(BFEntityEntity entity) {
		return new ResourceLocation("jjk_strongest:textures/entities/invis.png");
	}
}
