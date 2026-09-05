package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import javax.annotation.Nullable;


@EventBusSubscriber
public class ShrineScreenshakeProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (world.getLevelData().getGameTime() % 10 != 0)
			return;
		if (!(entity instanceof Player) || !(world instanceof Level level))
			return;
		// Two 200-block entity scans per player, every ten ticks, for something the
		// registry already has in a list.
		MalevolentShrineEntity nearestShrine = null;
		double bestSq = 100.0 * 100.0;
		for (MalevolentShrineEntity shrine : DomainRegistry.shrinesIn(level)) {
			double distSq = shrine.distanceToSqr(x, y, z);
			if (distSq <= bestSq) {
				bestSq = distSq;
				nearestShrine = shrine;
			}
		}
		if (nearestShrine == null)
			return;
		// Read off synced entity data, not off an "active" flag in persistent data. That
		// flag never crossed to the client; this ran client-side and only saw it because
		// another procedure was re-deriving the same counter on this side in parallel.
		if (nearestShrine.phase() != DomainPhase.ACTIVE)
			return;
		// suppress screenshake during clash if player is inside UV barrier
		if (nearestShrine.isClashing() && DomainRegistry.isInside(level, x, y, z))
			return;
		String ownerUUID = nearestShrine.domainOwnerUUID();
		boolean isOwner = entity.getStringUUID().equals(ownerUUID);
		if (isOwner) {
			TriggerScreenShakeProcedure.execute(level, entity, 20, 1.0f);
		} else {
			TriggerScreenShakeProcedure.execute(level, entity, 20, 4.0f);
		}
	}

}
