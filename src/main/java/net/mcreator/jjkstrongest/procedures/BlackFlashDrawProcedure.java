package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.SimpleParticleType;

import net.mcreator.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.ArrayList;

public class BlackFlashDrawProcedure {
	public static void execute(LevelAccessor world, double end_x, double end_y, double end_z, double start_x, double start_y, double start_z) {
		Vec3 dir = Vec3.ZERO;
		Vec3 perp1 = Vec3.ZERO;
		Vec3 perp2 = Vec3.ZERO;
		ArrayList<Object> offsetX = new ArrayList<>();
		ArrayList<Object> offsetY = new ArrayList<>();
		ArrayList<Object> offsetZ = new ArrayList<>();
		double spacing = 0;
		double totalDist = 0;
		double steps = 0;
		double stepX = 0;
		double stepY = 0;
		double stepZ = 0;
		double segLen = 0;
		double maxDev = 0;
		double zigzagPoints = 0;
		double prog = 0;
		double devStr = 0;
		double rand1 = 0;
		double rand2 = 0;
		double x = 0;
		double y = 0;
		double z = 0;
		double segPos = 0;
		double segIdx = 0;
		double localProg = 0;
		boolean hasBranched = false;
		double branchStartX = 0;
		double branchStartY = 0;
		double branchStartZ = 0;
		double branchEndX = 0;
		double branchEndY = 0;
		double branchEndZ = 0;
		spacing = 0.03;
		maxDev = 0.5;
		segLen = 10;
		totalDist = Math.sqrt(Math.pow(end_x - start_x, 2) + Math.pow(end_y - start_y, 2) + Math.pow(end_z - start_z, 2));
		steps = Math.round(totalDist / spacing);
		if (steps <= 0) {
			return;
		}
		stepX = (end_x - start_x) / steps;
		stepY = (end_y - start_y) / steps;
		stepZ = (end_z - start_z) / steps;
		dir = (new Vec3(stepX, stepY, stepZ)).normalize();
		perp1 = dir.cross(new Vec3(0, 1, 0)).normalize();
		if (perp1.length() < 0.1) {
			perp1 = dir.cross(new Vec3(1, 0, 0)).normalize();
		}
		perp2 = dir.cross(perp1).normalize();
		zigzagPoints = steps / segLen + 1;
		for (int index0 = 0; index0 < (int) zigzagPoints; index0++) {
			prog = index0 / (zigzagPoints - 1);
			devStr = Math.sin(prog * Math.PI * maxDev);
			rand1 = (Mth.nextDouble(RandomSource.create(), 0, 1) - 0.5) * 2 * devStr;
			rand2 = (Mth.nextDouble(RandomSource.create(), 0, 1) - 0.5) * 2 * devStr;
			offsetX.add(toSupportedType((perp1.x() * rand1 + perp2.x() * rand2)));
			offsetY.add(toSupportedType((perp1.y() * rand1 + perp2.y() * rand2)));
			offsetZ.add(toSupportedType((perp1.z() * rand1 + perp2.z() * rand2)));
		}
		offsetX.set(0, 0);
		offsetY.set(0, 0);
		offsetZ.set(0, 0);
		offsetX.set((int) (zigzagPoints - 1), 0);
		offsetY.set((int) (zigzagPoints - 1), 0);
		offsetZ.set((int) (zigzagPoints - 1), 0);
		for (int index1 = 0; index1 < (int) (steps + 1); index1++) {
			x = start_x + stepX * index1;
			y = start_y + stepY * index1;
			z = start_z + stepZ * index1;
			if (index1 > 0 && index1 < steps) {
				segPos = index1 / segLen;
				segIdx = (int) segPos;
				localProg = segPos - segIdx;
				if (segIdx >= zigzagPoints - 1) {
					segIdx = zigzagPoints - 2;
					localProg = 1;
				}
				x = x + /*@Double*/(getListElement(offsetX, (int) segIdx, Double.class, 0.0))
						+ (/*@Double*/(getListElement(offsetX, (int) (segIdx + 1), Double.class, 0.0)) - /*@Double*/(getListElement(offsetX, (int) segIdx, Double.class, 0.0))) * localProg;
				y = y + /*@Double*/(getListElement(offsetY, (int) segIdx, Double.class, 0.0))
						+ (/*@Double*/(getListElement(offsetY, (int) (segIdx + 1), Double.class, 0.0)) - /*@Double*/(getListElement(offsetY, (int) segIdx, Double.class, 0.0))) * localProg;
				z = z + /*@Double*/(getListElement(offsetZ, (int) segIdx, Double.class, 0.0))
						+ (/*@Double*/(getListElement(offsetZ, (int) (segIdx + 1), Double.class, 0.0)) - /*@Double*/(getListElement(offsetZ, (int) segIdx, Double.class, 0.0))) * localProg;
			}
			if (world instanceof ServerLevel _level)
				_level.sendParticles((SimpleParticleType) (JjkStrongestModParticleTypes.PARTICLE_BLACK_FLASH.get()), x, y, z, 1, 0, 0, 0, 0);
			// Branching logic - 5% chance per segment, only after 20% of the way through, and only once
			if (!hasBranched && index1 > steps * 0.2 && Math.random() < 0.05) {
				hasBranched = true;
				// Calculate a random branch direction perpendicular-ish to main direction
				double branchDist = totalDist * Mth.nextDouble(RandomSource.create(), 0.3, 0.6);
				Vec3 randomDir = new Vec3(Mth.nextDouble(RandomSource.create(), -1, 1), Mth.nextDouble(RandomSource.create(), -1, 1), Mth.nextDouble(RandomSource.create(), -1, 1)).normalize();
				// Mix with perpendicular directions for more natural branching
				Vec3 branchDir = dir.scale(0.3).add(randomDir.scale(0.7)).normalize();
				branchStartX = x;
				branchStartY = y;
				branchStartZ = z;
				branchEndX = x + branchDir.x * branchDist;
				branchEndY = y + branchDir.y * branchDist;
				branchEndZ = z + branchDir.z * branchDist;
			}
		}
		// Draw the branch if one was created
		if (hasBranched) {
			Vec3 branchDirVec = Vec3.ZERO;
			Vec3 branchPerp1 = Vec3.ZERO;
			Vec3 branchPerp2 = Vec3.ZERO;
			ArrayList<Object> branchOffsetX = new ArrayList<>();
			ArrayList<Object> branchOffsetY = new ArrayList<>();
			ArrayList<Object> branchOffsetZ = new ArrayList<>();
			double branchSpacing = 0.05 * 0.6;
			double branchMaxDev = 0.5 * 0.6;
			double branchSegLen = 10;
			double branchTotalDist = Math.sqrt(Math.pow(branchEndX - branchStartX, 2) + Math.pow(branchEndY - branchStartY, 2) + Math.pow(branchEndZ - branchStartZ, 2));
			double branchSteps = Math.round(branchTotalDist / branchSpacing);
			if (branchSteps > 0) {
				double branchStepX = (branchEndX - branchStartX) / branchSteps;
				double branchStepY = (branchEndY - branchStartY) / branchSteps;
				double branchStepZ = (branchEndZ - branchStartZ) / branchSteps;
				branchDirVec = (new Vec3(branchStepX, branchStepY, branchStepZ)).normalize();
				branchPerp1 = branchDirVec.cross(new Vec3(0, 1, 0)).normalize();
				if (branchPerp1.length() < 0.1) {
					branchPerp1 = branchDirVec.cross(new Vec3(1, 0, 0)).normalize();
				}
				branchPerp2 = branchDirVec.cross(branchPerp1).normalize();
				double branchZigzagPoints = branchSteps / branchSegLen + 1;
				for (int index0 = 0; index0 < (int) branchZigzagPoints; index0++) {
					prog = index0 / (branchZigzagPoints - 1);
					devStr = Math.sin(prog * Math.PI * branchMaxDev);
					rand1 = (Mth.nextDouble(RandomSource.create(), 0, 1) - 0.5) * 2 * devStr;
					rand2 = (Mth.nextDouble(RandomSource.create(), 0, 1) - 0.5) * 2 * devStr;
					branchOffsetX.add(toSupportedType((branchPerp1.x() * rand1 + branchPerp2.x() * rand2)));
					branchOffsetY.add(toSupportedType((branchPerp1.y() * rand1 + branchPerp2.y() * rand2)));
					branchOffsetZ.add(toSupportedType((branchPerp1.z() * rand1 + branchPerp2.z() * rand2)));
				}
				branchOffsetX.set(0, 0);
				branchOffsetY.set(0, 0);
				branchOffsetZ.set(0, 0);
				branchOffsetX.set((int) (branchZigzagPoints - 1), 0);
				branchOffsetY.set((int) (branchZigzagPoints - 1), 0);
				branchOffsetZ.set((int) (branchZigzagPoints - 1), 0);
				for (int index1 = 0; index1 < (int) (branchSteps + 1); index1++) {
					x = branchStartX + branchStepX * index1;
					y = branchStartY + branchStepY * index1;
					z = branchStartZ + branchStepZ * index1;
					if (index1 > 0 && index1 < branchSteps) {
						segPos = index1 / branchSegLen;
						segIdx = (int) segPos;
						localProg = segPos - segIdx;
						if (segIdx >= branchZigzagPoints - 1) {
							segIdx = branchZigzagPoints - 2;
							localProg = 1;
						}
						x = x + /*@Double*/(getListElement(branchOffsetX, (int) segIdx, Double.class, 0.0))
								+ (/*@Double*/(getListElement(branchOffsetX, (int) (segIdx + 1), Double.class, 0.0)) - /*@Double*/(getListElement(branchOffsetX, (int) segIdx, Double.class, 0.0))) * localProg;
						y = y + /*@Double*/(getListElement(branchOffsetY, (int) segIdx, Double.class, 0.0))
								+ (/*@Double*/(getListElement(branchOffsetY, (int) (segIdx + 1), Double.class, 0.0)) - /*@Double*/(getListElement(branchOffsetY, (int) segIdx, Double.class, 0.0))) * localProg;
						z = z + /*@Double*/(getListElement(branchOffsetZ, (int) segIdx, Double.class, 0.0))
								+ (/*@Double*/(getListElement(branchOffsetZ, (int) (segIdx + 1), Double.class, 0.0)) - /*@Double*/(getListElement(branchOffsetZ, (int) segIdx, Double.class, 0.0))) * localProg;
					}
					if (world instanceof ServerLevel _level)
						_level.sendParticles((SimpleParticleType) (JjkStrongestModParticleTypes.PARTICLE_BLACK_FLASH.get()), x, y, z, 1, 0, 0, 0, 0);
				}
			}
		}
	}

	private static double toSupportedType(Number value) {
		return value.doubleValue();
	}

	private static <E> E toSupportedType(E e) {
		return e;
	}

	private static <E> E getListElement(ArrayList<Object> objects, int index, Class<E> eClass, Object defaultValue) {
		if (index < objects.size()) {
			var element = objects.get(index);
			if (eClass.isInstance(element)) {
				return eClass.cast(element);
			}
		}
		return eClass.cast(defaultValue);
	}
}
