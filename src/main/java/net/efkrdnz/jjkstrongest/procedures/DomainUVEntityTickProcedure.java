package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.domain.DomainCarve;
import net.efkrdnz.jjkstrongest.domain.DomainDefinition;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.network.DomainShellSyncPacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/**
 * Drives an Unlimited Void domain through its life.
 *
 * <p>The shape lives in the entity's synced data now, so the client sees the same
 * radius and phase the server is acting on. What used to be three booleans that could
 * contradict each other is a single {@link DomainPhase}, and what used to be a voxel
 * shell of barrier blocks is an analytic sphere plus a budgeted carve.
 */
public class DomainUVEntityTickProcedure {

	/**
	 * Every timing this machine runs on now comes from the domain being ticked, not from
	 * constants private to this file. That is what makes the phase machine reusable: a
	 * second technique is a {@link DomainDefinition} and a renderer, not a second copy of
	 * everything below.
	 */
	/** How much of the expansion the visible wall takes to reach full size. */
	private static final float WALL_GROW_FRACTION = 0.3f;

	private static DomainDefinition def(DomainUVEntity domain) {
		return domain.definition();
	}

	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof DomainUVEntity domain) || !(world instanceof ServerLevel level))
			return;

		CompoundTag data = domain.getPersistentData();
		if (data.contains("storedBlocks")) {
			// A domain saved before the shell became analytic. Put its blocks back the
			// old way and let it go; the player can recast into the new system.
			restoreLegacyBlocks(level, data);
			domain.discard();
			return;
		}
		if (!data.contains("ownerUUID")) {
			domain.discard();
			return;
		}

		int absoluteTicks = data.getInt("domainAbsoluteTicks") + 1;
		data.putInt("domainAbsoluteTicks", absoluteTicks);

		DomainPhase phase = domain.getPhase();

		if (phase != DomainPhase.COLLAPSING) {
			if (absoluteTicks >= def(domain).maxLifetimeTicks() || shouldCollapseDueToCaster(level, domain)) {
				beginCollapse(domain);
				phase = DomainPhase.COLLAPSING;
			}
		}

		boolean clashing = false;
		if (phase != DomainPhase.COLLAPSING) {
			clashing = DomainClashManagerProcedure.detectAndRunClash(level, domain);
			if (!domain.isAlive())
				return;
			// detectAndRunClash can lose the clash outright, which asks for a collapse
			phase = domain.getPhase();
		}

		if (phase.isSealed() && !tickShell(domain, data)) {
			beginCollapse(domain);
			phase = DomainPhase.COLLAPSING;
		}

		switch (phase) {
			case EXPANDING -> tickExpanding(level, domain, data);
			case SETTLING -> DomainUVPostLinesPhaseProcedure.execute(level, domain, def(domain).settleTicks());
			case ACTIVE -> tickActive(level, domain, data, clashing);
			case COLLAPSING -> tickCollapsing(level, domain, data);
		}
	}

	private static void tickExpanding(ServerLevel level, DomainUVEntity domain, CompoundTag data) {
		int tick = data.getInt("expansionTick") + 1;
		data.putInt("expansionTick", tick);

		// The carve takes the ground under everything in the ball, so anything standing
		// below the plane when the domain opens has to be put on it first, once. Done here
		// against the full-size sphere rather than the growing shell: the carve is going to
		// reach that far whatever the wall is doing this tick.
		if (!data.getBoolean("lifted")) {
			liftOntoFloor(level, domain, fullSphere(domain), 0.0);
			data.putBoolean("lifted", true);
		}

		float progress = Math.min(1.0f, (float) tick / def(domain).expansionTicks());
		// The wall reaches full size in the first third of the phase, not at the end of it.
		// Collision is already at full size from tick one (the domain is sealed the moment it
		// is cast), so a wall that crawled outward for two seconds left a widening gap between
		// where the room ended and where it looked like it ended. Ease-out, so it slams.
		float grow = Math.min(1.0f, progress / WALL_GROW_FRACTION);
		float eased = 1.0f - (1.0f - grow) * (1.0f - grow);
		domain.setShellRadius(domain.getTargetRadius() * eased);
		domain.setPhaseProgress(progress);

		boolean carved = DomainCarve.advanceCarve(level, domain, domain.sphere());
		if (progress >= 1.0f && carved) {
			domain.setShellRadius(domain.getTargetRadius());
			domain.setPhase(DomainPhase.SETTLING);
			domain.setPhaseProgress(0.0f);
			data.putInt("postTick", 0);
		}
	}

	private static void tickActive(ServerLevel level, DomainUVEntity domain, CompoundTag data, boolean clashing) {
		DomainSphere sphere = domain.sphere();
		// The pull keeps things from loitering against the shell whether or not a rival
		// domain is pressing on it; everything else pauses for the duration of a clash.
		pullEntities(level, sphere);
		// Twice a second, anything that got under the plane anyway — an arrow, a dropped
		// item, a noclip player who dropped in — comes back up. The collision floor only
		// catches things crossing it, deliberately; this is what covers the rest.
		if (domain.tickCount % 10 == 0)
			liftOntoFloor(level, domain, sphere, 0.5);
		if (clashing)
			return;

		UVDomainSureHitProcedure.execute(level, domain);

		int duration = data.getInt("duration") - 1;
		data.putInt("duration", duration);
		if (duration <= 0)
			beginCollapse(domain);
	}

	private static void tickCollapsing(ServerLevel level, DomainUVEntity domain, CompoundTag data) {
		int tick = data.getInt("collapseTick") + 1;
		data.putInt("collapseTick", tick);

		float progress = Math.min(1.0f, (float) tick / def(domain).collapseTicks());
		domain.setPhaseProgress(progress);
		// The radius deliberately does not shrink any more. The shell breaks where it stands
		// and the pieces are thrown outward, so they have to start from where the wall
		// actually was; pulling the radius in under them would drag the shards to the centre.

		boolean restored = DomainCarve.advanceRestore(level, domain);
		if (restored && progress >= 1.0f)
			domain.discard();
	}

	/**
	 * Undoes a pre-rework domain: blocks were stored one compound per position under a
	 * "x,y,z" key. Kept only so existing worlds clean themselves up on load.
	 */
	private static void restoreLegacyBlocks(ServerLevel level, CompoundTag data) {
		CompoundTag stored = data.getCompound("storedBlocks");
		for (String key : stored.getAllKeys()) {
			String[] coords = key.split(",");
			if (coords.length != 3)
				continue;
			try {
				net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
				CompoundTag blockData = stored.getCompound(key);
				level.setBlock(pos, net.minecraft.nbt.NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), blockData.getCompound("state")), 3);
			} catch (NumberFormatException malformedKey) {
				// a key we did not write; nothing sensible to restore
			}
		}
		data.remove("storedBlocks");
	}

	/**
	 * Runs the barrier: heals what nothing is pressing on, and decides whether it still
	 * stands.
	 *
	 * <p>A hole is not a loss. Punching through the barrier gives you a way in for a few
	 * seconds and leaves a mark; it does not end the domain, and it used to — the threshold
	 * was zero holes, so one sword breach started a death clock and anybody willing to keep
	 * swinging could delete a domain from outside it.
	 *
	 * <p>What does end it is losing the surface: most of the shell open at once, or every
	 * cell of it gone. That second one is how a rival open domain wins, by pressing evenly
	 * from every side until the whole thing gives at once.
	 *
	 * @return false once the barrier has given out
	 */
	private static boolean tickShell(DomainUVEntity domain, CompoundTag data) {
		DomainShell shell = domain.shell();
		if (shell == null)
			return true;
		shell.tickRegen();
		domain.setShellIntegrity(shell.totalIntegrity());
		// Five times a second, and only when something has actually changed. Next to the
		// eighty slash packets a shrine already sends each player per tick, this is noise.
		if (shell.isDirty() && domain.tickCount % 4 == 0) {
			PacketDistributor.sendToPlayersTrackingEntity(domain, new DomainShellSyncPacket(domain.getId(), shell.version(), shell.snapshot()));
			shell.markSynced();
		}

		if (shell.isShattered() || shell.totalIntegrity() <= 0.0f)
			return false;

		int tolerated = Math.round(DomainShell.CELLS * def(domain).collapse().breachThreshold());
		if (shell.breachCount() > tolerated) {
			int left = data.contains("destabiliseTicks") ? data.getInt("destabiliseTicks") : def(domain).collapse().destabiliseTicks();
			// more holes, less time
			left -= Math.max(1, shell.breachCount());
			data.putInt("destabiliseTicks", left);
			return left > 0;
		}
		data.remove("destabiliseTicks");
		return true;
	}

	/** Puts the domain into its shutdown phase. Safe to call more than once. */
	public static void beginCollapse(DomainUVEntity domain) {
		if (domain.getPhase() == DomainPhase.COLLAPSING)
			return;
		domain.setPhase(DomainPhase.COLLAPSING);
		domain.setPhaseProgress(0.0f);
		domain.getPersistentData().putInt("collapseTick", 0);
		domain.getPersistentData().putInt("restoreIndex", 0);
	}

	private static boolean shouldCollapseDueToCaster(ServerLevel level, DomainUVEntity domain) {
		CompoundTag data = domain.getPersistentData();
		String owner = data.getString("ownerUUID");
		if (owner.isEmpty())
			return false;
		try {
			Entity caster = level.getEntity(UUID.fromString(owner));
			if (caster == null || !caster.isAlive())
				return true;
			if (caster instanceof Player player && player.isSpectator())
				return true;
			double reach = Math.max(domain.getTargetRadius(), domain.getShellRadius());
			return caster.position().distanceToSqr(domain.position()) > reach * reach;
		} catch (IllegalArgumentException malformedUUID) {
			return true;
		}
	}

	/** The domain at the size it is going to be, whatever the shell is doing right now. */
	private static DomainSphere fullSphere(DomainUVEntity domain) {
		return new DomainSphere(domain.position(), domain.getTargetRadius(), domain.getY() + domain.getFloorOffset(), domain.getPhase(), domain.getPhaseProgress());
	}

	/**
	 * Puts everything inside the ball and more than {@code slack} under the floor plane onto
	 * the plane. Other domains are left where they are — a Shrine overlapping this Void is
	 * not a thing that fell in.
	 */
	private static void liftOntoFloor(ServerLevel level, DomainUVEntity domain, DomainSphere sphere, double slack) {
		if (!sphere.isUsable())
			return;
		double floorY = sphere.floorY();
		for (Entity entity : level.getEntities(domain, sphere.bounds(), e -> !(e instanceof net.efkrdnz.jjkstrongest.domain.DomainSource))) {
			Vec3 pos = entity.position();
			if (pos.y >= floorY - slack || !sphere.withinRadius(pos.x, pos.y, pos.z))
				continue;
			entity.teleportTo(pos.x, floorY, pos.z);
			entity.setDeltaMovement(entity.getDeltaMovement().x, 0.0, entity.getDeltaMovement().z);
			entity.resetFallDistance();
		}
	}

	/** Nudges anything drifting against the shell back toward the middle. */
	private static void pullEntities(ServerLevel level, DomainSphere sphere) {
		if (!sphere.isUsable())
			return;
		double edge = sphere.radius() - 2.0;
		if (edge <= 0.0)
			return;
		double edgeSq = edge * edge;
		for (Entity entity : level.getEntitiesOfClass(Entity.class, sphere.bounds(), e -> e instanceof LivingEntity)) {
			if (entity instanceof Player player && (player.isCreative() || player.isSpectator()))
				continue;
			Vec3 pos = entity.position();
			if (pos.distanceToSqr(sphere.center()) <= edgeSq)
				continue;
			Vec3 inward = sphere.center().subtract(pos).normalize();
			entity.setDeltaMovement(entity.getDeltaMovement().add(inward.scale(0.3)));
		}
	}
}
