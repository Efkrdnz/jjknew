package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.SimpleParticleType;

import net.mcreator.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.List;
import java.util.Comparator;

public class InfinityOnEffectActiveTickProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double radius = entity.isShiftKeyDown() ? 5.0 : 3.0;
		double pushForce = entity.isShiftKeyDown() ? 0.8 : 0.3;
		final Vec3 _center = new Vec3(x, y, z);
		List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(radius), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
		for (Entity targetEntity : _entfound) {
			if (targetEntity == entity)
				continue;
			if (targetEntity instanceof Projectile) {
				InfinityFreezeProjectileProcedure.execute(targetEntity);
				continue;
			}
			if (!(targetEntity instanceof LivingEntity))
				continue;
			Vec3 pushDir = new Vec3(targetEntity.getX() - x, 0, targetEntity.getZ() - z).normalize();
			double distance = Math.sqrt(Math.pow(targetEntity.getX() - x, 2) + Math.pow(targetEntity.getZ() - z, 2));
			double scaledForce = pushForce * (1.0 - (distance / radius));
			targetEntity.setDeltaMovement(pushDir.x * scaledForce, targetEntity.getDeltaMovement().y, pushDir.z * scaledForce);
			InfinityCheckWallCrushProcedure.execute(world, targetEntity, pushDir, scaledForce, entity);
		}
		if (world instanceof ServerLevel _level)
			_level.sendParticles((SimpleParticleType) (JjkStrongestModParticleTypes.INFINITY_PARTICLE.get()), x, y + 1, z, 20, radius * 0.3, 0.8, radius * 0.3, 0);
	}
}
