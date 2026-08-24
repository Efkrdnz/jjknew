package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class InfiniteHollowOnEntityTickUpdateProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		double life = entity.getPersistentData().getDouble("liife") + 1.0;
		entity.getPersistentData().putDouble("liife", life);
		// timings (ticks)
		double LIFE_MAX = 240.0; // 12s
		double CHARGE_END = 150.0; // red/blue phase
		double TWITCH_END = 180.0; // purple twitch/compress
		double FADE_START = 210.0; // start fading out
		double FADE_END = 240.0; // fully gone
		double rad;
		if (life < CHARGE_END) {
			double t = life / CHARGE_END; // 0..1
			rad = 10.0 + (t * t) * 22.0; // 10 -> ~32
		} else if (life < TWITCH_END) {
			double t = (life - CHARGE_END) / (TWITCH_END - CHARGE_END); // 0..1
			rad = 32.0 + t * 10.0; // 32 -> 42
		} else {
			double t = (life - TWITCH_END) / (LIFE_MAX - TWITCH_END); // 0..1
			if (t < 0)
				t = 0;
			if (t > 1)
				t = 1;
			rad = 42.0 + (t * t) * 260.0; // 42 -> ~302
		}
		entity.getPersistentData().putDouble("rad", rad);
		// fade 1 -> 0 near end, used by renderer/shader
		double fade;
		if (life <= FADE_START) {
			fade = 1.0;
		} else {
			double t = (life - FADE_START) / (FADE_END - FADE_START); // 0..1
			if (t < 0)
				t = 0;
			if (t > 1)
				t = 1;
			fade = 1.0 - t;
		}
		entity.getPersistentData().putDouble("fade", fade);
		if (life >= LIFE_MAX) {
			if (!entity.level().isClientSide())
				entity.discard();
		}
	}
}
