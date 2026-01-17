
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.mcreator.jjkstrongest.client.particle.Red03Particle;
import net.mcreator.jjkstrongest.client.particle.Red02Particle;
import net.mcreator.jjkstrongest.client.particle.Red01Particle;
import net.mcreator.jjkstrongest.client.particle.Purple02Particle;
import net.mcreator.jjkstrongest.client.particle.Purple01Particle;
import net.mcreator.jjkstrongest.client.particle.PunchImpactParticle;
import net.mcreator.jjkstrongest.client.particle.ParticleHPChargeParticle;
import net.mcreator.jjkstrongest.client.particle.ParticleHPCharge2Particle;
import net.mcreator.jjkstrongest.client.particle.ParticleBlackFlashParticle;
import net.mcreator.jjkstrongest.client.particle.InfinityParticleParticle;
import net.mcreator.jjkstrongest.client.particle.ExplosionCustomParticle;
import net.mcreator.jjkstrongest.client.particle.Blueparticle3Particle;
import net.mcreator.jjkstrongest.client.particle.Blueparticle2Particle;
import net.mcreator.jjkstrongest.client.particle.Blueparticle1Particle;
import net.mcreator.jjkstrongest.client.particle.BlueDustParticle;
import net.mcreator.jjkstrongest.client.particle.BlueAuraParticle;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class JjkStrongestModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(JjkStrongestModParticleTypes.INFINITY_PARTICLE.get(), InfinityParticleParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.RED_01.get(), Red01Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.RED_02.get(), Red02Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.RED_03.get(), Red03Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.PUNCH_IMPACT.get(), PunchImpactParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.EXPLOSION_CUSTOM.get(), ExplosionCustomParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.PURPLE_01.get(), Purple01Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.PURPLE_02.get(), Purple02Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.BLUEPARTICLE_1.get(), Blueparticle1Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.BLUEPARTICLE_2.get(), Blueparticle2Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.BLUEPARTICLE_3.get(), Blueparticle3Particle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.BLUE_AURA.get(), BlueAuraParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.BLUE_DUST.get(), BlueDustParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.PARTICLE_BLACK_FLASH.get(), ParticleBlackFlashParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.PARTICLE_HP_CHARGE.get(), ParticleHPChargeParticle::provider);
		event.registerSpriteSet(JjkStrongestModParticleTypes.PARTICLE_HP_CHARGE_2.get(), ParticleHPCharge2Particle::provider);
	}
}
