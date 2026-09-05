
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

import net.efkrdnz.jjkstrongest.client.renderer.SukunaRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.GojoRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.ReversalRedRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.MalevolentShrineRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.MahoragaRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.LapseBlueRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.ImaginaryPurpleRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.HollowPurpleProjectileRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.HollowPurpleChargeRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.HollowPurpleBigRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.HollowNukeRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.FugaDomainExplosionRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.FlameArrowRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.FlameArrowExplosionRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.DomainUVRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.DismantleTravelRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.DismantleProjectileRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.BFEntityRenderer;
import net.efkrdnz.jjkstrongest.client.renderer.BlueVortexRenderer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class JjkStrongestModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(JjkStrongestModEntities.LAPSE_BLUE.get(), LapseBlueRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.REVERSAL_RED.get(), ReversalRedRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_PURPLE_CHARGE.get(), HollowPurpleChargeRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_PURPLE_PROJECTILE.get(), HollowPurpleProjectileRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), DismantleProjectileRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.BF_ENTITY.get(), BFEntityRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.MALEVOLENT_SHRINE.get(), MalevolentShrineRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.DOMAIN_UV.get(), DomainUVRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.DEBUG_BOT.get(), net.efkrdnz.jjkstrongest.client.renderer.DebugBotRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.FLAME_ARROW.get(), FlameArrowRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.FLAME_ARROW_EXPLOSION.get(), FlameArrowExplosionRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_PURPLE_BIG.get(), HollowPurpleBigRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.IMAGINARY_PURPLE.get(), ImaginaryPurpleRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.MAHORAGA.get(), MahoragaRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.FUGA_DOMAIN_EXPLOSION.get(), FugaDomainExplosionRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.HOLLOW_NUKE.get(), HollowNukeRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.DISMANTLE_TRAVEL.get(), DismantleTravelRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.SUKUNA.get(), SukunaRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.GOJO.get(), GojoRenderer::new);
		event.registerEntityRenderer(JjkStrongestModEntities.BLUE_VORTEX.get(), BlueVortexRenderer::new);
	}
}
