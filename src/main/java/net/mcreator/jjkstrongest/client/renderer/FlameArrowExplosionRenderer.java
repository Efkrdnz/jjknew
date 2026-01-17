
package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.mcreator.jjkstrongest.entity.FlameArrowExplosionEntity;
import net.mcreator.jjkstrongest.client.model.Modelblank_entity;

public class FlameArrowExplosionRenderer extends MobRenderer<FlameArrowExplosionEntity, Modelblank_entity<FlameArrowExplosionEntity>> {
	public FlameArrowExplosionRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelblank_entity(context.bakeLayer(Modelblank_entity.LAYER_LOCATION)), 0f);
	}

	@Override
	public ResourceLocation getTextureLocation(FlameArrowExplosionEntity entity) {
		return new ResourceLocation("jjk_strongest:textures/entities/invis.png");
	}
}
