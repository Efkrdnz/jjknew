package net.efkrdnz.jjkstrongest.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.domain.RippleField;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.domain.DomainSource;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Turns footsteps on the Void's floor into ripples.
 *
 * <p>Purely client side, on the same pattern as {@link DomainInteriorParticles}: the entities
 * are already here, so watching them costs nothing on the wire. Each entity standing on the
 * sea gets a ripple every {@link #STRIDE} blocks walked and a bigger one when it lands, sized
 * by how far it fell. Nothing else disturbs the water — a still sea is the point, and the
 * rings are what tell you it is water at all.
 *
 * <p>Falls are measured from our own position history rather than {@code fallDistance},
 * which the client only tracks for the local player. Remote entities arrive by
 * interpolated position with an on-ground flag, and the highest point since they last
 * touched down is all a landing needs.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public final class DomainFloorRipples {

	/** Blocks walked between steps. About one footfall at walking pace. */
	private static final double STRIDE = 0.85;
	/** A drop shorter than this is a step off a ledge, not a landing. */
	private static final double LANDING_MIN_DROP = 0.5;

	private static final Map<UUID, RippleField> FIELDS = new HashMap<>();
	private static final WeakHashMap<Entity, Tracker> TRACKERS = new WeakHashMap<>();
	private static final Set<UUID> LIVE = new HashSet<>();

	private static final class Tracker {
		double lastX;
		double lastZ;
		double walked;
		boolean wasOnGround;
		double peakY;

		Tracker(Entity e) {
			lastX = e.getX();
			lastZ = e.getZ();
			peakY = e.getY();
			wasOnGround = e.onGround();
		}
	}

	private DomainFloorRipples() {
	}

	/** The ripples on this domain's floor, or null when nothing has touched it yet. */
	public static RippleField ripplesFor(UUID domainId) {
		return FIELDS.get(domainId);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		if (DomainRegistry.activeCount == 0) {
			if (!FIELDS.isEmpty()) {
				FIELDS.clear();
				TRACKERS.clear();
			}
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || mc.isPaused())
			return;

		LIVE.clear();
		for (DomainUVEntity domain : DomainRegistry.voidsIn(level)) {
			if (!domain.isAlive() || domain.getPhase() == DomainPhase.COLLAPSING)
				continue;
			DomainSphere sphere = domain.sphere();
			if (!sphere.isUsable())
				continue;
			LIVE.add(domain.getUUID());
			RippleField field = FIELDS.computeIfAbsent(domain.getUUID(), id -> new RippleField());
			field.prune(domain.tickCount);
			double floorY = sphere.floorY();

			for (Entity entity : level.getEntities(domain, sphere.bounds(), e -> !(e instanceof DomainSource) && !e.isSpectator())) {
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (!sphere.contains(x, y, z))
					continue;
				Tracker t = TRACKERS.computeIfAbsent(entity, Tracker::new);
				boolean onGround = entity.onGround() && y <= floorY + 0.35;
				if (onGround) {
					if (!t.wasOnGround) {
						double drop = t.peakY - y;
						if (drop > LANDING_MIN_DROP)
							field.emit(x - sphere.center().x, z - sphere.center().z, domain.tickCount, (float) Math.min(2.0, 0.8 + drop * 0.3));
						t.walked = 0.0;
					} else {
						double ddx = x - t.lastX;
						double ddz = z - t.lastZ;
						t.walked += Math.sqrt(ddx * ddx + ddz * ddz);
						if (t.walked >= STRIDE) {
							t.walked -= STRIDE;
							field.emit(x - sphere.center().x, z - sphere.center().z, domain.tickCount, (float) (0.35 + 0.30 * entity.getBbWidth()));
						}
					}
					t.peakY = y;
				} else {
					t.peakY = Math.max(t.peakY, y);
				}
				t.wasOnGround = onGround;
				t.lastX = x;
				t.lastZ = z;
			}
		}
		FIELDS.keySet().retainAll(LIVE);
	}
}
