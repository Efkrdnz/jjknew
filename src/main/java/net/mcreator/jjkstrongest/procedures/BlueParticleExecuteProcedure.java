package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.core.particles.SimpleParticleType;

import net.mcreator.jjkstrongest.init.JjkStrongestModParticleTypes;

public class BlueParticleExecuteProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double Z0 = 0;
		double Y0 = 0;
		double X0 = 0;
		double Zc = 0;
		double Yc = 0;
		double Zd = 0;
		double Xc = 0;
		double Yd = 0;
		double Xd = 0;
		for (int index0 = 0; index0 < 30; index0++) {
			Xc = entity.getX();
			Yc = entity.getY() + 0.1;
			Zc = entity.getZ();
			X0 = entity.getX() + Mth.nextInt(RandomSource.create(), Mth.nextInt(RandomSource.create(), -6, -3), Mth.nextInt(RandomSource.create(), 3, 6));
			Y0 = entity.getY() + Mth.nextInt(RandomSource.create(), Mth.nextInt(RandomSource.create(), -6, -3), Mth.nextInt(RandomSource.create(), 3, 6));
			Z0 = entity.getZ() + Mth.nextInt(RandomSource.create(), Mth.nextInt(RandomSource.create(), -6, -3), Mth.nextInt(RandomSource.create(), 3, 6));
			Xd = (Xc - X0) * 0.05;
			Yd = (Yc - Y0) * 0.05;
			Zd = (Zc - Z0) * 0.05;
			if (Math.random() < (1) / ((float) 3)) {
				world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.BLUEPARTICLE_1.get()), X0, Y0, Z0, Xd, Yd, Zd);
			} else {
				if (Math.random() < (1) / ((float) 2)) {
					world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.BLUEPARTICLE_2.get()), X0, Y0, Z0, Xd, Yd, Zd);
				} else {
					world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.BLUEPARTICLE_3.get()), X0, Y0, Z0, Xd, Yd, Zd);
				}
			}
		}
		for (int index1 = 0; index1 < 15; index1++) {
			Xc = entity.getX();
			Yc = entity.getY() + 0.1;
			Zc = entity.getZ();
			X0 = entity.getX() + Mth.nextDouble(RandomSource.create(), -0.5, 0.5);
			Y0 = entity.getY() + Mth.nextDouble(RandomSource.create(), -0.5, 0.5);
			Z0 = entity.getZ() + Mth.nextDouble(RandomSource.create(), -0.5, 0.5);
			Xd = (Xc - X0) * 0.05;
			Yd = (Yc - Y0) * 0.05;
			Zd = (Zc - Z0) * 0.05;
			world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.BLUE_DUST.get()), X0, Y0, Z0, Xd, Yd, Zd);
		}
		if (entity.getPersistentData().getDouble("BlueLife") % 20 == 1) {
			world.addParticle((SimpleParticleType) (JjkStrongestModParticleTypes.BLUE_AURA.get()), (entity.getX()), (entity.getY()), (entity.getZ()), 0, 0, 0);
		}
	}
}
