package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaAuraFarmProcedure {
	// pause to charge and give player a break, cancels if hurt
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t");
		t++;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		entity.setDeltaMovement(new Vec3(0, entity.getDeltaMovement().y * 0.2, 0));
		MahoragaFaceTargetProcedure.execute(entity, target);
		if (t == 1) {
			if (entity instanceof MahoragaEntity) {
				((MahoragaEntity) entity).setAnimation("idle");
			}
			entity.getPersistentData().putInt("maho_cd_aura", 240);
		}
		// interrupted if recently hurt
		if (entity.getPersistentData().getInt("maho_hurt_ticks") > 0) {
			entity.getPersistentData().putString("maho_state", "DASH");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (t >= 60) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_global", 16);
		}
	}
}
