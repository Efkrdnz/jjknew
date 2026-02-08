package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaBarrageProcedure {
	// close-range multi hit
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
				((MahoragaEntity) entity).setAnimation("attack_normal");
			}
		}
		if (t <= 40 && t % 4 == 0) {
			if (entity.distanceTo(target) <= 3.2) {
				target.invulnerableTime = 0;
				target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 6.0F);
				Vec3 kb = new Vec3(target.getX() - entity.getX(), 0, target.getZ() - entity.getZ());
				double len = Math.sqrt(kb.x * kb.x + kb.z * kb.z);
				if (len > 0.001) {
					target.setDeltaMovement(target.getDeltaMovement().add(kb.x / len * 0.6, 0.08, kb.z / len * 0.6));
				}
			}
		}
		if (t >= 55) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_barrage", 80);
			entity.getPersistentData().putInt("maho_cd_global", 12);
		}
	}
}
