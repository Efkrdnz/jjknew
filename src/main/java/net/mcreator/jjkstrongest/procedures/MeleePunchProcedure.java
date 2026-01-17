package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleTypes;

import net.mcreator.jjkstrongest.init.JjkStrongestModParticleTypes;

import java.util.List;

public class MeleePunchProcedure {
	// executes melee punch with knockback in look direction
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		double range = 3.5;
		Vec3 lookVec = entity.getLookAngle();
		Vec3 startPos = entity.getEyePosition(1f);
		Vec3 endPos = startPos.add(lookVec.scale(range));
		// find all entities in front of player
		AABB searchBox = new AABB(startPos, endPos).inflate(1.0);
		List<Entity> targets = world.getEntitiesOfClass(Entity.class, searchBox, e -> e != entity && e instanceof LivingEntity && e.isAlive());
		boolean hitSomething = false;
		// hand swing
		if (entity instanceof LivingEntity _entity)
			_entity.swing(InteractionHand.MAIN_HAND, true);
		for (Entity target : targets) {
			// damage target
			if (world instanceof ServerLevel serverLevel) {
				target.hurt(new DamageSource(serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity), 6f);
			}
			// apply knockback
			Vec3 knockback = lookVec.multiply(1.2, 0.3, 1.2);
			target.setDeltaMovement(target.getDeltaMovement().add(knockback));
			target.hurtMarked = true;
			// impact particles on target
			if (world instanceof ServerLevel serverLevel) {
				Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
				serverLevel.sendParticles(ParticleTypes.CRIT, targetPos.x, targetPos.y, targetPos.z, 8, 0.3, 0.3, 0.3, 0.1);
				serverLevel.sendParticles((SimpleParticleType) (JjkStrongestModParticleTypes.PUNCH_IMPACT.get()), targetPos.x, targetPos.y, targetPos.z, 1, 0, 0, 0, 0);
			}
			hitSomething = true;
		}
		// swing trail particles even if missed
		if (world instanceof ServerLevel serverLevel) {
			Vec3 perpendicular = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
			for (int i = 0; i < 5; i++) {
				double progress = i / 4.0;
				Vec3 trailPos = startPos.add(lookVec.scale(1.5 * progress));
				Vec3 offset = perpendicular.scale(0.3 * Math.sin(progress * Math.PI));
				serverLevel.sendParticles(ParticleTypes.CLOUD, trailPos.x + offset.x, trailPos.y + offset.y, trailPos.z + offset.z, 1, 0, 0, 0, 0);
			}
		}
	}
}
