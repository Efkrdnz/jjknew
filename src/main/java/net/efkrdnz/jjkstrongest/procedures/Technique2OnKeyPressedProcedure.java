package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class Technique2OnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			boolean _setval = false;
			{
				JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				capability.infinity_crush = _setval;
				capability.syncPlayerVariables(entity);
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).contains("melee")) {
			MeleeUppercutProcedure.execute(entity.level(), entity);
			ReleaseArmAnimationProcedure.execute(entity);
			{
				boolean _setval = !(entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).left;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.left = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("reverse_cursed_technique")) {
			{
				boolean _setval = false;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.rct_self = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_dismantle")) {
			ReleaseArmAnimationProcedure.execute(entity);
			{
				boolean _setval = false;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.dismantle_barrage = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_cleave")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).cleave_melee_toggle) {
				{
					boolean _setval = false;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.cleave_melee_toggle = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("Cleave disabled!"), true);
			} else if (!(entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).cleave_melee_toggle) {
				{
					boolean _setval = true;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.cleave_melee_toggle = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("Cleave enabled!"), true);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
			entity.getPersistentData().putString("chanting", "");
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_red")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).red_flight) {
				{
					boolean _setval = false;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.red_flight = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("Flight disabled!"), true);
			} else if (!(entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).red_flight) {
				{
					boolean _setval = true;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.red_flight = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("Flight enabled!"), true);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_blue")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).blue_fist_toggle) {
				{
					boolean _setval = false;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.blue_fist_toggle = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("Blue Fist disabled!"), true);
			} else if (!(entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).blue_fist_toggle) {
				{
					boolean _setval = true;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.blue_fist_toggle = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("Blue Fist enabled!"), true);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("yuji_divergentfist")) {
			BlackFlashQTEMasterProcedure.onKeyRelease(entity);
		}
	}
}
