package net.mcreator.jjkstrongest.procedures;

import org.joml.Vector3f;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.DustParticleOptions;

public class LapseBlueParticlesProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		// spawns moving dust particles sucked toward center
		double radius = entity.getPersistentData().getDouble("TechniquePower") * 5;
		for (int i = 0; i < 20; i++) {
			double angle = Mth.nextDouble(RandomSource.create(), 0, Math.PI * 2);
			double verticalAngle = Mth.nextDouble(RandomSource.create(), -Math.PI / 4, Math.PI / 4);
			double distance = Mth.nextDouble(RandomSource.create(), radius * 0.5, radius);
			double px = x + Math.cos(angle) * distance * Math.cos(verticalAngle);
			double py = y + Math.sin(verticalAngle) * distance;
			double pz = z + Math.sin(angle) * distance * Math.cos(verticalAngle);
			double velocityX = (x - px) * 0.15;
			double velocityY = (y - py) * 0.15;
			double velocityZ = (z - pz) * 0.15;
			if (world instanceof ServerLevel _level) {
				// bright cyan moving inward
				_level.sendParticles(new DustParticleOptions(new Vector3f(0.0f, 0.8f, 1.0f), 1.5f), px, py, pz, 1, velocityX, velocityY, velocityZ, 1.0);
				// darker blue accent
				_level.sendParticles(new DustParticleOptions(new Vector3f(0.0f, 0.4f, 0.9f), 1.0f), px, py, pz, 1, velocityX, velocityY, velocityZ, 1.0);
			}
		}
		// bright core particles
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(new DustParticleOptions(new Vector3f(0.5f, 0.9f, 1.0f), 2.0f), x, y, z, 3, 0.1, 0.1, 0.1, 0.05);
		}
	}
}
