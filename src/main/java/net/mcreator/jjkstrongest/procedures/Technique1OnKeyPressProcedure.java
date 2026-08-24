package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

public class Technique1OnKeyPressProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putDouble("TechniquePower", 1);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("all_generic")) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).CE_FLOW) {
				{
					boolean _setval = false;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.CE_FLOW = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.CE_FLOW = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
			}
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_blue")) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue < 3) {
				{
					double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue + 1;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.charge_blue = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_red")) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red < 3) {
				{
					double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red + 1;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.charge_red = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_purple")) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple < 3) {
				if (entity.isShiftKeyDown()) {
					while ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue > 0
							&& (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red > 0
							&& (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple < 3) {
						{
							double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red - 1;
							entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
								capability.charge_red = _setval;
								capability.syncPlayerVariables(entity);
							});
						}
						{
							double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue - 1;
							entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
								capability.charge_blue = _setval;
								capability.syncPlayerVariables(entity);
							});
						}
						{
							double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple + 1;
							entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
								capability.charge_purple = _setval;
								capability.syncPlayerVariables(entity);
							});
						}
					}
				} else {
					if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue > 0
							&& (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red > 0
							&& (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple < 3) {
						{
							double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red - 1;
							entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
								capability.charge_red = _setval;
								capability.syncPlayerVariables(entity);
							});
						}
						{
							double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue - 1;
							entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
								capability.charge_blue = _setval;
								capability.syncPlayerVariables(entity);
							});
						}
						{
							double _setval = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple + 1;
							entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
								capability.charge_purple = _setval;
								capability.syncPlayerVariables(entity);
							});
						}
					}
				}
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).contains("melee")) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).left) {
				PlayArmAnimationProcedure.execute(entity, "jab_left", true);
			} else {
				PlayArmAnimationProcedure.execute(entity, "jab_right", true);
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_dismantle")) {
			entity.getPersistentData().putString("chanting", "dismantle");
			PlayArmAnimationProcedure.execute(entity, "dismantle", true);
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_cleave")) {
			entity.getPersistentData().putString("chanting", "cleave");
			PlayArmAnimationProcedure.execute(entity, "cleave", true);
			CleaveHoldStartProcedure.execute(entity.level(), entity);
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_wcs")) {
			entity.getPersistentData().putString("chanting", "wcs1");
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("inumaki_control")) {
			SpeechExecuteStopProcedure.execute(entity);
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("inumaki_assault")) {
			SpeechExecuteBlastProcedure.execute(entity);
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("inumaki_binding")) {
			SpeechExecuteTwistProcedure.execute(entity);
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("inumaki_utility")) {
			SpeechExecuteFallProcedure.execute(entity);
		}
	}
}
