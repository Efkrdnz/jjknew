package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

import javax.annotation.Nullable;

@EventBusSubscriber
public class DomainEffectTickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).domain_image_1 > 0) {
			{
				double _setval = Math.min(1, Math.max((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).domain_image_1, 0)) - 0.025;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.domain_image_1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
		if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).domain_image_2 > 0) {
			{
				double _setval = Math.min(1, Math.max((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).domain_image_2, 0)) - 0.05;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.domain_image_2 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
	}
}
