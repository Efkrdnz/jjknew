package net.efkrdnz.jjkstrongest.domain;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Tracks the live domains in each level so nothing has to go looking for them.
 *
 * <p>The code this replaces answered "is there a domain here?" with entity AABB
 * scans — a 300-block box per slash candidate (up to eighty a tick), a 200-block box
 * on every NPC AI tick, and a &plusmn;30000 &times; &plusmn;512 sweep of the whole world on
 * every melee hit. There are only ever a handful of domains, so a list per level
 * answers the same questions for free.
 *
 * <p>Keyed on the {@link Level} instance rather than its dimension key, because the
 * client and the integrated server each have their own {@code Level} object for the
 * same dimension and their domains must not be mixed together. Entries are weak so a
 * level that gets unloaded does not pin its entities in memory.
 */
@EventBusSubscriber(modid = "jjk_strongest")
public final class DomainRegistry {

	private static final Map<Level, List<DomainUVEntity>> VOIDS = Collections.synchronizedMap(new WeakHashMap<>());
	private static final Map<Level, List<MalevolentShrineEntity>> SHRINES = Collections.synchronizedMap(new WeakHashMap<>());

	/**
	 * Number of live Unlimited Void domains across every level.
	 *
	 * <p>Read once per entity movement by the collision hook, so it is deliberately a
	 * plain volatile field: when nobody has a domain open — which is nearly always —
	 * the whole hook costs one field read and a branch.
	 */
	public static volatile int activeCount = 0;

	private DomainRegistry() {
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof DomainUVEntity domain) {
			synchronized (VOIDS) {
				List<DomainUVEntity> list = VOIDS.computeIfAbsent(event.getLevel(), l -> new ArrayList<>(2));
				if (!list.contains(domain))
					list.add(domain);
			}
			recount();
		} else if (entity instanceof MalevolentShrineEntity shrine) {
			synchronized (SHRINES) {
				List<MalevolentShrineEntity> list = SHRINES.computeIfAbsent(event.getLevel(), l -> new ArrayList<>(2));
				if (!list.contains(shrine))
					list.add(shrine);
			}
		}
	}

	@SubscribeEvent
	public static void onLeave(EntityLeaveLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof DomainUVEntity domain) {
			synchronized (VOIDS) {
				List<DomainUVEntity> list = VOIDS.get(event.getLevel());
				if (list != null)
					list.remove(domain);
			}
			recount();
		} else if (entity instanceof MalevolentShrineEntity shrine) {
			synchronized (SHRINES) {
				List<MalevolentShrineEntity> list = SHRINES.get(event.getLevel());
				if (list != null)
					list.remove(shrine);
			}
		}
	}

	/**
	 * A carve whose domain never got the chance to put it back — a crash, a hard
	 * stop — is repaired when the level comes back.
	 */
	@SubscribeEvent
	public static void onLevelLoad(LevelEvent.Load event) {
		if (event.getLevel() instanceof ServerLevel serverLevel)
			DomainCarve.restoreOrphans(serverLevel);
	}

	private static void recount() {
		int total = 0;
		synchronized (VOIDS) {
			for (List<DomainUVEntity> list : VOIDS.values())
				total += list.size();
		}
		activeCount = total;
	}

	// ---- queries -------------------------------------------------------------

	/** Live Unlimited Void domains in this level. Returns a snapshot safe to iterate. */
	public static List<DomainUVEntity> voidsIn(Level level) {
		synchronized (VOIDS) {
			List<DomainUVEntity> list = VOIDS.get(level);
			if (list == null || list.isEmpty())
				return Collections.emptyList();
			return new ArrayList<>(list);
		}
	}

	/** Live Malevolent Shrines in this level. Returns a snapshot safe to iterate. */
	public static List<MalevolentShrineEntity> shrinesIn(Level level) {
		synchronized (SHRINES) {
			List<MalevolentShrineEntity> list = SHRINES.get(level);
			if (list == null || list.isEmpty())
				return Collections.emptyList();
			return new ArrayList<>(list);
		}
	}

	/** The sphere of the first domain whose interior contains this point, or null. */
	public static DomainSphere sphereAt(Level level, double x, double y, double z) {
		if (activeCount == 0)
			return null;
		for (DomainUVEntity domain : voidsIn(level)) {
			if (!domain.isAlive())
				continue;
			DomainSphere sphere = domain.sphere();
			if (sphere.isUsable() && sphere.contains(x, y, z))
				return sphere;
		}
		return null;
	}

	/**
	 * Whether a point sits inside any Unlimited Void barrier.
	 *
	 * <p>Replaces two hand-rolled copies of this test that each did their own
	 * world scan — {@code DomainClashManagerProcedure.isPosInsideUV} and
	 * {@code ShrineScreenshakeProcedure.isPlayerInsideUV}.
	 */
	public static boolean isInside(Level level, double x, double y, double z) {
		return sphereAt(level, x, y, z) != null;
	}

	public static DomainUVEntity voidByOwner(Level level, String ownerUUID) {
		if (ownerUUID == null || ownerUUID.isEmpty())
			return null;
		for (DomainUVEntity domain : voidsIn(level)) {
			if (domain.isAlive() && ownerUUID.equals(domain.getPersistentData().getString("ownerUUID")))
				return domain;
		}
		return null;
	}

	public static DomainUVEntity voidByOwner(Level level, UUID ownerUUID) {
		return ownerUUID == null ? null : voidByOwner(level, ownerUUID.toString());
	}

	public static MalevolentShrineEntity shrineByOwner(Level level, String ownerUUID) {
		if (ownerUUID == null || ownerUUID.isEmpty())
			return null;
		for (MalevolentShrineEntity shrine : shrinesIn(level)) {
			if (shrine.isAlive() && ownerUUID.equals(shrine.getPersistentData().getString("ownerUUID")))
				return shrine;
		}
		return null;
	}

	/** Whether this entity already has a domain of either kind open. */
	public static boolean hasDomain(Level level, String ownerUUID) {
		return voidByOwner(level, ownerUUID) != null || shrineByOwner(level, ownerUUID) != null;
	}

	/** The nearest live domain to a point, whatever its phase. Used by the renderer and fog. */
	public static DomainUVEntity nearestVoid(Level level, double x, double y, double z, double maxDistance) {
		DomainUVEntity best = null;
		double bestSq = maxDistance * maxDistance;
		for (DomainUVEntity domain : voidsIn(level)) {
			if (!domain.isAlive())
				continue;
			double distSq = domain.position().distanceToSqr(x, y, z);
			if (distSq <= bestSq) {
				bestSq = distSq;
				best = domain;
			}
		}
		return best;
	}
}
