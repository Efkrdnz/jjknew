package net.efkrdnz.jjkstrongest.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.entity.DebugBotEntity;

/**
 * Draws a debug bot as whichever sorcerer it was spawned as.
 *
 * <p>Wearing the right face matters more than it sounds: the whole point of the bot is
 * watching two of them fight from a distance, and at forty blocks the only thing telling
 * you which is which is the silhouette and the name over its head.
 */
public class DebugBotRenderer extends HumanoidMobRenderer<DebugBotEntity, HumanoidModel<DebugBotEntity>> {

	private static final ResourceLocation GOJO = ResourceLocation.parse("jjk_strongest:textures/entities/gojoskin.png");
	// The same opaquely-named file SukunaRenderer uses; it is his skin.
	private static final ResourceLocation SUKUNA = ResourceLocation.parse("jjk_strongest:textures/entities/c9e97bce71d8c9f9.png");
	private static final ResourceLocation FALLBACK = ResourceLocation.parse("minecraft:textures/entity/player/wide/steve.png");

	public DebugBotRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(DebugBotEntity entity) {
		return switch (entity.getCharacter()) {
			case "gojo" -> GOJO;
			case "sukuna" -> SUKUNA;
			default -> FALLBACK;
		};
	}
}
