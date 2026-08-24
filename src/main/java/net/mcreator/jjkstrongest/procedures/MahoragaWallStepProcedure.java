package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaWallStepProcedure {
	// wall step impulse and delayed break
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t");
		t++;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		if (t == 1) {
			if (entity instanceof MahoragaEntity) {
				((MahoragaEntity) entity).setAnimation("jump");
			}
			Vec3 look = entity.getLookAngle();
			entity.setDeltaMovement(look.x * 0.75, 0.95, look.z * 0.75);
			entity.hurtMarked = true;
			entity.getPersistentData().putInt("maho_wall_break_delay", 8);
			entity.getPersistentData().putInt("maho_cd_wallstep", 35);
		}
		if (t >= 12) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_global", 6);
		}
	}
}
