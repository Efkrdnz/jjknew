package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;

import java.util.List;

public class MeleeUppercutProcedure {
	// executes uppercut launching player and target upward
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double range = 3.0;
		Vec3 lookVec = entity.getLookAngle();
		Vec3 startPos = entity.getEyePosition(1f);
		Vec3 endPos = startPos.add(lookVec.scale(range));
		AABB searchBox = new AABB(startPos, endPos).inflate(1.0);
		List<Entity> targets = world.getEntitiesOfClass(Entity.class, searchBox, e -> e != entity && e instanceof LivingEntity && e.isAlive());
		boolean hitTarget = false;
		for (Entity target : targets) {
			// damage target
			if (world instanceof ServerLevel serverLevel) {
				target.hurt(new DamageSource(serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity), 8f);
			}
			// launch target upward with forward momentum
			Vec3 launchVec = new Vec3(lookVec.x * 0.6, 1.2, lookVec.z * 0.6);
			target.setDeltaMovement(launchVec);
			target.hurtMarked = true;
			hitTarget = true;
		}
		// launch player if hit something
		if (hitTarget) {
			Vec3 launchVec = new Vec3(lookVec.x * 0.6, 1.2, lookVec.z * 0.6);
			entity.setDeltaMovement(launchVec);
			entity.hurtMarked = true;
		}
	}
}
