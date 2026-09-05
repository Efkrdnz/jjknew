package net.efkrdnz.jjkstrongest.domain;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

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

	/**
	 * Every live domain in each level, whatever technique it came from.
	 *
	 * <p>This used to be two maps — one of {@code DomainUVEntity}, one of
	 * {@code MalevolentShrineEntity} — with a parallel query for each. That shape grew a
	 * branch, a map and a pair of accessors per new domain; a third technique meant
	 * editing this file. One list keyed on {@link DomainSource} does not: an entity that
	 * implements the interface registers itself, and {@link #closedIn} and {@link #openIn}
	 * answer the questions the engine actually asks.
	 *
	 * <p>Keyed on the {@link Level} instance rather than its dimension key, because the
	 * client and the integrated server each have their own {@code Level} object for the
	 * same dimension and their domains must not be mixed together. Entries are weak so a
	 * level that gets unloaded does not pin its entities in memory.
	 */
	private static final Map<Level, List<DomainSource>> DOMAINS = Collections.synchronizedMap(new WeakHashMap<>());

	/**
	 * Number of live closed domains across every level.
	 *
	 * <p>Read once per entity movement by the collision hook, so it is deliberately a
	 * plain volatile field: when nobody has a domain open — which is nearly always —
	 * the whole hook costs one field read and a branch. Open domains are not counted:
	 * they have no barrier, so they never take part in collision.
	 */
	public static volatile int activeCount = 0;

	private DomainRegistry() {
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof DomainSource source))
			return;
		synchronized (DOMAINS) {
			List<DomainSource> list = DOMAINS.computeIfAbsent(event.getLevel(), l -> new ArrayList<>(2));
			if (!list.contains(source))
				list.add(source);
		}
		recount();
	}

	@SubscribeEvent
	public static void onLeave(EntityLeaveLevelEvent event) {
		if (!(event.getEntity() instanceof DomainSource source))
			return;
		synchronized (DOMAINS) {
			List<DomainSource> list = DOMAINS.get(event.getLevel());
			if (list != null)
				list.remove(source);
		}
		recount();
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
		synchronized (DOMAINS) {
			for (List<DomainSource> list : DOMAINS.values()) {
				for (DomainSource source : list) {
					if (source.isClosed())
						total++;
				}
			}
		}
		activeCount = total;
	}

	// ---- queries -------------------------------------------------------------

	/** Every live domain in this level. Returns a snapshot safe to iterate. */
	public static List<DomainSource> domainsIn(Level level) {
		synchronized (DOMAINS) {
			List<DomainSource> list = DOMAINS.get(level);
			if (list == null || list.isEmpty())
				return Collections.emptyList();
			return new ArrayList<>(list);
		}
	}

	/** The domains in this level with a barrier: the ones you can be inside of. */
	public static List<DomainSource> closedIn(Level level) {
		return filter(level, true);
	}

	/** The domains in this level with no surface: the ones that only cover ground. */
	public static List<DomainSource> openIn(Level level) {
		return filter(level, false);
	}

	private static List<DomainSource> filter(Level level, boolean closed) {
		List<DomainSource> all = domainsIn(level);
		if (all.isEmpty())
			return Collections.emptyList();
		List<DomainSource> out = new ArrayList<>(all.size());
		for (DomainSource source : all) {
			if (source.isClosed() == closed)
				out.add(source);
		}
		return out;
	}

	/**
	 * Live Unlimited Void domains in this level.
	 *
	 * <p>A typed view over the one list, for the code that genuinely needs the concrete
	 * entity — the renderer, the fog, the phase machine. Engine code that only needs "a
	 * domain with a barrier" should ask {@link #closedIn} instead.
	 */
	public static List<DomainUVEntity> voidsIn(Level level) {
		return typed(level, DomainUVEntity.class);
	}

	/** Live Malevolent Shrines in this level. As {@link #voidsIn}, for the open domain. */
	public static List<MalevolentShrineEntity> shrinesIn(Level level) {
		return typed(level, MalevolentShrineEntity.class);
	}

	private static <T extends DomainSource> List<T> typed(Level level, Class<T> type) {
		List<DomainSource> all = domainsIn(level);
		if (all.isEmpty())
			return Collections.emptyList();
		List<T> out = new ArrayList<>(all.size());
		for (DomainSource source : all) {
			if (type.isInstance(source))
				out.add(type.cast(source));
		}
		return out;
	}

	/** The sphere of the first barriered domain whose interior contains this point, or null. */
	public static DomainSphere sphereAt(Level level, double x, double y, double z) {
		if (activeCount == 0)
			return null;
		for (DomainSource source : closedIn(level)) {
			if (!source.isAlive())
				continue;
			DomainSphere sphere = source.volume();
			if (sphere.isUsable() && sphere.contains(x, y, z))
				return sphere;
		}
		return null;
	}

	/**
	 * Whether a point sits inside any closed domain's barrier.
	 *
	 * <p>Replaces two hand-rolled copies of this test that each did their own world scan,
	 * one of them re-running for every slash candidate the shrine considered.
	 */
	public static boolean isInside(Level level, double x, double y, double z) {
		return sphereAt(level, x, y, z) != null;
	}

	/** The domain of either kind cast by this player, or null. */
	public static DomainSource byOwner(Level level, String ownerUUID) {
		if (ownerUUID == null || ownerUUID.isEmpty())
			return null;
		for (DomainSource source : domainsIn(level)) {
			if (source.isAlive() && ownerUUID.equals(source.domainOwnerUUID()))
				return source;
		}
		return null;
	}

	public static DomainUVEntity voidByOwner(Level level, String ownerUUID) {
		return ownerOf(voidsIn(level), ownerUUID);
	}

	public static DomainUVEntity voidByOwner(Level level, UUID ownerUUID) {
		return ownerUUID == null ? null : voidByOwner(level, ownerUUID.toString());
	}

	public static MalevolentShrineEntity shrineByOwner(Level level, String ownerUUID) {
		return ownerOf(shrinesIn(level), ownerUUID);
	}

	/**
	 * Searches a typed list rather than filtering {@link #byOwner}'s result, so a player
	 * who somehow held both kinds at once would still be found by either query rather than
	 * by whichever the one list happened to hold first.
	 */
	private static <T extends DomainSource> T ownerOf(List<T> candidates, String ownerUUID) {
		if (ownerUUID == null || ownerUUID.isEmpty())
			return null;
		for (T source : candidates) {
			if (source.isAlive() && ownerUUID.equals(source.domainOwnerUUID()))
				return source;
		}
		return null;
	}

	/** Whether this entity already has a domain of either kind open. */
	public static boolean hasDomain(Level level, String ownerUUID) {
		return byOwner(level, ownerUUID) != null;
	}

	/** The nearest live Void to a point, whatever its phase. Used by the renderer and fog. */
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
