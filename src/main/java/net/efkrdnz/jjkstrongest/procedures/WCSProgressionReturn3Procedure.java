package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class WCSProgressionReturn3Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).wcs_chant_progress == 3) {
			return true;
		}
		return false;
	}
}
