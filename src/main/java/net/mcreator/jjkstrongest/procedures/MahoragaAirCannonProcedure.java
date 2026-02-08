package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaAirCannonProcedure {
	// mid range blast that knocks back
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
		if (t == 12) {
			if (world instanceof ServerLevel level) {
				Vec3 look = entity.getLookAngle();
				for (int i = 1; i <= 14; i++) {
					double px = entity.getX() + look.x * i;
					double py = entity.getY() + 1.5 + look.y * i;
					double pz = entity.getZ() + look.z * i;
					level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 2, 0.05, 0.05, 0.05, 0.01);
				}
			}
			if (entity.distanceTo(target) <= 14 && ((Mob) entity).hasLineOfSight(target)) {
				target.hurt(target.damageSources().mobAttack((LivingEntity) entity), 18.0F);
				Vec3 kb = new Vec3(target.getX() - entity.getX(), 0, target.getZ() - entity.getZ());
				double len = Math.sqrt(kb.x * kb.x + kb.z * kb.z);
				if (len > 0.001) {
					target.setDeltaMovement(target.getDeltaMovement().add(kb.x / len * 2.0, 0.25, kb.z / len * 2.0));
				}
			}
		}
		if (t >= 22) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_cannon", 70);
			entity.getPersistentData().putInt("maho_cd_global", 12);
		}
	}
}
