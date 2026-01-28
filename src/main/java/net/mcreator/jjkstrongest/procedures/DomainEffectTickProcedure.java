package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class DomainEffectTickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player);
		}
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).domain_image_1 > 0) {
			{
				double _setval = Math.min(1, Math.max((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).domain_image_1, 0)) - 0.025;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.domain_image_1 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).domain_image_2 > 0) {
			{
				double _setval = Math.min(1, Math.max((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).domain_image_2, 0)) - 0.05;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.domain_image_2 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
	}
}
