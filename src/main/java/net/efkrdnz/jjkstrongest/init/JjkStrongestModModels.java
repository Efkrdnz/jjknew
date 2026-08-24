
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

import net.efkrdnz.jjkstrongest.client.model.Modelmalevolent_shrine;
import net.efkrdnz.jjkstrongest.client.model.Modelfire_arrow_Converted_Converted;
import net.efkrdnz.jjkstrongest.client.model.Modelblank_entity;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class JjkStrongestModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Modelmalevolent_shrine.LAYER_LOCATION, Modelmalevolent_shrine::createBodyLayer);
		event.registerLayerDefinition(Modelfire_arrow_Converted_Converted.LAYER_LOCATION, Modelfire_arrow_Converted_Converted::createBodyLayer);
		event.registerLayerDefinition(Modelblank_entity.LAYER_LOCATION, Modelblank_entity::createBodyLayer);
	}
}
