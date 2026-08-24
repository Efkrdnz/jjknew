package net.efkrdnz.jjkstrongest.procedures;


import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

import net.efkrdnz.jjkstrongest.network.SpawnDomainSlashPacket;
import net.efkrdnz.jjkstrongest.network.DomainSlashNetworkHandler;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.UUID;
import java.util.List;

public class MalevolentShrineTickProcedure {
	private static final int MAX_LIFETIME = 600;
	private static final int ABSOLUTE_MAX_LIFETIME = 1200;
	private static final int STARTUP_DELAY = 40;
	private static final double RADIUS = 100.0;
	private static final double RADIUS_SQ = RADIUS * RADIUS;
	private static final int DAMAGE_INTERVAL = 4;
	private static final int OWNER_CHECK_INTERVAL = 20;
	private static final int BASE_SLASH_COUNT = 60;
	private static final int SLASH_VARIANCE = 20;

	public static void execute(Level world, double x, double y, double z, Entity domainEntity) {
		if (world == null || domainEntity == null || world.isClientSide())
			return;
		CompoundTag data = domainEntity.getPersistentData();
		int absoluteTicks = data.getInt("domainAbsoluteTicks") + 1;
		data.putInt("domainAbsoluteTicks", absoluteTicks);
		if (absoluteTicks >= ABSOLUTE_MAX_LIFETIME) {
			domainEntity.discard();
			return;
		}
		boolean isClashing = data.getBoolean("isClashing");
		if (isClashing) {
			isClashing = reconcileClashState((ServerLevel) world, domainEntity, data);
		}
		// freeze lifetime counter during clash
		if (!isClashing) {
			int lifetimeTicks = data.getInt("domainLifetimeTicks");
			lifetimeTicks++;
			data.putInt("domainLifetimeTicks", lifetimeTicks);
			// check natural collapse
			if (lifetimeTicks >= MAX_LIFETIME) {
				domainEntity.discard();
				return;
			}
			// check owner every 20 ticks
			if (lifetimeTicks % OWNER_CHECK_INTERVAL == 0) {
				if (!validateOwner(world, data, x, y, z)) {
					domainEntity.discard();
					return;
				}
			}
		}
		int lifetimeTicks = data.getInt("domainLifetimeTicks");
		// wait for startup regardless of clash state
		if (lifetimeTicks < STARTUP_DELAY)
			return;
		Entity owner = getOwner(world, data);
		if (owner == null) {
			domainEntity.discard();
			return;
		}
		// slashes always fire — but filtered to exclude inside UV during clash
		int slashCount = BASE_SLASH_COUNT + world.random.nextInt(SLASH_VARIANCE);
		spawnSlashesViaPackets((ServerLevel) world, owner, x, y, z, slashCount, domainEntity.getStringUUID(), isClashing);
		// damage every 4 ticks — also filtered during clash
		if (lifetimeTicks % DAMAGE_INTERVAL == 0) {
			damageEntitiesOptimized(world, owner, x, y, z, isClashing);
		}
	}

