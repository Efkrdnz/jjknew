package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Comparator;

public class CleaveHoldTickProcedure {
	// update cleave hold state and find target
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		CompoundTag data = entity.getPersistentData();
		if (!data.getBoolean("cleave_holding"))
			return;
		boolean hasTarget = data.getBoolean("cleave_has_target");
		// if no target yet, keep searching
		if (!hasTarget) {
			Vec3 look = entity.getLookAngle();
			Vec3 start = entity.getEyePosition();
			Vec3 end = start.add(look.scale(3.5));
			AABB searchBox = new AABB(start, end).inflate(1.5);
			List<Entity> targets = world.getEntitiesOfClass(Entity.class, searchBox, e -> e != entity && e instanceof LivingEntity && e.isAlive()).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(entity))).toList();
			if (!targets.isEmpty()) {
				Entity target = targets.get(0);
				data.putString("cleave_target_uuid", target.getStringUUID());
				data.putBoolean("cleave_has_target", true);
				data.putDouble("cleave_hold_timer", 0);
				// freeze both
				if (entity instanceof LivingEntity living) {
					living.setDeltaMovement(Vec3.ZERO);
				}
				if (target instanceof LivingEntity living) {
					living.setDeltaMovement(Vec3.ZERO);
				}
				// trigger shaders when lock happens
				if (world instanceof Level _level) {
					TriggerCleaveDistortionProcedure.execute(_level, entity);
					TestCleaveDistortionProcedure.execute(_level, entity);
				}
				// start camera lock
				data.putBoolean("cleave_camera_locked", true);
				// sound
				if (!world.isClientSide()) {
					world.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
				}
			}
			return;
		}
		// has target, continue with existing logic
		double timer = data.getDouble("cleave_hold_timer");
		String targetUUID = data.getString("cleave_target_uuid");
		if (targetUUID.isEmpty()) {
			data.putBoolean("cleave_holding", false);
			data.putBoolean("cleave_has_target", false);
			data.putBoolean("cleave_camera_locked", false);
			return;
		}
		Entity target = null;
		if (world instanceof Level _level) {
			List<Entity> entities = _level.getEntitiesOfClass(Entity.class, new AABB(entity.getX() - 10, entity.getY() - 10, entity.getZ() - 10, entity.getX() + 10, entity.getY() + 10, entity.getZ() + 10));
			for (Entity e : entities) {
				if (e.getStringUUID().equals(targetUUID)) {
					target = e;
					break;
				}
			}
		}
		if (target == null || !target.isAlive()) {
			data.putBoolean("cleave_holding", false);
			data.putBoolean("cleave_has_target", false);
			data.putBoolean("cleave_camera_locked", false);
			return;
		}
		// lock camera towards target
		if (data.getBoolean("cleave_camera_locked")) {
			CleaveForcePlayerLookProcedure.execute(entity, target);
		}
		// keep both frozen
		if (entity instanceof LivingEntity living) {
			living.setDeltaMovement(Vec3.ZERO);
		}
		if (target instanceof LivingEntity living) {
			living.setDeltaMovement(Vec3.ZERO);
		}
		// spawn NEW shader-based slash effects around target
		if (world instanceof Level _level) {
			spawnSlashEffect(_level, entity, target);
		}
		timer++;
		data.putDouble("cleave_hold_timer", timer);
		// execute at 20 ticks (1 second)
		if (timer >= 20) {
			CleaveHoldExecuteProcedure.execute(world, entity, target);
		}
	}

	// spawn shader-based slash effect on target
	private static void spawnSlashEffect(Level world, Entity shooter, Entity target) {
		// random slash direction
		double angleH = world.random.nextDouble() * Math.PI * 2;
		double angleV = (world.random.nextDouble() - 0.5) * Math.PI * 0.5;
		Vec3 slashDir = new Vec3(Math.cos(angleH) * Math.cos(angleV), Math.sin(angleV), Math.sin(angleH) * Math.cos(angleV)).normalize();
		double techniquePower = 1.0; // fixed power for cleave hold visuals
		// spawn using new system
		SpawnDismantleSlashProcedure.execute(world, shooter, target, slashDir, techniquePower);
	}
}
