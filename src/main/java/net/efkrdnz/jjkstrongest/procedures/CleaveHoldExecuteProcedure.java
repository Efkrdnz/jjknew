package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

public class CleaveHoldExecuteProcedure {
	// cleaves all living entities in a 10 block cone in front
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		// damage must be server side
		if (world instanceof Level lvl && lvl.isClientSide())
			return;
		final double range = 10.0;
		final double dotThreshold = 0.35; // lower = wider cone
		final double outputMultiplier = ReturnOutputGeneralProcedure.execute(entity.level(), entity);
		final double output = outputMultiplier + 1;
		final float damage = 12.0f * (float)output;
		Vec3 origin = entity.getEyePosition();
		Vec3 look = entity.getLookAngle().normalize();
		AABB box = new AABB(entity.getX() - range, entity.getY() - range, entity.getZ() - range, entity.getX() + range, entity.getY() + range, entity.getZ() + range);
		List<LivingEntity> targets = entity.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != entity && e.isAlive());
		DamageSource damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cleave"))), entity);
		for (LivingEntity living : targets) {
			Vec3 targetPos = living.position().add(0.0, living.getBbHeight() * 0.5, 0.0);
			Vec3 toTarget = targetPos.subtract(origin);
			double dist = toTarget.length();
			if (dist > range || dist < 0.001)
				continue;
			double dot = toTarget.normalize().dot(look);
			if (dot < dotThreshold)
				continue;
			living.hurt(damageSource, damage);
			// small knock + feedback
			Vec3 kb = look.scale(0.8).add(0, 0.15, 0);
			living.setDeltaMovement(living.getDeltaMovement().add(kb));
			living.hurtMarked = true;
			if (world instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.SWEEP_ATTACK, living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(), 1, 0.2, 0.2, 0.2, 0);
			}
		}
		// sound at execute
		if (!world.isClientSide()) {
			world.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.7f);
		}
	}
}
