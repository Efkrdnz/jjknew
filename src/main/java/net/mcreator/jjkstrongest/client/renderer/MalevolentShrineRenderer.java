
package net.mcreator.jjkstrongest.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.mcreator.jjkstrongest.entity.MalevolentShrineEntity;
import net.mcreator.jjkstrongest.client.model.Modelmalevolent_shrine;

public class MalevolentShrineRenderer extends MobRenderer<MalevolentShrineEntity, Modelmalevolent_shrine<MalevolentShrineEntity>> {
	public MalevolentShrineRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelmalevolent_shrine(context.bakeLayer(Modelmalevolent_shrine.LAYER_LOCATION)), 2f);
	}

	@Override
	public ResourceLocation getTextureLocation(MalevolentShrineEntity entity) {
		return new ResourceLocation("jjk_strongest:textures/entities/malevolent_shrine.png");
	}
}
