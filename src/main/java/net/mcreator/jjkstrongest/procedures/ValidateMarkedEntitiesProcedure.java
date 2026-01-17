package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import java.util.UUID;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class ValidateMarkedEntitiesProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || !(entity instanceof Player) || !(world instanceof ServerLevel))
			return;
		Player player = (Player) entity;
		ServerLevel serverLevel = (ServerLevel) world;
		String sorcerer = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer;
		String markedJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_entities;
		String timestampJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_timestamps;
		// check if markedJson is null or empty - if so, skip validation
		if (markedJson == null || markedJson.isEmpty())
			return;
		// determine limits based on technique
		long maxDuration = 0;
		double maxDistance = 0;
		if (sorcerer.equals("gojo")) {
			maxDuration = 60000; // 60 seconds
			maxDistance = 100.0;
		} else if (sorcerer.equals("sukuna")) {
			maxDuration = 5000; // 5 seconds
			maxDistance = 25.0;
		} else {
			return;
		}
		JsonArray markedArray;
		JsonArray timestampArray;
		try {
			markedArray = JsonParser.parseString(markedJson).getAsJsonArray();
			timestampArray = JsonParser.parseString(timestampJson).getAsJsonArray();
		} catch (Exception e) {
			// if parsing fails, initialize empty arrays and save them
			player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.marked_entities = "[]";
				capability.marked_timestamps = "[]";
				capability.syncPlayerVariables(player);
			});
			return;
		}
		JsonArray newMarkedArray = new JsonArray();
		JsonArray newTimestampArray = new JsonArray();
		long currentTime = System.currentTimeMillis();
		// validate each mark
		for (int i = 0; i < markedArray.size(); i++) {
			String uuidString = markedArray.get(i).getAsString();
			long markTime = timestampArray.get(i).getAsLong();
			boolean valid = true;
			// check time expiration
			if (currentTime - markTime > maxDuration) {
				valid = false;
			}
			// check distance
			if (valid) {
				try {
					UUID targetUUID = UUID.fromString(uuidString);
					Entity target = serverLevel.getEntity(targetUUID);
					if (target == null || !target.isAlive()) {
						valid = false;
					} else {
						double distance = player.distanceTo(target);
						if (distance > maxDistance) {
							valid = false;
						}
					}
				} catch (Exception e) {
					valid = false;
				}
			}
			if (valid) {
				newMarkedArray.add(uuidString);
				newTimestampArray.add(markTime);
			}
		}
		// save cleaned arrays
		player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			capability.marked_entities = newMarkedArray.toString();
			capability.marked_timestamps = newTimestampArray.toString();
			capability.syncPlayerVariables(player);
		});
	}
}
