package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

public class MahoragaPressureAndRegenTickProcedure {
	// handles burst window decay and out-of-pressure regen
	public static void execute(ServerLevel level, Entity entity) {
		if (level == null || entity == null)
			return;
		if (!(entity instanceof LivingEntity living))
			return;
		if (entity instanceof LivingEntity le) {
			if (le.isDeadOrDying() || le.getHealth() <= 0) {
				return;
			}
		}
		int ticks = entity.getPersistentData().getInt("maho_recent_ticks");
		if (ticks > 0) {
			entity.getPersistentData().putInt("maho_recent_ticks", ticks - 1);
			if (ticks - 1 <= 0) {
				entity.getPersistentData().putDouble("maho_recent_dmg", 0);
			}
		}
		// regen if not pressured, suppressed by bf
		if (entity.getPersistentData().getInt("maho_recent_ticks") <= 0) {
			double bf = entity.getPersistentData().getDouble("maho_bf");
			double mult = Math.max(0, 1.0 - bf * 0.2); // bf 5 -> 0
			float max = living.getMaxHealth();
			float heal = (float) (max * 0.0012 * mult); // ~2.4% per sec at bf=0
			if (heal > 0 && living.getHealth() < max) {
				living.setHealth(Math.min(max, living.getHealth() + heal));
			}
		}
	}
}
