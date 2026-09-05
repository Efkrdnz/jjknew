package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

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
		if (!(entity instanceof Player))
			return;
		AABB searchBox = AABB.ofSize(new Vec3(x, y, z), 200, 200, 200);
		if (world.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> true).isEmpty())
			return;
		// get nearest shrine
		MalevolentShrineEntity nearestShrine = (MalevolentShrineEntity) world.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> true).stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z))).orElse(null);
		if (nearestShrine == null)
			return;
		if (!nearestShrine.getPersistentData().getBoolean("active"))
			return;
		// suppress screenshake during clash if player is inside UV barrier
		if (nearestShrine.getPersistentData().getBoolean("isClashing")) {
			if (world instanceof Level level && DomainRegistry.isInside(level, x, y, z))
				return;
		}
		String ownerUUID = nearestShrine.getPersistentData().getString("ownerUUID");
		boolean isOwner = entity.getStringUUID().equals(ownerUUID);
		if (isOwner) {
			TriggerScreenShakeProcedure.execute((Level) world, entity, 20, 1.0f);
		} else {
			TriggerScreenShakeProcedure.execute((Level) world, entity, 20, 4.0f);
		}
	}

}
