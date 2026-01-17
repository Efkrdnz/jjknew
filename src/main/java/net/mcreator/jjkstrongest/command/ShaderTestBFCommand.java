package net.mcreator.jjkstrongest.command;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.Commands;

import net.mcreator.jjkstrongest.procedures.TriggerBlackFlashShaderProcedure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ShaderTestBFCommand {
	@SubscribeEvent
	public static void registerCommand(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("shader:test:blackflash")
				// with arguments
				.then(Commands.argument("duration", IntegerArgumentType.integer(1)).then(Commands.argument("intensity", DoubleArgumentType.doubleArg(0.0)).executes(arguments -> {
					Level world = arguments.getSource().getUnsidedLevel();
					Entity entity = arguments.getSource().getEntity();
					if (entity == null && world instanceof ServerLevel _servLevel)
						entity = FakePlayerFactory.getMinecraft(_servLevel);
					// extract the argument values
					int duration = IntegerArgumentType.getInteger(arguments, "duration");
					float intensity = (float) DoubleArgumentType.getDouble(arguments, "intensity");
					// call with custom values
					TriggerBlackFlashShaderProcedure.execute(world, entity, duration, intensity);
					return 0;
				})))
				// without arguments (default)
				.executes(arguments -> {
					Level world = arguments.getSource().getUnsidedLevel();
					Entity entity = arguments.getSource().getEntity();
					if (entity == null && world instanceof ServerLevel _servLevel)
						entity = FakePlayerFactory.getMinecraft(_servLevel);
					// call with default values
					TriggerBlackFlashShaderProcedure.execute(world, entity, 20, 1.0f);
					return 0;
				}));
	}
}
