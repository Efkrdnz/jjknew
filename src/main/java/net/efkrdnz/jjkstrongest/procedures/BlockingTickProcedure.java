package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

import javax.annotation.Nullable;

@EventBusSubscriber
public class BlockingTickProcedure {
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
		Entity ent = null;
		ent = entity;
		// get guard variable
		boolean isGuarding = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).block;
		// get current animation state
		String currentAnim = entity.getPersistentData().getString("current_arm_animation");
		boolean isPlaying = entity.getPersistentData().getBoolean("arm_anim_playing");
		// if guard is true AND (no animation OR animation finished), start holding
		if (isGuarding && currentAnim.isEmpty()) {
			PlayArmAnimationProcedure.execute(entity, "blocking", true);
		}
	}
}
