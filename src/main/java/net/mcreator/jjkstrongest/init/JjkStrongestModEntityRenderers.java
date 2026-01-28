
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.mcreator.jjkstrongest.client.renderer.ReversalRedRenderer;
import net.mcreator.jjkstrongest.client.renderer.MalevolentShrineRenderer;
import net.mcreator.jjkstrongest.client.renderer.LapseBlueRenderer;
import net.mcreator.jjkstrongest.client.renderer.InfiniteHollowRenderer;
import net.mcreator.jjkstrongest.client.renderer.HollowPurpleProjectileRenderer;
import net.mcreator.jjkstrongest.client.renderer.HollowPurpleChargeRenderer;
import net.mcreator.jjkstrongest.client.renderer.HollowPurpleBigRenderer;
import net.mcreator.jjkstrongest.client.renderer.FlameArrowRenderer;
import net.mcreator.jjkstrongest.client.renderer.FlameArrowExplosionRenderer;
import net.mcreator.jjkstrongest.client.renderer.DomainUVRenderer;
import net.mcreator.jjkstrongest.client.renderer.DismantleProjectileRenderer;
import net.mcreator.jjkstrongest.client.renderer.BFEntityRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class JjkStrongestModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(JjkStrongestModEntities.LAPSE_BLUE.get(), LapseBlueRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.REVERSAL_RED.get(), ReversalRedRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_PURPLE_CHARGE.get(), HollowPurpleChargeRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_PURPLE_PROJECTILE.get(), HollowPurpleProjectileRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.INFINITE_HOLLOW.get(), InfiniteHollowRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), DismantleProjectileRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.BF_ENTITY.get(), BFEntityRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.MALEVOLENT_SHRINE.get(), MalevolentShrineRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.DOMAIN_UV.get(), DomainUVRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.FLAME_ARROW.get(), FlameArrowRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.FLAME_ARROW_EXPLOSION.get(), FlameArrowExplosionRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_PURPLE_BIG.get(), HollowPurpleBigRenderer::new);
	}
}
