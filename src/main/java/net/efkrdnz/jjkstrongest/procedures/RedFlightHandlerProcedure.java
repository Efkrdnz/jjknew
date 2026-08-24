package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

@EventBusSubscriber
public class RedFlightHandlerProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(Event event, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player player && !player.level().isClientSide()) {
			boolean redFlight = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).red_flight;
			if (redFlight && !player.isCreative() && !player.isSpectator()) {
				if (!player.getAbilities().mayfly) {
					player.getAbilities().mayfly = true;
					player.onUpdateAbilities();
				}
			} else if (!redFlight && !player.isCreative() && !player.isSpectator()) {
				if (player.getAbilities().mayfly) {
					player.getAbilities().mayfly = false;
					player.getAbilities().flying = false;
					player.onUpdateAbilities();
				}
			}
		}
	}
}
