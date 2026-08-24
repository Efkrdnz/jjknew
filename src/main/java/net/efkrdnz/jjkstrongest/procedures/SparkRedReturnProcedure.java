package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class SparkRedReturnProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("gojo")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red >= 1) {
				return true;
			}
		}
		return false;
	}
}