	private static boolean validateOwner(Level world, CompoundTag data, double x, double y, double z) {
		String ownerUUIDStr = data.getString("ownerUUID");
		if (ownerUUIDStr.isEmpty())
			return false;
		try {
			UUID ownerUUID = UUID.fromString(ownerUUIDStr);
			if (world instanceof ServerLevel serverLevel) {
				Entity owner = serverLevel.getEntity(ownerUUID);
				if (owner == null || !owner.isAlive())
					return false;
				if (owner instanceof Player player && player.isSpectator())
					return false;
				return owner.distanceToSqr(x, y, z) <= RADIUS_SQ;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private static Entity getOwner(Level world, CompoundTag data) {
		String ownerUUIDStr = data.getString("ownerUUID");
		if (ownerUUIDStr.isEmpty())
			return null;
		try {
			UUID ownerUUID = UUID.fromString(ownerUUIDStr);
			if (world instanceof ServerLevel serverLevel)
				return serverLevel.getEntity(ownerUUID);
		} catch (Exception e) {
		}
		return null;
	}

	private static boolean reconcileClashState(ServerLevel level, Entity domainEntity, CompoundTag data) {
		String rivalUUIDStr = data.getString("rivalUUID");
		boolean rivalAlive = false;
		if (!rivalUUIDStr.isEmpty()) {
			try {
				Entity rival = level.getEntity(UUID.fromString(rivalUUIDStr));
				rivalAlive = rival instanceof DomainUVEntity && rival.isAlive();
			} catch (Exception ignored) {
				rivalAlive = false;
			}
		}
		if (rivalAlive) {
			data.putInt("clashLostTicks", 0);
			return true;
		}
		int graceTicks = data.getInt("clashLostTicks") + 1;
		data.putInt("clashLostTicks", graceTicks);
		if (graceTicks < 40) {
			return true;
		}
		data.putBoolean("isClashing", false);
		data.putInt("clashLostTicks", 0);
		data.remove("rivalUUID");
		data.remove("shrineClashHP");
		return false;
	}

	private static void spawnSlashesViaPackets(ServerLevel world, Entity owner, double centerX, double centerY, double centerZ, int count, String domainUUID, boolean isClashing) {
		double radiusSq = RADIUS * RADIUS;
		double twoPI = Math.PI * 2;
		List<ServerPlayer> nearbyPlayers = world.getEntitiesOfClass(ServerPlayer.class, new AABB(centerX - 150, centerY - 150, centerZ - 150, centerX + 150, centerY + 150, centerZ + 150));
		if (nearbyPlayers.isEmpty())
			return;
		for (int i = 0; i < count; i++) {
			double angle = world.random.nextDouble() * twoPI;
			double radius = Math.sqrt(world.random.nextDouble()) * RADIUS;
			double offsetX = Math.cos(angle) * radius;
			double offsetZ = Math.sin(angle) * radius;
			double horizontalDistSq = offsetX * offsetX + offsetZ * offsetZ;
			double maxHeight = Math.sqrt(Math.max(0, radiusSq - horizontalDistSq));
			double offsetY = world.random.nextDouble() * maxHeight;
			double slashX = centerX + offsetX;
			double slashY = centerY + offsetY;
			double slashZ = centerZ + offsetZ;
			// during clash skip slash positions that are inside UV barrier
			if (isClashing && DomainClashManagerProcedure.isPosInsideUV(world, slashX, slashY, slashZ))
				continue;
			Vec3 randomDir = new Vec3(world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5).normalize();
			int styleRoll = world.random.nextInt(100);
			int style = styleRoll < 30 ? 0 : 1;
			float length = 25.0f + world.random.nextFloat() * 10.0f;
			float width = 1.5f + world.random.nextFloat() * 1.5f;
			float roll = world.random.nextFloat() * 6.2831853f;
			float seed = world.random.nextFloat() * 1000.0f;
			float r, g, b;
			if (style == 0) {
				r = g = b = 1.0f;
			} else {
				r = 1.0f;
				g = 0.1f + world.random.nextFloat() * 0.15f;
				b = 0.1f + world.random.nextFloat() * 0.15f;
			}
			SpawnDomainSlashPacket packet = new SpawnDomainSlashPacket(slashX, slashY, slashZ, randomDir.x, randomDir.y, randomDir.z, length, width, style, roll, seed, r, g, b, 12, domainUUID);
			for (ServerPlayer player : nearbyPlayers)
				DomainSlashNetworkHandler.sendToPlayer(player, packet);
		}
	}

	private static void damageEntitiesOptimized(Level world, Entity owner, double centerX, double centerY, double centerZ, boolean isClashing) {
		if (world == null || owner == null)
			return;
		AABB boundingBox = new AABB(centerX - RADIUS, centerY - RADIUS, centerZ - RADIUS, centerX + RADIUS, centerY + RADIUS, centerZ + RADIUS);
		List<Entity> entities = world.getEntitiesOfClass(Entity.class, boundingBox, e -> e instanceof LivingEntity && e != owner && !e.isPassengerOfSameVehicle(owner));
		double radiusSq = RADIUS * RADIUS;
		for (Entity target : entities) {
			if (target instanceof Player player && (player.isCreative() || player.isSpectator()))
				continue;
			double dx = target.getX() - centerX;
			double dy = target.getY() - centerY;
			double dz = target.getZ() - centerZ;
			if (dx * dx + dy * dy + dz * dz > radiusSq)
				continue;
			// during clash: skip entities inside UV's barrier — they're protected
			if (isClashing && world instanceof ServerLevel serverLevel) {
				if (DomainClashManagerProcedure.isPosInsideUV(serverLevel, target.getX(), target.getY(), target.getZ()))
					continue;
			}
			int slashCount = 2 + world.random.nextInt(2);
			if (world instanceof ServerLevel serverLevel) {
				for (int i = 0; i < slashCount; i++) {
					double offsetX = (world.random.nextDouble() - 0.5) * target.getBbWidth();
					double offsetY2 = world.random.nextDouble() * target.getBbHeight();
					double offsetZ = (world.random.nextDouble() - 0.5) * target.getBbWidth();
					serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX() + offsetX, target.getY() + offsetY2, target.getZ() + offsetZ, 2, 0.1, 0.4, 0.1, 1);
				}
			}
			Vec3 originalVelocity = target.getDeltaMovement();
			target.invulnerableTime = 0;
			target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cleave"))), owner), 2.0f);
			target.setDeltaMovement(originalVelocity);
		}
	}
}
