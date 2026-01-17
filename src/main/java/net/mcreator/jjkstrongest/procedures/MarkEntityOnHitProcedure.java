package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

public class MarkEntityOnHitProcedure {
	public static void execute(LevelAccessor world, Entity attacker, Entity target, Entity immediatesourceentity) {
		if (attacker == null || target == null || !(attacker instanceof Player))
			return;
		Player player = (Player) attacker;
		// get player variables using MCreator capability system
		String currentTechnique = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer;
		boolean blueToggle = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).blue_fist_toggle;
		boolean cleaveToggle = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).cleave_melee_toggle;
		//System.out.println("DEBUG: current_technique = " + currentTechnique);
		//System.out.println("DEBUG: blue_fist_toggle = " + blueToggle);
		//System.out.println("DEBUG: cleave_melee_toggle = " + cleaveToggle);
		boolean shouldMark = false;
		int maxMarks = 0;
		if (!(immediatesourceentity == null) && immediatesourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("technique")))) {
			return;
		}
		if (currentTechnique.equals("gojo") && blueToggle) {
			shouldMark = true;
			maxMarks = 5;
			//System.out.println("DEBUG: Should mark (Gojo mode)");
		} else if (currentTechnique.equals("sukuna") && cleaveToggle) {
			shouldMark = true;
			maxMarks = 3;
			//System.out.println("DEBUG: Should mark (Sukuna mode)");
		}
		if (target == attacker) {
			return;
		}
		if (!shouldMark) {
			//System.out.println("DEBUG: Not marking - conditions not met");
			return;
		}
		// get current marks from player variables
		String markedJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_entities;
		String timestampJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_timestamps;
		JsonArray markedArray = (markedJson == null || markedJson.isEmpty()) ? new JsonArray() : JsonParser.parseString(markedJson).getAsJsonArray();
		JsonArray timestampArray = (timestampJson == null || timestampJson.isEmpty()) ? new JsonArray() : JsonParser.parseString(timestampJson).getAsJsonArray();
		String targetUUID = target.getStringUUID();
		// check if already marked
		boolean alreadyMarked = false;
		for (JsonElement element : markedArray) {
			if (element.getAsString().equals(targetUUID)) {
				alreadyMarked = true;
				break;
			}
		}
		if (alreadyMarked) {
			//System.out.println("DEBUG: Target already marked");
			return;
		}
		// remove oldest if at limit
		if (markedArray.size() >= maxMarks) {
			markedArray.remove(0);
			timestampArray.remove(0);
			//System.out.println("DEBUG: Removed oldest mark (at limit)");
		}
		// add new mark
		markedArray.add(targetUUID);
		timestampArray.add(System.currentTimeMillis());
		// save back using MCreator capability system
		player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			capability.marked_entities = markedArray.toString();
			capability.marked_timestamps = timestampArray.toString();
			capability.syncPlayerVariables(player);
		});
		//System.out.println("DEBUG: Marked entity! UUID: " + targetUUID);
		//System.out.println("DEBUG: Total marks: " + markedArray.size());
		// visual feedback
		if (player instanceof ServerPlayer) {
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eTarget Marked!"), true);
		}
	}
}
