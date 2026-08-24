package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaSkyLaunchProcedure {
	// launches target high then switches into dive state
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t") + 1;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		if (t == 1) {
			if (entity instanceof MahoragaEntity _m)
				_m.setAnimation("attack_normal");
		}
		if (t == 6) {
			if (entity.distanceTo(target) <= 3.8) {
				target.invulnerableTime = 0;
				target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 10.0F);
				double dx = target.getX() - entity.getX();
				double dz = target.getZ() - entity.getZ();
				double len = Math.sqrt(dx * dx + dz * dz);
				double pushx = 0;
				double pushz = 0;
				if (len > 0.001) {
					pushx = dx / len;
					pushz = dz / len;
				}
				// ~25-35 blocks depending on drag
				target.setDeltaMovement(target.getDeltaMovement().x * 0.2 + pushx * 0.25, 2.15, target.getDeltaMovement().z * 0.2 + pushz * 0.25);
				target.hurtMarked = true;
				// prevents fall damage during this combo window
				target.getPersistentData().putInt("maho_launch_ticks", 80);
				// store last launch dir for the dive homing start
				entity.getPersistentData().putDouble("maho_launch_dirx", pushx);
				entity.getPersistentData().putDouble("maho_launch_dirz", pushz);
			}
		}
		if (t >= 10) {
			entity.getPersistentData().putString("maho_state", "SKY_DIVE");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_launch", 220);
			entity.getPersistentData().putInt("maho_cd_global", 18);
		}
	}
}
