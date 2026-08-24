package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;

public class SorcererSukunaProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		try {
			for (Entity entityiterator : EntityArgument.getEntities(arguments, "target")) {
				{
					String _setval = "sukuna";
					{
						JjkStrongestModVariables.PlayerVariables capability = entityiterator.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.sorcerer = _setval;
						capability.syncPlayerVariables(entityiterator);
					}
				}
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
	}
}
