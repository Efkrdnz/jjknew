package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.cd;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

public class TeleportBlinkProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (world == null || entity == null)
			return;
		if (world instanceof Level level) {
			if (level.isClientSide())
				return;
		} else {
			return;
		}
		int cd = entity.getPersistentData().getInt("tp_cd_ticks");
		if (cd > 0)
			return;
		final double maxRange = 16.0;
		Vec3 eye = entity.getEyePosition(1.0F);
		Vec3 view = entity.getViewVector(1.0F);
		Vec3 end = eye.add(view.x * maxRange, view.y * maxRange, view.z * maxRange);
		BlockHitResult hit = entity.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
		Vec3 dest = end;
		if (hit.getType() == HitResult.Type.BLOCK) {
			Vec3 hitLoc = hit.getLocation();
			Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
			double backOff = 0.35;
			dest = hitLoc.add(normal.x * backOff, normal.y * backOff, normal.z * backOff);
			dest = dest.subtract(view.x * 0.15, view.y * 0.15, view.z * 0.15);
		}
		double tx = dest.x;
		double ty = dest.y;
		double tz = dest.z;
		if (!isTwoBlockSpace(world, tx, ty, tz))
			return;
		teleport(entity, tx, ty, tz);
		entity.getPersistentData().putInt("tp_cd_ticks", 10);
	}

	private static boolean isTwoBlockSpace(LevelAccessor world, double x, double y, double z) {
		BlockPos feet = BlockPos.containing(x, y, z);
		BlockPos head = feet.above();
		BlockState feetState = world.getBlockState(feet);
		if (!feetState.getCollisionShape(world, feet).isEmpty())
			return false;
		BlockState headState = world.getBlockState(head);
		if (!headState.getCollisionShape(world, head).isEmpty())
			return false;
		return true;
	}

	private static void teleport(Entity entity, double x, double y, double z) {
		if (entity instanceof ServerPlayer sp) {
			sp.connection.teleport(x, y, z, sp.getYRot(), sp.getXRot());
		} else {
			entity.teleportTo(x, y, z);
		}
	}
}
