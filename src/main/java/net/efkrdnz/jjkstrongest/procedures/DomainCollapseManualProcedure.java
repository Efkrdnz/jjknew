package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

/**
 * Casting a domain a second time closes the one you have open.
 *
 * <p>These used to be 200-block entity scans over two classes, run on every cast and
 * on every NPC AI tick. They are registry lookups now.
 */
public class DomainCollapseManualProcedure {

	/** Whether this entity already has a domain of either kind open. */
	public static boolean hasActiveDomain(LevelAccessor world, Entity entity) {
		if (!(world instanceof Level level) || entity == null)
			return false;
		return DomainRegistry.hasDomain(level, entity.getStringUUID());
	}

	/** Closes whichever domain this entity owns. */
	public static void collapsePlayerDomain(LevelAccessor world, Entity entity) {
		if (!(world instanceof Level level) || entity == null)
			return;
		String owner = entity.getStringUUID();

		DomainUVEntity domain = DomainRegistry.voidByOwner(level, owner);
		if (domain != null) {
			domain.getPersistentData().putInt("duration", 0);
			DomainUVEntityTickProcedure.beginCollapse(domain);
			return;
		}
		MalevolentShrineEntity shrine = DomainRegistry.shrineByOwner(level, owner);
		if (shrine != null)
			shrine.getPersistentData().putInt("domainLifetimeTicks", 600);
	}

	/** Closes the nearest domain to a position, whoever owns it. */
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!(world instanceof Level level))
			return;
		DomainUVEntity domain = DomainRegistry.nearestVoid(level, x, y, z, 50.0);
		if (domain != null) {
			domain.getPersistentData().putInt("duration", 0);
			DomainUVEntityTickProcedure.beginCollapse(domain);
			return;
		}
		Vec3 point = new Vec3(x, y, z);
		MalevolentShrineEntity nearest = null;
		double bestSq = 50.0 * 50.0;
		for (MalevolentShrineEntity shrine : DomainRegistry.shrinesIn(level)) {
			double distSq = shrine.position().distanceToSqr(point);
			if (distSq <= bestSq) {
				bestSq = distSq;
				nearest = shrine;
			}
		}
		if (nearest != null)
			nearest.getPersistentData().putInt("domainLifetimeTicks", 600);
	}
}
