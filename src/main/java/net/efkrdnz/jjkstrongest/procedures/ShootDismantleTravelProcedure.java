package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.entity.DismantleTravelEntity;
import net.efkrdnz.jjkstrongest.Freezeframe;

public class ShootDismantleTravelProcedure {
	public static void execute(Level world, Entity entity, double techniquePower, double output, boolean breakBlocks) {
		if (world == null || entity == null)
			return;
		if (!(entity instanceof LivingEntity shooter))
			return;
		int chargeTicks = (int) shooter.getPersistentData().getDouble("ChantCounter");
		double tp = shooter.getPersistentData().getDouble("TechniquePower");
		if (tp <= 0)
			tp = techniquePower;
		if (tp <= 0)
			tp = 1.0;
		double out = output;
		if (out <= 0)
			out = ReturnOutputDismantleProcedure.execute(world, shooter) + 1.0;
		if (out <= 0)
			out = 1.0;
		double dmg = 8.0 * tp * out;
		// Shift = vertical only
		// Normal = horizontal or diagonal (random)
		int slashMode = shooter.isShiftKeyDown() ? 1 : 4;
		boolean diagonalFlip = false;
		if (tp == 2) {
			if (world.isClientSide()) {
				TriggerChargedImpactProcedure.execute((Level) world, entity, 3, 1.0f, 2.0f, 2.5f);
				// FREEZE FRAME on client after 2 ticks (100ms)
				net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
				new java.util.Timer().schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						// execute on main thread
						mc.execute(() -> {
							Freezeframe.execute(75);
						});
					}
				}, 100); // 100ms = ~2 ticks
				TriggerScreenShakeProcedure.execute((Level) world, entity, 10, 3.0f);
			}
		}
		DismantleTravelEntity.shoot(world, shooter, world.random, 1.0f, tp, out, dmg, breakBlocks, slashMode, diagonalFlip, chargeTicks);
	}
}
