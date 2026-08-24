package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.radians;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.DismantleTravelEntity;
import net.mcreator.jjkstrongest.Freezeframe;

public class ShootDismantleNetProcedure {
	public static void execute(Level world, Entity entity, double techniquePower, double output, boolean breakBlocks) {
		if (world == null || entity == null)
			return;
		if (!(entity instanceof LivingEntity shooter))
			return;
		// chargeTicks drives AMOUNT of slashes
		int chargeTicks = (int) Math.round(entity.getPersistentData().getDouble("ChantCounter"));
		if (chargeTicks < 0)
			chargeTicks = 0;
		// 2x2 uncharged, 3x3 at 20, 4x4 at 40, 5x5 at 60
		int size = 2 + Math.min(chargeTicks / 20, 3);
		if (size < 2)
			size = 2;
		if (size > 5)
			size = 5;
		// TechniquePower only scales slash size, damage, width, etc
		double tp = entity.getPersistentData().getDouble("TechniquePower");
		if (tp <= 0)
			tp = techniquePower;
		if (tp <= 0)
			tp = 1.0;
		double out = output;
		if (out <= 0)
			out = ReturnOutputDismantleProcedure.execute(world, entity) + 1.0;
		if (out <= 0)
			out = 1.0;
		double baseDamage = 8.0;
		double dmg = baseDamage * tp * out;
		Vec3 forward = shooter.getViewVector(1).normalize();
		Vec3 right = new Vec3(0, 1, 0).cross(forward);
		if (right.lengthSqr() < 1.0e-6)
			right = new Vec3(1, 0, 0);
		right = right.normalize();
		Vec3 up = right.cross(forward).normalize();
		// add 2 more block offset forward so net starts away from you
		double forwardOffset = 2.0;
		// spacing between lines
		// 1 block was original, now +2 blocks
		double spacing = 3.0;
		double[] offsets = buildOffsets(size, spacing);
		// How much each slash fans outward per block of lateral offset.
		// A value of 0.035 gives a gentle spread — outer slashes diverge ~6° at 3 blocks offset,
		// opening the net at range without the outer slashes flying too far off-axis.
		double divergeFactor = 0.035;
		// horizontal lines, stacked by up offsets, roll 0 degrees
		for (int i = 0; i < offsets.length; i++) {
			Vec3 offset = forward.scale(forwardOffset).add(up.scale(offsets[i]));
			DismantleTravelEntity proj = DismantleTravelEntity.shoot(world, shooter, world.random, 1.0f, tp, out, dmg, breakBlocks, 0, false, chargeTicks);
			if (proj != null) {
				moveAndSync(proj, shooter, offset);
				overrideRollAndCut(proj, shooter, 0.0);
				// fan this slash outward along the up-axis proportional to its offset
				applyDivergence(proj, forward, up, offsets[i], divergeFactor);
			}
		}
		// vertical lines, stacked by right offsets, roll 90 degrees
		for (int i = 0; i < offsets.length; i++) {
			Vec3 offset = forward.scale(forwardOffset).add(right.scale(offsets[i]));
			DismantleTravelEntity proj = DismantleTravelEntity.shoot(world, shooter, world.random, 1.0f, tp, out, dmg, breakBlocks, 0, false, chargeTicks);
			if (proj != null) {
				moveAndSync(proj, shooter, offset);
				overrideRollAndCut(proj, shooter, 90.0);
				// fan this slash outward along the right-axis proportional to its offset
				applyDivergence(proj, forward, right, offsets[i], divergeFactor);
			}
		}
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
	}

	/**
	 * Overrides the fixed travel direction of a spawned slash projectile so it fans
	 * outward from the centre of the net. {@code spreadAxis} is the axis along which
	 * slashes are stacked (up for horizontal lines, right for vertical lines).
	 * {@code lateralOffset} is how far this particular slash sits from the centre
	 * along that axis. The resulting travel vector tilts away from {@code forward} by
	 * {@code divergeFactor * lateralOffset} radians worth of blend — outer slashes
	 * spread progressively further apart as they travel, making the net pattern legible
	 * at any range instead of collapsing into one blurry gap.
	 */
	private static void applyDivergence(DismantleTravelEntity proj,
			Vec3 forward, Vec3 spreadAxis,
			double lateralOffset, double divergeFactor) {
		// blend a fraction of the spread axis into the forward direction, then normalise —
		// this is equivalent to rotating forward toward spreadAxis by arctan(blend)
		double blend = lateralOffset * divergeFactor;
		Vec3 diverged = forward.add(spreadAxis.scale(blend)).normalize();
		proj.getPersistentData().putDouble("fixed_vx", diverged.x);
		proj.getPersistentData().putDouble("fixed_vy", diverged.y);
		proj.getPersistentData().putDouble("fixed_vz", diverged.z);
		// also prime the current frame's delta movement so the very first tick is correct
		double spd = proj.getPersistentData().getDouble("fixed_speed");
		if (spd > 0) {
			proj.setDeltaMovement(diverged.scale(spd));
		}
	}

	private static double[] buildOffsets(int size, double spacing) {
		double[] arr = new double[size];
		if (size % 2 == 1) {
			int half = size / 2;
			for (int i = 0; i < size; i++) {
				arr[i] = (i - half) * spacing;
			}
		} else {
			double start = -((size / 2.0) - 0.5) * spacing;
			for (int i = 0; i < size; i++) {
				arr[i] = start + i * spacing;
			}
		}
		return arr;
	}

	private static void moveAndSync(DismantleTravelEntity proj, LivingEntity shooter, Vec3 offset) {
		Vec3 eye = shooter.getEyePosition();
		Vec3 pos = eye.add(offset);
		proj.setPos(pos.x, pos.y, pos.z);
		proj.getPersistentData().putDouble("lastX", pos.x);
		proj.getPersistentData().putDouble("lastY", pos.y);
		proj.getPersistentData().putDouble("lastZ", pos.z);
		proj.getPersistentData().putInt("arm_ticks", 6);
	}

	private static void overrideRollAndCut(DismantleTravelEntity proj, LivingEntity shooter, double rollDeg) {
		Vec3 forward = shooter.getViewVector(1).normalize();
		Vec3 right = new Vec3(0, 1, 0).cross(forward);
		if (right.lengthSqr() < 1.0e-6)
			right = new Vec3(1, 0, 0);
		right = right.normalize();
		double rollRad = Math.toRadians(rollDeg);
		Vec3 cutAxis = rotateAroundAxis(right, forward, rollRad).normalize();
		proj.getPersistentData().putDouble("cut_x", cutAxis.x);
		proj.getPersistentData().putDouble("cut_y", cutAxis.y);
		proj.getPersistentData().putDouble("cut_z", cutAxis.z);
		proj.setSlashParams(proj.getSlashLength(), proj.getSlashWidth(), proj.getSlashStyle(), (float) rollRad, proj.getSlashSeed(), (float) forward.x, (float) forward.y, (float) forward.z, proj.getColorR(), proj.getColorG(), proj.getColorB());
	}

	private static Vec3 rotateAroundAxis(Vec3 v, Vec3 axis, double radians) {
		Vec3 k = axis.normalize();
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		double dot = v.dot(k);
		Vec3 term1 = v.scale(cos);
		Vec3 term2 = k.cross(v).scale(sin);
		Vec3 term3 = k.scale(dot * (1.0 - cos));
		return term1.add(term2).add(term3);
	}
}
