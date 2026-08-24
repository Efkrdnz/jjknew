package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class MahoragaBreakBlocksByHardnessProcedure {
	// breaks blocks in radius if destroy speed is below obsidian and mobgriefing is true
	public static void execute(LevelAccessor world, double x, double y, double z, int radius) {
		if (world == null)
			return;
		if (world instanceof ServerLevel level) {
			if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
				return;
			final float OBSIDIAN_HARDNESS = 50.0F;
			BlockPos center = BlockPos.containing(x, y, z);
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dy = -radius; dy <= radius; dy++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if ((dx * dx + dy * dy + dz * dz) > (radius * radius))
							continue;
						BlockPos pos = center.offset(dx, dy, dz);
						BlockState state = level.getBlockState(pos);
						if (state.isAir())
							continue;
						if (!state.getFluidState().isEmpty() && state.getFluidState().getType() != Fluids.EMPTY)
							continue;
						BlockEntity be = level.getBlockEntity(pos);
						if (be != null)
							continue;
						float destroySpeed = state.getDestroySpeed(level, pos);
						if (destroySpeed < 0)
							continue;
						if (destroySpeed < OBSIDIAN_HARDNESS) {
							level.destroyBlock(pos, true);
						}
					}
				}
			}
		}
	}
}
