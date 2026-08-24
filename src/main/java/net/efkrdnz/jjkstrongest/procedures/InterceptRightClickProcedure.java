package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

import java.util.UUID;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class InterceptRightClickProcedure {
	private static boolean checkAndExecuteMarkAbility(Player player) {
		if (player == null || player.level().isClientSide)
			return false;
		String markedJson = (player.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).marked_entities;
		if (markedJson == null || markedJson.isEmpty())
			return false;
		JsonArray markedArray;
		try {
			markedArray = JsonParser.parseString(markedJson).getAsJsonArray();
		} catch (Exception e) {
			return false;
		}
		if (markedArray.size() == 0)
			return false;
		// check if looking at marked entity
		Vec3 lookVec = player.getLookAngle();
		Vec3 playerPos = player.getEyePosition();
		double minDotThreshold = Math.cos(Math.toRadians(20));
		ServerLevel serverLevel = (ServerLevel) player.level();
		for (int i = 0; i < markedArray.size(); i++) {
			String uuidString = markedArray.get(i).getAsString();
			try {
				UUID targetUUID = UUID.fromString(uuidString);
				Entity target = serverLevel.getEntity(targetUUID);
				if (target != null && target.isAlive()) {
					Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
					Vec3 toTarget = targetPos.subtract(playerPos).normalize();
					double dot = lookVec.dot(toTarget);
					if (dot > minDotThreshold) {
						ExecuteMarkAbilityProcedure.execute(player.level(), player);
						return true;
					}
				}
			} catch (Exception e) {
				// skip
			}
		}
		return false;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (checkAndExecuteMarkAbility(event.getEntity())) {
			event.setCanceled(true);
			event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (checkAndExecuteMarkAbility(event.getEntity())) {
			event.setCanceled(true);
			event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (checkAndExecuteMarkAbility(event.getEntity())) {
			event.setCanceled(true);
			event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
		}
	}
}
