package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class BlockingTickProcedure {
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
		Entity ent = null;
		ent = entity;
		// get guard variable
		boolean isGuarding = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).block;
		// get current animation state
		String currentAnim = entity.getPersistentData().getString("current_arm_animation");
		boolean isPlaying = entity.getPersistentData().getBoolean("arm_anim_playing");
		// if guard is true AND (no animation OR animation finished), start holding
		if (isGuarding && currentAnim.isEmpty()) {
			PlayArmAnimationProcedure.execute(entity, "blocking", true);
		}
	}
}
