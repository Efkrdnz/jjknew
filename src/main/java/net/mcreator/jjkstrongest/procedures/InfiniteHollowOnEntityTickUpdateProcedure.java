package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

public class InfiniteHollowOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		entity.getPersistentData().putDouble("liife", (entity.getPersistentData().getDouble("liife") + 1));
		entity.getPersistentData().putDouble("rad", 10);
		if (entity.getPersistentData().getDouble("liife") >= 120) {
			if (!entity.level().isClientSide())
				entity.discard();
		}
		if (entity.getPersistentData().getDouble("liife") >= 75) {
			if (entity.getPersistentData().getDouble("liife") % 5 == 0) {
				entity.getPersistentData().putDouble("rad", (entity.getPersistentData().getDouble("rad") + 4));
				int horizontalRadiusSphere = (int) (entity.getPersistentData().getDouble("rad")) - 1;
				int verticalRadiusSphere = (int) (entity.getPersistentData().getDouble("rad")) - 1;
				int yIterationsSphere = verticalRadiusSphere;
				for (int i = -yIterationsSphere; i <= yIterationsSphere; i++) {
					for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
						for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
							double distanceSq = (xi * xi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere) + (i * i) / (double) (verticalRadiusSphere * verticalRadiusSphere)
									+ (zi * zi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere);
							if (distanceSq <= 1.0) {
								if (world instanceof ServerLevel _level)
									_level.sendParticles(ParticleTypes.EXPLOSION, x + xi, y + i, z + zi, 1, 0, 0, 0, 1);
								world.setBlock(BlockPos.containing(x + xi, y + i, y + i), Blocks.AIR.defaultBlockState(), 3);
							}
						}
					}
				}
			}
		}
	}
}
