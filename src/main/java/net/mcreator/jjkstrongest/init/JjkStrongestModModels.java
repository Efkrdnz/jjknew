
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.mcreator.jjkstrongest.client.model.Modelmalevolent_shrine;
import net.mcreator.jjkstrongest.client.model.Modelfire_arrow_Converted_Converted;
import net.mcreator.jjkstrongest.client.model.Modelblank_entity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class JjkStrongestModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Modelmalevolent_shrine.LAYER_LOCATION, Modelmalevolent_shrine::createBodyLayer);
		event.registerLayerDefinition(Modelfire_arrow_Converted_Converted.LAYER_LOCATION, Modelfire_arrow_Converted_Converted::createBodyLayer);
		event.registerLayerDefinition(Modelblank_entity.LAYER_LOCATION, Modelblank_entity::createBodyLayer);
	}
}
