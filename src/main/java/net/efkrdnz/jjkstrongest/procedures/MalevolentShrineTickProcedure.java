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
import net.efkrdnz.jjkstrongest.domain.DomainOcclusion;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.domain.DomainDefinition;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import java.util.UUID;
import java.util.List;

public class MalevolentShrineTickProcedure {
	/**
	 * The shrine's mechanics, from the same table the Void's come from.
	 *
	 * <p>MAX_LIFETIME, ABSOLUTE_MAX_LIFETIME and STARTUP_DELAY used to live here as three
	 * private constants, and the shrine had no phases at all: it counted ticks, crossed a
	 * threshold, and started cutting. It runs the same four phases the closed domain does
	 * now, off the same definition, so "what phase is this domain in" has one answer
	 * whichever kind you are holding.
	 */
	private static final DomainDefinition DEFINITION = DomainDefinition.MALEVOLENT_SHRINE;
	// Single source of truth for the shrine's reach, shared with its DomainSource volume.
	private static final double RADIUS = MalevolentShrineEntity.FIELD_RADIUS;
	private static final double RADIUS_SQ = RADIUS * RADIUS;
	private static final int DAMAGE_INTERVAL = 4;
	private static final int OWNER_CHECK_INTERVAL = 20;
	private static final int BASE_SLASH_COUNT = 60;
	private static final int SLASH_VARIANCE = 20;
	/** What one slash stopped by a barrier costs that barrier. Small — pressure does the real work. */
	private static final float IMPACT_DAMAGE = 0.35f;

	public static void execute(Level world, double x, double y, double z, Entity domainEntity) {
		if (world == null || !(domainEntity instanceof MalevolentShrineEntity shrine) || world.isClientSide())
			return;
		CompoundTag data = shrine.getPersistentData();
		int absoluteTicks = data.getInt("domainAbsoluteTicks") + 1;
		data.putInt("domainAbsoluteTicks", absoluteTicks);
		if (absoluteTicks >= DEFINITION.maxLifetimeTicks() && shrine.phase() != DomainPhase.COLLAPSING)
			beginCollapse(shrine);

		boolean isClashing = data.getBoolean("isClashing");
		if (isClashing) {
			isClashing = reconcileClashState((ServerLevel) world, shrine, data);
		}

		if (shrine.phase() == DomainPhase.COLLAPSING) {
			tickCollapsing(shrine, data);
			return;
		}

		// The clash freezes the shrine's hostile life, exactly as it freezes the Void's — but
		// only once it HAS one. A clash beginning while the shrine was still opening used to
		// pin it in EXPANDING for the whole clash, and the guard below then killed the rest of
		// its tick every time: no slashes, no damage, no carve, for as long as the rival stood
		// there. Letting the counter run until it is hostile costs the clash nothing.
		if (!isClashing || shrine.phase() != DomainPhase.ACTIVE) {
			int lifetimeTicks = data.getInt("domainLifetimeTicks") + 1;
			data.putInt("domainLifetimeTicks", lifetimeTicks);
			if (lifetimeTicks % OWNER_CHECK_INTERVAL == 0 && !validateOwner(world, data, x, y, z)) {
				beginCollapse(shrine);
				return;
			}
			if (!advancePhase(shrine, lifetimeTicks))
				return;
		}

		if (shrine.phase() != DomainPhase.ACTIVE)
			return;

		Entity owner = getOwner(world, data);
		if (owner == null) {
			beginCollapse(shrine);
			return;
		}
		// slashes always fire — but filtered to exclude inside UV during clash
		int slashCount = BASE_SLASH_COUNT + world.random.nextInt(SLASH_VARIANCE);
		spawnSlashesViaPackets((ServerLevel) world, owner, x, y, z, slashCount, shrine, isClashing);
		// Damage every four ticks, off the entity's own clock rather than the lifetime
		// counter. The lifetime counter freezes during a clash, so this modulo was a CONSTANT
		// for the whole clash: a one-in-four chance the shrine cut everyone every single tick,
		// and a three-in-four chance it cut nobody at all, decided by whichever tick the rival
		// happened to come into range on. The slashes kept drawing either way, so it looked
		// fully active while dealing nothing.
		if (shrine.tickCount % DAMAGE_INTERVAL == 0) {
			damageEntitiesOptimized(world, owner, x, y, z, isClashing, shrine);
		}
	}

	/**
	 * Walks the shrine through opening and running out.
	 *
	 * <p>The carve radius is set here and synced, rather than each side counting its own
	 * ticks and hoping to agree.
	 *
	 * @return false if the shrine has nothing more to do this tick
	 */
	private static boolean advancePhase(MalevolentShrineEntity shrine, int lifetimeTicks) {
		int expansion = Math.max(1, DEFINITION.expansionTicks());
		if (lifetimeTicks <= expansion) {
			shrine.setPhase(DomainPhase.EXPANDING);
			shrine.setPhaseProgress((float) lifetimeTicks / expansion);
			return false;
		}
		int active = lifetimeTicks - expansion;
		if (active >= DEFINITION.durationTicks()) {
			beginCollapse(shrine);
			return false;
		}
		shrine.setPhase(DomainPhase.ACTIVE);
		shrine.setPhaseProgress((float) active / Math.max(1, DEFINITION.durationTicks()));
		// The ground opens once the domain is hostile and keeps creeping outward, one block
		// every four ticks — the cadence the block-breaking pass has always run at. It used
		// to be a separate counter maintained on both sides; it is set here and synced now.
		if (active % 4 == 0)
			shrine.setCarveRadius(shrine.getCarveRadius() + 1.0f);
		return true;
	}

