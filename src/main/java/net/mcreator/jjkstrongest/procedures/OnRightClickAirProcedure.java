package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class OnRightClickAirProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof Player))
			return;
		Player player = (Player) entity;
		// check if player has any marks
		String markedJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_entities;
		if (markedJson == null || markedJson.isEmpty())
			return;
		try {
			JsonArray markedArray = JsonParser.parseString(markedJson).getAsJsonArray();
			if (markedArray.size() > 0) {
				ExecuteMarkAbilityProcedure.execute(world, entity);
			}
		} catch (Exception e) {
			// ignore
		}
	}
}
