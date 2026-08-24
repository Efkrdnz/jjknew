package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

import java.util.List;

public class MahoragaGroundSlamProcedure {
	// hop + impact aoe + block break
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t");
		t++;
		entity.getPersistentData().putInt("maho_t", t);
		if (entity instanceof Mob mob)
			mob.getNavigation().stop();
		MahoragaFaceTargetProcedure.execute(entity, target);
		if (t == 1) {
			if (entity instanceof MahoragaEntity) {
				((MahoragaEntity) entity).setAnimation("attack_overhead");
			}
			entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.35, 0));
		}
		if (t == 10) {
			double r = 4.0;
			AABB box = new AABB(entity.blockPosition()).inflate(r);
			List<Entity> list = entity.level().getEntities(entity, box, e -> e instanceof LivingEntity && e != entity);
			for (Entity e : list) {
				LivingEntity le = (LivingEntity) e;
				le.hurt(le.damageSources().mobAttack((LivingEntity) entity), 16.0F);
				Vec3 kb = new Vec3(le.getX() - entity.getX(), 0, le.getZ() - entity.getZ());
				double len = Math.sqrt(kb.x * kb.x + kb.z * kb.z);
				if (len > 0.001) {
					le.setDeltaMovement(le.getDeltaMovement().add(kb.x / len * 1.2, 0.35, kb.z / len * 1.2));
				}
			}
			MahoragaBreakBlocksByHardnessProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), 3);
			if (world instanceof ServerLevel level) {
				level.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 0.2, entity.getZ(), 3, 0.8, 0.2, 0.8, 0.02);
				level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.2, entity.getZ(), 40, 1.2, 0.2, 1.2, 0.05);
			}
		}
		if (t >= 22) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_slam", 90);
			entity.getPersistentData().putInt("maho_cd_global", 14);
		}
	}
}
