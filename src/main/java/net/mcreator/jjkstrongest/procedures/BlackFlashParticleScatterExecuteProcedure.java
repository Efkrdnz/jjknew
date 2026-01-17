package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class BlackFlashParticleScatterExecuteProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		for (int index0 = 0; index0 < Mth.nextInt(RandomSource.create(), 15, 25); index0++) {
			double finalX = x + Mth.nextDouble(RandomSource.create(), -10, 10);
			double finalY = y + 1 + Mth.nextDouble(RandomSource.create(), -10, 10);
			double finalZ = z + Mth.nextDouble(RandomSource.create(), -10, 10);
			double dirX = finalX - x;
			double dirY = finalY - (y + 1);
			double dirZ = finalZ - z;
			int numJumps = Mth.nextInt(RandomSource.create(), 5, 8);
			double currentX = x;
			double currentY = y + 1;
			double currentZ = z;
			for (int jumpIndex = 0; jumpIndex < numJumps; jumpIndex++) {
				int delay = jumpIndex * 1;
				double progress = (double) (jumpIndex + 1) / numJumps;
				double nextX = x + dirX * progress;
				double nextY = (y + 1) + dirY * progress;
				double nextZ = z + dirZ * progress;
				final double startX = currentX;
				final double startY = currentY;
				final double startZ = currentZ;
				final double endX = nextX;
				final double endY = nextY;
				final double endZ = nextZ;
				if (delay == 0) {
					BlackFlashDrawProcedure.execute(world, endX, endY, endZ, startX, startY, startZ);
				} else {
					new BlackFlashScheduledTask(world, delay, endX, endY, endZ, startX, startY, startZ).start();
				}
				currentX = nextX;
				currentY = nextY;
				currentZ = nextZ;
			}
		}
	}

	private static class BlackFlashScheduledTask {
		private int ticks = 0;
		private final int waitTicks;
		private final LevelAccessor world;
		private final double endX, endY, endZ;
		private final double startX, startY, startZ;

		public BlackFlashScheduledTask(LevelAccessor world, int waitTicks, double endX, double endY, double endZ, double startX, double startY, double startZ) {
			this.world = world;
			this.waitTicks = waitTicks;
			this.endX = endX;
			this.endY = endY;
			this.endZ = endZ;
			this.startX = startX;
			this.startY = startY;
			this.startZ = startZ;
		}

		public void start() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void tick(TickEvent.ServerTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				this.ticks += 1;
				if (this.ticks >= this.waitTicks) {
					run();
				}
			}
		}

		private void run() {
			BlackFlashDrawProcedure.execute(world, endX, endY, endZ, startX, startY, startZ);
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}
}
