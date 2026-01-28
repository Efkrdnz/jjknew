package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

public class Technique2OnKeyPressProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putDouble("TechniquePower", 0);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_melee")
				|| ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_melee")) {
			MeleeUppercutProcedure.execute(entity.level(), entity);
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_limitless")) {
			{
				boolean _setval = true;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.infinity_crush = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_dismantle")) {
			{
				boolean _setval = true;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.dismantle_barrage = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			PlayArmAnimationProcedure.execute(entity, "dismantle", true);
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_wcs")) {
			entity.getPersistentData().putString("chanting", "wcs2");
		}
	}
}
