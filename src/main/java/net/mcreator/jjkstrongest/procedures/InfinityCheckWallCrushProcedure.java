package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class InfinityCheckWallCrushProcedure {
	public static void execute(LevelAccessor world, Entity entity, Vec3 pushDir, double pushForce, Entity sourceEntity) {
		if (entity == null || sourceEntity == null || !(world instanceof Level _level))
			return;
		Vec3 start = entity.position();
		Vec3 end = start.add(pushDir.x * 1.5, 0, pushDir.z * 1.5);
		BlockHitResult hit = _level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
		if (hit.getType() == BlockHitResult.Type.BLOCK) {
			BlockPos wallPos = hit.getBlockPos();
			BlockState wallState = _level.getBlockState(wallPos);
			Direction hitDirection = hit.getDirection();
			if (hitDirection == Direction.UP || hitDirection == Direction.DOWN)
				return;
			if (!wallState.isAir() && wallState.getDestroySpeed(_level, wallPos) >= 0) {
				CompoundTag nbt = entity.getPersistentData();
				nbt.putBoolean("infinityCrushing", true);
				nbt.putInt("infinityCrushX", wallPos.getX());
				nbt.putInt("infinityCrushY", wallPos.getY());
				nbt.putInt("infinityCrushZ", wallPos.getZ());
				nbt.putDouble("infinityCrushForce", pushForce);
				nbt.putString("infinityCrushDirection", hitDirection.getName());
				InfinityProcessCrushProcedure.execute(world, entity, sourceEntity);
			}
		} else {
			CompoundTag nbt = entity.getPersistentData();
			nbt.putBoolean("infinityCrushing", false);
			nbt.putFloat("infinityCrushTimer", 0);
		}
	}
}
