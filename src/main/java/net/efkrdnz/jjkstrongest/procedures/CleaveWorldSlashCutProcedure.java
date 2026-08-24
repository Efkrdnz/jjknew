package net.efkrdnz.jjkstrongest.procedures;


import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

public class CleaveWorldSlashCutProcedure {
	public static void execute(LevelAccessor world, Entity entity, double forwardRange, int maxBlocks) {
		if (entity == null)
			return;
		if (!(world instanceof Level lvl))
			return;
		if (lvl.isClientSide())
			return;
		CompoundTag data = entity.getPersistentData();
		int count = Math.max(0, Math.min(4, data.getInt("cleave_slash_count")));
		if (count <= 0)
			return;
		Vec3 eye = entity.getEyePosition();
		Vec3 look = entity.getLookAngle().normalize();
		Vec3 up = new Vec3(0, 1, 0);
		Vec3 right = look.cross(up);
		if (right.lengthSqr() < 1e-6) {
			right = new Vec3(1, 0, 0);
		} else {
			right = right.normalize();
		}
		Vec3 camUp = right.cross(look).normalize();
		// canvas position in front of player
		double canvasDist = Math.min(6.0, forwardRange);
		Vec3 canvasCenter = eye.add(look.scale(canvasDist));
		// how wide/tall the canvas is in world blocks
		double canvasHalfSize = 4.8; // bigger = more world area affected
		int destroyed = 0;
		for (int i = 1; i <= count; i++) {
			if (!(data.get("cleave_slash" + i) instanceof ListTag tag) || tag.size() < 4)
				continue;
			float sx = tag.getFloat(0);
			float sy = tag.getFloat(1);
			float ang = tag.getFloat(2);
			float strength = tag.getFloat(3);
			Vec3 origin = canvasCenter.add(right.scale((sx - 0.5) * 2.0 * canvasHalfSize)).add(camUp.scale((sy - 0.5) * 2.0 * canvasHalfSize));
			Vec3 lineDir = right.scale(Math.cos(ang)).add(camUp.scale(Math.sin(ang))).normalize();
			double halfLen = 6.5; // slash length on canvas
			double step = 0.35;
			double thickness = 0.9 + (strength * 0.35);
			for (double s = -halfLen; s <= halfLen; s += step) {
				Vec3 p = origin.add(lineDir.scale(s));
				int cx = (int) Math.floor(p.x);
				int cy = (int) Math.floor(p.y);
				int cz = (int) Math.floor(p.z);
				int rad = (int) Math.ceil(thickness);
				for (int dx = -rad; dx <= rad; dx++) {
					for (int dy = -rad; dy <= rad; dy++) {
						for (int dz = -rad; dz <= rad; dz++) {
							double dd = dx * dx + dy * dy + dz * dz;
							if (dd > thickness * thickness)
								continue;
							BlockPos pos = new BlockPos(cx + dx, cy + dy, cz + dz);
							// only cut blocks in front-ish region (avoid cutting behind player)
							Vec3 to = Vec3.atCenterOf(pos).subtract(eye);
							if (to.dot(look) < 0.0)
								continue;
							if (to.length() > forwardRange + 2.0)
								continue;
							if (lvl.getBlockState(pos).isAir())
								continue;
							if (lvl.getBlockState(pos).is(Blocks.BEDROCK))
								continue;
							lvl.destroyBlock(pos, true);
							destroyed++;
							if (destroyed >= maxBlocks)
								return;
						}
					}
				}
			}
		}
	}
}
