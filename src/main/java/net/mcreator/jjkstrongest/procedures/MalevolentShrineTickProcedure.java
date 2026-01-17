package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

import java.util.UUID;
import java.util.List;
import java.util.Comparator;

public class MalevolentShrineTickProcedure {
	// main tick logic for malevolent shrine domain
	public static void execute(Level world, double x, double y, double z, Entity domainEntity) {
		if (world == null || domainEntity == null || world.isClientSide())
			return;
		// get domain data
		int lifetimeTicks = domainEntity.getPersistentData().getInt("domainLifetimeTicks");
		String ownerUUIDStr = domainEntity.getPersistentData().getString("ownerUUID");
		// increment lifetime
		lifetimeTicks++;
		domainEntity.getPersistentData().putInt("domainLifetimeTicks", lifetimeTicks);
		// check if domain should collapse (20 seconds = 400 ticks)
		if (lifetimeTicks >= 400) {
			domainEntity.discard();
			return;
		}
		// get owner player
		Entity owner = null;
		if (!ownerUUIDStr.isEmpty()) {
			try {
				UUID ownerUUID = UUID.fromString(ownerUUIDStr);
				if (world instanceof ServerLevel serverLevel) {
					owner = serverLevel.getEntity(ownerUUID);
				}
			} catch (Exception e) {
			}
		}
		// check if owner is out of range (force collapse)
		if (owner != null) {
			double distSq = owner.distanceToSqr(x, y, z);
			if (distSq > 100 * 100) {
				domainEntity.discard();
				return;
			}
		} else {
			// owner not found - collapse domain
			domainEntity.discard();
			return;
		}
		// after 2 second delay (40 ticks), start domain effects
		if (lifetimeTicks < 40) {
			return;
		}
		// spawn wild slashes everywhere (80 random slashes per tick)
		spawnWildSlashes(world, owner, x, y, z);
		// damage entities every 4 ticks
		if (lifetimeTicks % 4 == 0) {
			damageEntitiesInDomain(world, owner, x, y, z);
		}
	}

	// spawn 80 massive random slashes throughout the domain hemisphere
	private static void spawnWildSlashes(Level world, Entity owner, double centerX, double centerY, double centerZ) {
		if (world == null || owner == null)
			return;
		for (int i = 0; i < 110; i++) {
			// random position within 100 block radius hemisphere
			double angle = world.random.nextDouble() * Math.PI * 2;
			double radius = Math.sqrt(world.random.nextDouble()) * 100;
			double offsetX = Math.cos(angle) * radius;
			double offsetZ = Math.sin(angle) * radius;
			// random height within hemisphere
			double horizontalDistSq = offsetX * offsetX + offsetZ * offsetZ;
			double maxHeight = Math.sqrt(Math.max(0, 100 * 100 - horizontalDistSq));
			double offsetY = world.random.nextDouble() * maxHeight;
			double slashX = centerX + offsetX;
			double slashY = centerY + offsetY;
			double slashZ = centerZ + offsetZ;
			// random direction for slash
			Vec3 randomDir = new Vec3(world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5).normalize();
			// spawn massive domain slash using custom procedure
			SpawnMalevolentShrineSlashProcedure.execute(world, owner, slashX, slashY, slashZ, randomDir);
		}
	}

	// damage all entities in domain every 4 ticks (full sphere, no knockback)
	private static void damageEntitiesInDomain(Level world, Entity owner, double centerX, double centerY, double centerZ) {
		if (world == null || owner == null)
			return;
		// get all entities within 100 block radius
		AABB boundingBox = new AABB(centerX - 100, centerY - 100, centerZ - 100, centerX + 100, centerY + 100, centerZ + 100);
		List<Entity> entities = world.getEntitiesOfClass(Entity.class, boundingBox, e -> true).stream().sorted(new Object() {
			Comparator<Entity> compareDistOf(double x, double y, double z) {
				return Comparator.comparingDouble(ent -> ent.distanceToSqr(x, y, z));
			}
		}.compareDistOf(centerX, centerY, centerZ)).toList();
		// damage each entity
		for (Entity target : entities) {
			if (target == owner || target == owner.getVehicle() || target.isPassengerOfSameVehicle(owner))
				continue;
			if (!(target instanceof LivingEntity))
				continue;
			// check if within full 100 block sphere (3D distance)
			double dx = target.getX() - centerX;
			double dy = target.getY() - centerY;
			double dz = target.getZ() - centerZ;
			double dist3D = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (dist3D > 100)
				continue;
			// spawn 3-5 slash effects on the entity getting hit
			int slashCount = 3 + world.random.nextInt(3);
			for (int i = 0; i < slashCount; i++) {
				// random position around entity
				double offsetX = (world.random.nextDouble() - 0.5) * target.getBbWidth() * 2;
				double offsetY = world.random.nextDouble() * target.getBbHeight();
				double offsetZ = (world.random.nextDouble() - 0.5) * target.getBbWidth() * 2;
				double slashX = target.getX() + offsetX;
				double slashY = target.getY() + offsetY;
				double slashZ = target.getZ() + offsetZ;
				// random slash direction
				Vec3 randomDir = new Vec3(world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5).normalize();
				// spawn slash effect
				//TestDismantleRightclickedProcedure.execute(world, target);
				if (world instanceof ServerLevel _level)
					_level.sendParticles(ParticleTypes.SWEEP_ATTACK, slashX, slashY, slashZ, 2, 0.1, 0.4, 0.1, 1);
			}
			// disable invulnerability frames and knockback
			target.invulnerableTime = 0;
			Vec3 originalDeltaMovement = target.getDeltaMovement();
			// deal jujutsu damage from owner
			target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), owner), 2.0f);
			// restore original velocity (no knockback)
			target.setDeltaMovement(originalDeltaMovement);
		}
	}
}
