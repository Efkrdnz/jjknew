package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

public class WCSProgressionReturn2Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_chant_progress == 2) {
			return true;
		}
		return false;
	}
}
