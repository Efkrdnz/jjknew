package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

public class MeleeJumpSlamProcedure {
	// executes downward slam attack
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		// propel player downward
		Vec3 currentVel = entity.getDeltaMovement();
		entity.setDeltaMovement(currentVel.x, -2.0, currentVel.z);
		entity.hurtMarked = true;
		// set slam state
		CompoundTag nbt = entity.getPersistentData();
		nbt.putBoolean("is_slamming", true);
	}

	// handles slam impact when player hits ground
	public static void onSlamImpact(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		CompoundTag nbt = entity.getPersistentData();
		if (!nbt.getBoolean("is_slamming"))
			return;
		if (entity.onGround()) {
			nbt.putBoolean("is_slamming", false);
			// damage nearby entities
			AABB impactBox = new AABB(x - 4, y - 1, z - 4, x + 4, y + 2, z + 4);
			List<Entity> targets = world.getEntitiesOfClass(Entity.class, impactBox, e -> e != entity && e instanceof LivingEntity && e.isAlive());
			for (Entity target : targets) {
				double distance = target.distanceTo(entity);
				if (distance <= 4) {
					// damage scales with distance
					float damage = (float) (12 - (distance * 2));
					if (world instanceof ServerLevel serverLevel) {
						target.hurt(new DamageSource(serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity), damage);
					}
					// knockback away from impact
					Vec3 direction = target.position().subtract(entity.position()).normalize();
					Vec3 knockback = direction.multiply(1.5, 0.8, 1.5);
					target.setDeltaMovement(knockback);
					target.hurtMarked = true;
				}
			}
			// spawn impact particles
			if (world instanceof ServerLevel serverLevel) {
				for (int i = 0; i < 30; i++) {
					double offsetX = (Math.random() - 0.5) * 4;
					double offsetZ = (Math.random() - 0.5) * 4;
					serverLevel.sendParticles(ParticleTypes.EXPLOSION, x + offsetX, y + 0.1, z + offsetZ, 1, 0, 0, 0, 0);
					serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, x + offsetX, y + 0.5, z + offsetZ, 1, 0, 0.2, 0, 0);
				}
			}
		}
	}
}
