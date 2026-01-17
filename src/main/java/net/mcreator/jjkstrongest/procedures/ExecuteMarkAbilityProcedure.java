package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import java.util.UUID;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class ExecuteMarkAbilityProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || !(entity instanceof Player) || !(world instanceof ServerLevel))
			return;
		Player player = (Player) entity;
		ServerLevel serverLevel = (ServerLevel) world;
		String currentTechnique = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer;
		String markedJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_entities;
		if (markedJson == null || markedJson.isEmpty())
			return;
		JsonArray markedArray = JsonParser.parseString(markedJson).getAsJsonArray();
		if (markedArray.size() == 0)
			return;
		// get player look direction
		Vec3 lookVec = player.getLookAngle();
		Vec3 playerPos = player.getEyePosition();
		// find best marked target in view
		Entity bestTarget = null;
		double bestDot = -1.0; // cos(angle), higher = more centered
		double minDotThreshold = Math.cos(Math.toRadians(20)); // 20 degree tolerance
		for (int i = 0; i < markedArray.size(); i++) {
			String uuidString = markedArray.get(i).getAsString();
			try {
				UUID targetUUID = UUID.fromString(uuidString);
				Entity target = serverLevel.getEntity(targetUUID);
				if (target != null && target.isAlive()) {
					Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
					Vec3 toTarget = targetPos.subtract(playerPos).normalize();
					double dot = lookVec.dot(toTarget);
					if (dot > minDotThreshold && dot > bestDot) {
						bestDot = dot;
						bestTarget = target;
					}
				}
			} catch (Exception e) {
				// skip invalid uuid
			}
		}
		if (bestTarget == null)
			return;
		//System.out.println("DEBUG: Executing mark ability on target");
		// execute ability based on technique
		boolean isSneaking = player.isShiftKeyDown();
		if (currentTechnique.equals("sukuna")) {
			if (isSneaking) {
				SukunaMarkSlashBarrageProcedure.execute(world, player, bestTarget);
			} else {
				SukunaMarkSlashSingleProcedure.execute(world, player, bestTarget);
			}
		} else if (currentTechnique.equals("gojo")) {
			double distance = player.distanceTo(bestTarget);
			if (isSneaking || distance > 50) {
				GojoMarkTeleportProcedure.execute(world, player, bestTarget);
			} else {
				GojoMarkPullProcedure.execute(world, player, bestTarget);
			}
		}
		// remove the used mark
		String targetUUID = bestTarget.getStringUUID();
		JsonArray newMarkedArray = new JsonArray();
		String timestampJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_timestamps;
		JsonArray timestampArray = JsonParser.parseString(timestampJson).getAsJsonArray();
		JsonArray newTimestampArray = new JsonArray();
		for (int i = 0; i < markedArray.size(); i++) {
			if (!markedArray.get(i).getAsString().equals(targetUUID)) {
				newMarkedArray.add(markedArray.get(i));
				newTimestampArray.add(timestampArray.get(i));
			}
		}
		player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			capability.marked_entities = newMarkedArray.toString();
			capability.marked_timestamps = newTimestampArray.toString();
			capability.syncPlayerVariables(player);
		});
	}
}
