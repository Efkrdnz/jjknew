package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class WCSProgressionReturnProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).wcs_chant_progress >= 1) {
			return true;
		}
		return false;
	}
}
