package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.client.particle.BlueDustParticle;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.List;
import java.util.Random;

/**
 * The information drifting inside Unlimited Void.
 *
 * <p>Emitted from the client tick rather than the domain's server tick on purpose:
 * every client already knows where the domains are — {@link DomainRegistry} is fed by
 * entity join and leave events, which fire on both logical sides — so spawning locally
 * costs nothing on the wire. A server-side emitter would be a packet per mote per
 * player, several hundred a second, for something purely decorative.
 *
 * <p>No new particle class. {@link BlueDustParticle} is already gravity-free, physics-free
 * and full-bright; it only needed a longer life and a gentler spin, which
 * {@link BlueDustParticle#asInteriorMote} gives it.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public final class DomainInteriorParticles {

	/** Emit on every Nth tick. With the lifetime below this settles at ~160 live motes. */
	private static final int EMIT_INTERVAL = 2;
	/** Motes per emitting tick. The hard cap on this effect, before any settings scaling. */
	private static final int MOTES_PER_EMIT = 2;
	private static final int LIFETIME_MIN = 120;
	private static final int LIFETIME_MAX = 220;
	/**
	 * Nothing spawns closer than this to the eye. Without it the centre of the sphere —
	 * where the density is deliberately highest — puts motes inside your own head.
	 */
	private static final double MIN_EYE_DISTANCE = 3.5;
	/** Tries at finding a spot outside that radius before giving up on this mote. */
	private static final int PLACEMENT_ATTEMPTS = 4;

	private static final Random RANDOM = new Random();
	private static int tickCounter;

	private DomainInteriorParticles() {
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		if (DomainRegistry.activeCount == 0)
			return;
		if (++tickCounter % EMIT_INTERVAL != 0)
			return;

		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Entity camera = mc.getCameraEntity();
		if (level == null || camera == null || mc.isPaused())
			return;

		ParticleStatus status = mc.options.particles().get();
		if (status == ParticleStatus.MINIMAL)
			return;
		int budget = status == ParticleStatus.DECREASED ? 1 : MOTES_PER_EMIT;

		double eyeX = camera.getX();
		double eyeY = camera.getEyeY();
		double eyeZ = camera.getZ();

		// Only the domain you are standing in emits. From outside you cannot see the
		// volume anyway — the shell draws over it — so there is nothing to pay for.
		DomainUVEntity host = null;
		DomainSphere sphere = null;
		List<DomainUVEntity> domains = DomainRegistry.voidsIn(level);
		for (DomainUVEntity domain : domains) {
			if (!domain.isAlive())
				continue;
			DomainPhase phase = domain.getPhase();
			if (phase != DomainPhase.ACTIVE && phase != DomainPhase.SETTLING)
				continue;
			DomainSphere candidate = domain.sphere();
			if (candidate.isUsable() && candidate.contains(eyeX, eyeY, eyeZ)) {
				host = domain;
				sphere = candidate;
				break;
			}
		}
		if (host == null)
			return;

		SimpleParticleType type = JjkStrongestModParticleTypes.BLUE_DUST.get();
		double radius = sphere.radius();
		for (int i = 0; i < budget; i++)
			spawnMote(mc, level, type, sphere, radius, eyeX, eyeY, eyeZ);
	}

	private static void spawnMote(Minecraft mc, ClientLevel level, SimpleParticleType type, DomainSphere sphere, double radius, double eyeX, double eyeY, double eyeZ) {
		for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
			// Uniform direction, then a radial fraction raised to a power so the middle of
			// the sphere is crowded and the wall is nearly bare. A uniform-density ball
			// would want cbrt here; this is deliberately the other way.
			double polar = Math.acos(1.0 - 2.0 * RANDOM.nextDouble());
			double azimuth = RANDOM.nextDouble() * Math.PI * 2.0;
			double dist = radius * Math.pow(RANDOM.nextDouble(), 1.6);
			double sinPolar = Math.sin(polar);
			double x = sphere.center().x + dist * sinPolar * Math.cos(azimuth);
			double y = sphere.center().y + dist * Math.cos(polar);
			double z = sphere.center().z + dist * sinPolar * Math.sin(azimuth);

			double dx = x - eyeX;
			double dy = y - eyeY;
			double dz = z - eyeZ;
			if (dx * dx + dy * dy + dz * dz < MIN_EYE_DISTANCE * MIN_EYE_DISTANCE)
				continue;

			// A drift, not a launch: a mote crosses about two blocks in its whole life.
			double vx = (RANDOM.nextDouble() - 0.5) * 0.016;
			double vy = (RANDOM.nextDouble() - 0.5) * 0.010 + 0.004;
			double vz = (RANDOM.nextDouble() - 0.5) * 0.016;

			Particle particle = mc.particleEngine.createParticle(type, x, y, z, vx, vy, vz);
			if (particle instanceof BlueDustParticle dust)
				dust.asInteriorMote(LIFETIME_MIN + RANDOM.nextInt(LIFETIME_MAX - LIFETIME_MIN), 0.02f, 0.35f + RANDOM.nextFloat() * 0.35f);
			return;
		}
	}
}
