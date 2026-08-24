package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.DismantleTravelEntity;

public class BarrageProjectileSpamProcedure {
	public static void execute(Level world, Entity entity) {
		if (world == null || entity == null)
			return;
		if (!(entity instanceof LivingEntity shooter))
			return;
		double outputMultiplier = ReturnOutputDismantleProcedure.execute(world, entity);
		double out = outputMultiplier + 1.0;
		if (out <= 0)
			out = 1.0;
		double tp = 1.6;
		double baseDamage = 6.0;
		double dmg = baseDamage * tp * out;
		boolean breakBlocks = true;
		int slashMode = shooter.isShiftKeyDown() ? 1 : 4;
		boolean diagonalFlip = false;
		int chargeTicks = 0;
		DismantleTravelEntity.shoot(world, shooter, world.random, 1.0f, tp, out, dmg, breakBlocks, slashMode, diagonalFlip, chargeTicks);
	}
}
