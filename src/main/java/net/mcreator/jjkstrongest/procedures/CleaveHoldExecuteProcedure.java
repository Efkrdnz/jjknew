package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

public class CleaveHoldExecuteProcedure {
	// execute cleave damage and knockback
	public static void execute(LevelAccessor world, Entity entity, Entity target) {
		if (entity == null || target == null)
			return;
		CompoundTag data = entity.getPersistentData();
		if (target instanceof LivingEntity living) {
			if (world instanceof Level _level) {
				DamageSource damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), entity);
				living.hurt(damageSource, 12.0f);
			}
		}
		if (world instanceof ServerLevel server) {
			for (int i = 0; i < 30; i++) {
				double px = target.getX() + (Math.random() - 0.5) * 2;
				double py = target.getY() + Math.random() * 2;
				double pz = target.getZ() + (Math.random() - 0.5) * 2;
				server.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, (Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3, 0);
			}
		}
		Vec3 direction = target.position().subtract(entity.position()).normalize();
		Vec3 knockback = direction.scale(1.5).add(0, 0.5, 0);
		if (target instanceof LivingEntity living) {
			living.setDeltaMovement(knockback);
			living.hurtMarked = true;
		}
		if (entity instanceof LivingEntity living) {
			living.setDeltaMovement(direction.scale(-0.3).add(0, 0.2, 0));
			living.hurtMarked = true;
		}
		if (!world.isClientSide()) {
			world.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5f, 0.7f);
		}
		// unlock camera
		data.putBoolean("cleave_camera_locked", false);
		data.putBoolean("cleave_holding", false);
		data.putBoolean("cleave_has_target", false);
		data.putDouble("cleave_hold_timer", 0);
		data.putString("cleave_target_uuid", "");
	}
}
