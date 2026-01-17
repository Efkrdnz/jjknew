
package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.mcreator.jjkstrongest.entity.DomainUVEntity;
import net.mcreator.jjkstrongest.client.model.Modelblank_entity;

public class DomainUVRenderer extends MobRenderer<DomainUVEntity, Modelblank_entity<DomainUVEntity>> {
	public DomainUVRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	public ResourceLocation getTextureLocation(DomainUVEntity entity) {
		return new ResourceLocation("jjk_strongest:textures/entities/invis.png");
	}
}
