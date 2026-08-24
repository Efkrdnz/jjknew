package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class ReturnMovesetTESTProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset;
	}
}
