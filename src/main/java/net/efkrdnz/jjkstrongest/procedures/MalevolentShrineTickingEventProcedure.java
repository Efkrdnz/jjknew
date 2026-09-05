package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class MalevolentShrineTickingEventProcedure {
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof MalevolentShrineEntity) {
			// Lifecycle first, then the carve: the carve reads the radius the phase machine
			// sets, so running it first would always work a tick behind.
			MalevolentShrineTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
			MalevolentShrineTickBlockBreakingProcedure.execute(world, x, y, z, entity);
		}
	}
}