	/** Puts the shrine into its shutdown phase. Safe to call more than once. */
	public static void beginCollapse(MalevolentShrineEntity shrine) {
		if (shrine.phase() == DomainPhase.COLLAPSING)
			return;
		shrine.setPhase(DomainPhase.COLLAPSING);
		shrine.setPhaseProgress(0.0f);
		shrine.getPersistentData().putInt("collapseTick", 0);
	}

	/**
	 * An open domain has no terrain to put back, so closing it is only a fade — but it is
	 * a fade rather than the entity vanishing between one tick and the next, which is what
	 * {@code discard()} straight out of the running state used to be.
	 */
	private static void tickCollapsing(MalevolentShrineEntity shrine, CompoundTag data) {
		int tick = data.getInt("collapseTick") + 1;
		data.putInt("collapseTick", tick);
		int collapse = Math.max(1, DEFINITION.collapseTicks());
		shrine.setPhaseProgress(Math.min(1.0f, (float) tick / collapse));
		if (tick >= collapse)
			shrine.discard();
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

	private static boolean reconcileClashState(ServerLevel level, MalevolentShrineEntity domainEntity, CompoundTag data) {
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
		if (graceTicks < DomainClashManagerProcedure.CLASH_END_GRACE_TICKS) {
			return true;
		}
		data.putBoolean("isClashing", false);
		data.putInt("clashLostTicks", 0);
		data.remove("rivalUUID");
		data.remove("shrineClashHP");
		return false;
	}

	private static void spawnSlashesViaPackets(ServerLevel world, Entity owner, double centerX, double centerY, double centerZ, int count, Entity domainEntity, boolean isClashing) {
		String domainUUID = domainEntity.getStringUUID();
		// Resolved once per tick. This used to be a fresh 300-block entity scan for
		// every one of the sixty-odd slash candidates, i.e. up to eighty world scans
		// a tick; now the inner loop is pure arithmetic.
		// Not gated on the clash flag: a closed barrier stops what an open domain throws at
		// it because it is a barrier, not because the two are formally locked together.
		DomainUVEntity rival = DomainClashManagerProcedure.rivalVoid(world, domainEntity);
		DomainSphere rivalSphere = rival != null ? rival.volume() : null;
		DomainShell rivalShell = rival != null ? rival.shell() : null;
		double radiusSq = RADIUS * RADIUS;
		double twoPI = Math.PI * 2;
		// Only a gate now: if nobody can see the domain there is no reason to do any of the
		// slash maths at all. The packets themselves go out as one broadcast per slash.
		if (world.getEntitiesOfClass(ServerPlayer.class, new AABB(centerX - 150, centerY - 150, centerZ - 150, centerX + 150, centerY + 150, centerZ + 150)).isEmpty())
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
			Vec3 randomDir = new Vec3(world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5).normalize();
			int styleRoll = world.random.nextInt(100);
			int style = styleRoll < 30 ? 0 : 1;
			float length = 25.0f + world.random.nextFloat() * 10.0f;
			float width = 1.5f + world.random.nextFloat() * 1.5f;
			float roll = world.random.nextFloat() * 6.2831853f;
			// A rival barrier stops the slash at its surface rather than letting it cut
			// through. What the barrier absorbs, it pays for in integrity.
			if (rivalSphere != null) {
				DomainOcclusion.Clip clip = DomainOcclusion.clip(new Vec3(slashX, slashY, slashZ), randomDir, roll, length, rivalSphere);
				if (clip.impact() != null && rivalShell != null)
					rivalShell.applyImpact(clip.impact().subtract(rivalSphere.center()), IMPACT_DAMAGE);
				if (clip.blocked())
					continue;
				slashX = clip.position().x;
				slashY = clip.position().y;
				slashZ = clip.position().z;
				length = (float) clip.length();
			}
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
			DomainSlashNetworkHandler.sendToNearby(world, centerX, centerY, centerZ, 150.0, packet);
		}
	}

	private static void damageEntitiesOptimized(Level world, Entity owner, double centerX, double centerY, double centerZ, boolean isClashing, Entity domainEntity) {
		if (world == null || owner == null)
			return;
		DomainUVEntity rival = isClashing && world instanceof ServerLevel srv ? DomainClashManagerProcedure.rivalVoid(srv, domainEntity) : null;
		DomainSphere rivalSphere = rival != null ? rival.volume() : null;
		// A holed barrier is no longer cover. Rather than raycast every target against
		// every breach, protection simply fades as the shell fails — which is also how it
		// reads: the domain stops shielding you because it is coming apart.
		DomainShell rivalShell = rival != null ? rival.shell() : null;
		float shelter = rivalShell == null ? 1.0f : Math.max(0.0f, Math.min(1.0f, rivalShell.totalIntegrity() * 1.25f));
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
			if (rivalSphere != null && rivalSphere.contains(target.getX(), target.getY(), target.getZ()))
				continue;
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
