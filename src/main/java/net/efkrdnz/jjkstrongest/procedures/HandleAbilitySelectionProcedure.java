package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

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
				break;
			// yuji
			case 9 :
				moveset = "yuji_bloodmanipulation";
				displayName = "Blood Manipulation";
				break;
			case 10 :
				moveset = "yuji_shrine";
				displayName = "Shrine";
				break;
			case 11 :
				moveset = "yuji_divergentfist";
				displayName = "Divergent Fist";
				break;
			case 12 :
				moveset = "yuji_melee";
				displayName = "Melee";
				break;
			default :
				return;
		}
		String finalMoveset = moveset;
		String finalDisplayName = displayName;
		{
			JjkStrongestModVariables.PlayerVariables capability = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
			capability.current_moveset = finalMoveset;
			capability.syncPlayerVariables(player);
		}
		player.displayClientMessage(Component.literal("Selected: " + finalDisplayName), true);
	}
}
