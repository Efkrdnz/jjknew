package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

public class HandleAbilitySelectionProcedure {
	public static void execute(Player player, double abilityId) {
		if (player == null)
			return;
		String moveset = "";
		String displayName = "";
		switch ((int) abilityId) {
			case 0 :
				moveset = "gojo_blue";
				displayName = "Blue";
				break;
			case 1 :
				moveset = "gojo_general";
				displayName = "General";
				break;
			case 2 :
				moveset = "gojo_red";
				displayName = "Red";
				break;
			case 3 :
				moveset = "gojo_purple";
				displayName = "Purple";
				break;
			case 4 :
				moveset = "gojo_melee";
				displayName = "Melee";
				break;
			// sukuna
			case 5 :
				moveset = "sukuna_cleave";
				displayName = "Cleave";
				break;
			case 6 :
				moveset = "sukuna_dismantle";
				displayName = "Dismantle";
				break;
			case 7 :
				moveset = "sukuna_fuga";
				displayName = "Fuga";
				break;
			case 8 :
				moveset = "sukuna_wcs";
				displayName = "World Slash";
			default :
				return;
		}
		String finalMoveset = moveset;
		String finalDisplayName = displayName;
		player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
			capability.current_moveset = finalMoveset;
			capability.syncPlayerVariables(player);
		});
		player.displayClientMessage(Component.literal("Selected: " + finalDisplayName), true);
	}
}
