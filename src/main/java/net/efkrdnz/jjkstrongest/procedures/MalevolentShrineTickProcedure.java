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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

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
	/**
	 * Fewer, bigger, deliberate. Sixty to eighty a tick was a swarm of streaks nobody could
	 * read — and more than the client would keep, so it evicted them as fast as they came.
	 * A dozen to twenty, each thirty to sixty blocks and oriented with intent, plus a fanned
	 * volley from one point every half second, is a barrage. Live count settles near 220.
	 */
	private static final int BASE_SLASH_COUNT = 12;
	private static final int SLASH_VARIANCE = 9;
	private static final int VOLLEY_INTERVAL = 10;
	private static final int VOLLEY_COUNT = 30;
	private static final double VOLLEY_SPREAD = Math.toRadians(35.0);
	private static final int SLASH_LIFETIME = 14;
	private static final int STRIKE_LIFETIME = 8;
	/** Style codes the client decodes; see MalevolentShrineSlashManager and shrine_cleave.fsh. */
	private static final int STYLE_CLEAVE = 0;
	private static final int STYLE_DISMANTLE = 1;
	private static final int STYLE_STRIKE = 2;
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

		// The clash freezes the shrine's life, exactly as it freezes the Void's.
		if (!isClashing) {
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
		int lifetimeTicks = data.getInt("domainLifetimeTicks");
		// slashes always fire — but filtered to exclude inside UV during clash
		int slashCount = BASE_SLASH_COUNT + world.random.nextInt(SLASH_VARIANCE);
		spawnSlashesViaPackets((ServerLevel) world, owner, x, y, z, slashCount, lifetimeTicks % VOLLEY_INTERVAL == 0, shrine, isClashing);
		// damage every 4 ticks — also filtered during clash
		if (lifetimeTicks % DAMAGE_INTERVAL == 0) {
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

	private static void spawnSlashesViaPackets(ServerLevel world, Entity owner, double centerX, double centerY, double centerZ, int count, boolean volley, Entity domainEntity, boolean isClashing) {
		String domainUUID = domainEntity.getStringUUID();
		// Resolved once per tick. This used to be a fresh 300-block entity scan for
		// every one of the sixty-odd slash candidates, i.e. up to eighty world scans
		// a tick; now the inner loop is pure arithmetic.
		// Not gated on the clash flag: a closed barrier stops what an open domain throws at
		// it because it is a barrier, not because the two are formally locked together.
		DomainUVEntity rival = DomainClashManagerProcedure.rivalVoid(world, domainEntity);
		DomainSphere rivalSphere = rival != null ? rival.volume() : null;
		DomainShell rivalShell = rival != null ? rival.shell() : null;
		// Only a gate now: if nobody can see the domain there is no reason to do any of the
		// slash maths at all. The packets themselves go out as one broadcast per slash.
		if (world.getEntitiesOfClass(ServerPlayer.class, new AABB(centerX - 150, centerY - 150, centerZ - 150, centerX + 150, centerY + 150, centerZ + 150)).isEmpty())
			return;

		// Scattered cuts, oriented with intent. One in ten is a Cleave: white-hot, and bigger.
		for (int i = 0; i < count; i++) {
			Vec3 at = randomInField(world, centerX, centerY, centerZ);
			Vec3 dir = intentDirection(world);
			boolean cleave = world.random.nextInt(100) < 10;
			float length = 30.0f + world.random.nextFloat() * 30.0f;
			float width = 0.6f + world.random.nextFloat() * 0.8f;
			if (cleave) {
				length *= 1.4f;
				width *= 1.6f;
			}
			emitSlash(world, at, dir, length, width, cleave ? STYLE_CLEAVE : STYLE_DISMANTLE, SLASH_LIFETIME, rivalSphere, rivalShell, domainUUID, centerX, centerY, centerZ);
		}

		// The volley: a fan from one point, the barrage that is the shrine's signature.
		if (volley) {
			Vec3 origin = randomInField(world, centerX, centerY, centerZ);
			Vec3 axis = intentDirection(world);
			for (int i = 0; i < VOLLEY_COUNT; i++) {
				Vec3 dir = perturb(world, axis, VOLLEY_SPREAD);
				float length = 30.0f + world.random.nextFloat() * 20.0f;
				// A blade's position is its centre; put its origin at the volley's.
				Vec3 at = origin.add(dir.scale(length * 0.5));
				emitSlash(world, at, dir, length, 0.7f + world.random.nextFloat() * 0.6f, STYLE_DISMANTLE, SLASH_LIFETIME, rivalSphere, rivalShell, domainUUID, centerX, centerY, centerZ);
			}
			SoundEvent swoosh = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:kai"));
			if (swoosh != null)
				world.playSound(null, origin.x, origin.y, origin.z, swoosh, SoundSource.HOSTILE, 1.4f, 0.85f + world.random.nextFloat() * 0.3f);
		}
	}

	/** A point uniformly in the field's upper half-ball, as the slashes have always been placed. */
	private static Vec3 randomInField(ServerLevel world, double cx, double cy, double cz) {
		double angle = world.random.nextDouble() * Math.PI * 2.0;
		double radius = Math.sqrt(world.random.nextDouble()) * RADIUS;
		double ox = Math.cos(angle) * radius;
		double oz = Math.sin(angle) * radius;
		double maxHeight = Math.sqrt(Math.max(0.0, RADIUS_SQ - (ox * ox + oz * oz)));
		return new Vec3(cx + ox, cy + world.random.nextDouble() * maxHeight, cz + oz);
	}

	/**
	 * A direction with intent: half near-horizontal, a third steep diagonal, the rest
	 * vertical, yaw uniform. Random-in-a-ball is what made the old field read as noise.
	 */
	private static Vec3 intentDirection(ServerLevel world) {
		double r = world.random.nextDouble();
		double pitch;
		if (r < 0.5)
			pitch = (world.random.nextDouble() * 2.0 - 1.0) * Math.toRadians(25.0);
		else if (r < 0.8)
			pitch = (world.random.nextBoolean() ? 1.0 : -1.0) * Math.toRadians(40.0 + world.random.nextDouble() * 25.0);
		else
			pitch = (world.random.nextBoolean() ? 1.0 : -1.0) * Math.toRadians(80.0 + world.random.nextDouble() * 10.0);
		double yaw = world.random.nextDouble() * Math.PI * 2.0;
		double c = Math.cos(pitch);
		return new Vec3(c * Math.cos(yaw), Math.sin(pitch), c * Math.sin(yaw));
	}

	/** A unit direction within {@code spread} radians of the axis, uniform in area around it. */
	private static Vec3 perturb(ServerLevel world, Vec3 axis, double spread) {
		Vec3 ref = Math.abs(axis.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
		Vec3 e1 = axis.cross(ref).normalize();
		Vec3 e2 = axis.cross(e1);
		double around = world.random.nextDouble() * Math.PI * 2.0;
		double tilt = Math.sqrt(world.random.nextDouble()) * spread;
		return axis.scale(Math.cos(tilt)).add(e1.scale(Math.sin(tilt) * Math.cos(around))).add(e2.scale(Math.sin(tilt) * Math.sin(around))).normalize();
	}

	/**
	 * One slash to everyone in range, through the rival barrier's clip first. {@code at} is the
	 * blade's centre. A rival barrier stops the slash at its surface rather than letting it cut
	 * through, and what the barrier absorbs it pays for in integrity.
	 *
	 * <p>The packet's roll carries a per-slash jitter now, and its colour fields nothing at
	 * all: the look is decided by style, client side. The codec is unchanged.
	 */
	private static void emitSlash(ServerLevel world, Vec3 at, Vec3 dir, float length, float width, int style, int lifetime, DomainSphere rivalSphere, DomainShell rivalShell, String domainUUID, double cx,
			double cy, double cz) {
		float roll = world.random.nextFloat() * 6.2831853f;
		if (rivalSphere != null) {
			DomainOcclusion.Clip clip = DomainOcclusion.clip(at, dir, roll, length, rivalSphere);
			if (clip.impact() != null && rivalShell != null)
				rivalShell.applyImpact(clip.impact().subtract(rivalSphere.center()), IMPACT_DAMAGE);
			if (clip.blocked())
				return;
			at = clip.position();
			length = (float) clip.length();
		}
		float seed = world.random.nextFloat() * 1000.0f;
		SpawnDomainSlashPacket packet = new SpawnDomainSlashPacket(at.x, at.y, at.z, dir.x, dir.y, dir.z, length, width, style, roll, seed, 1.0f, 1.0f, 1.0f, lifetime, domainUUID);
		DomainSlashNetworkHandler.sendToNearby(world, cx, cy, cz, 150.0, packet);
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
			// The hit, shown as what it is: two or three short blades through the body, drawn
			// in an instant and gone. Not sweep particles.
			if (world instanceof ServerLevel serverLevel) {
				int strikes = 2 + world.random.nextInt(2);
				String domainUUID = domainEntity.getStringUUID();
				for (int i = 0; i < strikes; i++) {
					Vec3 at = new Vec3(target.getX() + (world.random.nextDouble() - 0.5) * target.getBbWidth() * 0.6, target.getY() + target.getBbHeight() * (0.3 + 0.6 * world.random.nextDouble()),
							target.getZ() + (world.random.nextDouble() - 0.5) * target.getBbWidth() * 0.6);
					float length = 3.0f + target.getBbHeight() * 2.0f;
					emitSlash(serverLevel, at, intentDirection(serverLevel), length, 0.5f, STYLE_STRIKE, STRIKE_LIFETIME, null, null, domainUUID, centerX, centerY, centerZ);
				}
			}
			Vec3 originalVelocity = target.getDeltaMovement();
			target.invulnerableTime = 0;
			target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cleave"))), owner), 2.0f);
			target.setDeltaMovement(originalVelocity);
		}
	}
}
