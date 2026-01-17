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

import java.util.List;

public class MeleeBarrageProcedure {
	// executes rapid punches
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		CompoundTag nbt = entity.getPersistentData();
		// initialize barrage counter
		if (!nbt.contains("barrage_count")) {
			nbt.putInt("barrage_count", 0);
		}
		int barrageCount = nbt.getInt("barrage_count");
		// barrage consists of 8 rapid hits
		if (barrageCount >= 8) {
			nbt.putInt("barrage_count", 0);
			return;
		}
		double range = 3.5;
		Vec3 lookVec = entity.getLookAngle();
		Vec3 startPos = entity.getEyePosition(1f);
		Vec3 endPos = startPos.add(lookVec.scale(range));
		AABB searchBox = new AABB(startPos, endPos).inflate(1.2);
		List<Entity> targets = world.getEntitiesOfClass(Entity.class, searchBox, e -> e != entity && e instanceof LivingEntity && e.isAlive());
		boolean isInAir = !entity.onGround();
		boolean isFinalHit = (barrageCount == 7);
		boolean hitTarget = false;
		for (Entity target : targets) {
			// damage target
			if (world instanceof ServerLevel serverLevel) {
				target.hurt(new DamageSource(serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity), 3f);
			}
			// keep target in air during air barrage
			if (isInAir && !isFinalHit) {
				Vec3 targetVel = target.getDeltaMovement();
				target.setDeltaMovement(targetVel.x, 0.05, targetVel.z);
				target.hurtMarked = true;
			}
			// apply knockback on final hit
			if (isFinalHit) {
				Vec3 knockback = lookVec.multiply(1.5, 0.4, 1.5);
				target.setDeltaMovement(target.getDeltaMovement().add(knockback));
				target.hurtMarked = true;
			}
			hitTarget = true;
		}
		if (hitTarget) {
			// keep player in air during air barrage
			if (isInAir && !isFinalHit) {
				Vec3 currentVel = entity.getDeltaMovement();
				entity.setDeltaMovement(currentVel.x, 0.05, currentVel.z);
				entity.hurtMarked = true;
			}
			// increment barrage counter
			nbt.putInt("barrage_count", barrageCount + 1);
		}
	}
}
