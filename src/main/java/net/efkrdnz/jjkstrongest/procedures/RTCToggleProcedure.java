package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.BoolArgumentType;

public class RTCToggleProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		try {
			for (Entity entityiterator : EntityArgument.getEntities(arguments, "target")) {
				{
					boolean _setval = BoolArgumentType.getBool(arguments, "logic");
					{
						JjkStrongestModVariables.PlayerVariables capability = entityiterator.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.RTC_unlocked = _setval;
						capability.syncPlayerVariables(entityiterator);
					}
				}
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
	}
}
