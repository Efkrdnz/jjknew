package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class Technique1OnKeyPressProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		// Keep a charge that is already building. Holding the key cannot reach here
		// twice, so this only ever protects a chant raised another way -- a spoken
		// one, whose whole point is that the charge survives until it is released.
		if (entity.getPersistentData().getString("chanting").isEmpty())
			entity.getPersistentData().putDouble("TechniquePower", 1);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("all_generic")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).CE_FLOW) {
				{
					boolean _setval = false;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.CE_FLOW = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
			} else {
				{
					boolean _setval = true;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.CE_FLOW = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_blue")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue < 3) {
				{
					double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue + 1;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.charge_blue = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_red")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red < 3) {
				{
					double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red + 1;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.charge_red = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_purple")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple < 3) {
				if (entity.isShiftKeyDown()) {
					while ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue > 0
							&& (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red > 0
							&& (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple < 3) {
						{
							double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red - 1;
							{
								JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
								capability.charge_red = _setval;
								capability.syncPlayerVariables(entity);
							}
						}
						{
							double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue - 1;
							{
								JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
								capability.charge_blue = _setval;
								capability.syncPlayerVariables(entity);
							}
						}
						{
							double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple + 1;
							{
								JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
								capability.charge_purple = _setval;
								capability.syncPlayerVariables(entity);
							}
						}
					}
				} else {
					if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue > 0
							&& (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red > 0
							&& (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple < 3) {
						{
							double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red - 1;
							{
								JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
								capability.charge_red = _setval;
								capability.syncPlayerVariables(entity);
							}
						}
						{
							double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue - 1;
							{
								JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
								capability.charge_blue = _setval;
								capability.syncPlayerVariables(entity);
							}
						}
						{
							double _setval = (entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple + 1;
							{
								JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
								capability.charge_purple = _setval;
								capability.syncPlayerVariables(entity);
							}
						}
					}
				}
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).contains("melee")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).left) {
				PlayArmAnimationProcedure.execute(entity, "jab_left", true);
			} else {
				PlayArmAnimationProcedure.execute(entity, "jab_right", true);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_dismantle")) {
			entity.getPersistentData().putString("chanting", "dismantle");
			PlayArmAnimationProcedure.execute(entity, "dismantle", true);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_cleave")) {
			entity.getPersistentData().putString("chanting", "cleave");
			PlayArmAnimationProcedure.execute(entity, "cleave", true);
			CleaveHoldStartProcedure.execute(entity.level(), entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
			entity.getPersistentData().putString("chanting", "wcs1");
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_control")) {
			SpeechExecuteStopProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_assault")) {
			SpeechExecuteBlastProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_binding")) {
			SpeechExecuteTwistProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_utility")) {
			SpeechExecuteFallProcedure.execute(entity);
		}
	}
}
