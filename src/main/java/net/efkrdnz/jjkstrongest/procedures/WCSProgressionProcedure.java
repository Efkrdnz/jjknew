package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class WCSProgressionProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).wcs_chant_progress < 3) {
			{
				double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).wcs_chant_progress + 1;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_chant_progress = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		} else {
			{
				double _setval = 0;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_chant_progress = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
	}
}
