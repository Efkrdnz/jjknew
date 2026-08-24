package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class Technique2OnKeyPressProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putDouble("TechniquePower", 0);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("reverse_cursed_technique")) {
			{
				boolean _setval = true;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.rct_self = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).contains("melee")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).left) {
				PlayArmAnimationProcedure.execute(entity, "uppercut_left", true);
			} else {
				PlayArmAnimationProcedure.execute(entity, "uppercut_right", true);
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_limitless")) {
			{
				boolean _setval = true;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.infinity_crush = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_dismantle")) {
			{
				boolean _setval = true;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.dismantle_barrage = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			PlayArmAnimationProcedure.execute(entity, "dismantle", true);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
			entity.getPersistentData().putString("chanting", "wcs2");
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("yuji_divergentfist")) {
			BlackFlashQTEMasterProcedure.onKeyPress(entity);
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_assault")) {
			SpeechExecuteBurstProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_control")) {
			SpeechExecuteSleepProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_binding")) {
			SpeechExecuteRotProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_utility")) {
			SpeechExecuteSpitProcedure.execute(entity);
		}
	}
}
