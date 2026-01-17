package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class TestCleaveDistortionProcedure {
	public static void execute(Level world, Entity entity) {
		if (entity == null || world == null) {
			System.out.println("[TEST] Entity or world is null");
			return;
		}
		System.out.println("[TEST] World isClientSide: " + world.isClientSide);
		if (!world.isClientSide) {
			// Send message on server side
			entity.sendSystemMessage(Component.literal("Testing cleave distortion (server side)"));
			return;
		}
		// Client side
		System.out.println("[TEST] Activating distortion state...");
		CleaveDistortionStateProcedure.INSTANCE.triggerRandom(20, 1.0f, 10);
		System.out.println("[TEST] State.active = " + CleaveDistortionStateProcedure.INSTANCE.active);
		System.out.println("[TEST] State.remainingTicks = " + CleaveDistortionStateProcedure.INSTANCE.remainingTicks);
		System.out.println("[TEST] State.slashCount = " + CleaveDistortionStateProcedure.INSTANCE.slashCount);
	}
}
