package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.domain.DomainCarve;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

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

	private static final int ABSOLUTE_MAX_LIFETIME = 1200;
	/** Ticks spent growing to full size. */
	private static final int EXPANSION_TICKS = 40;
	/** Ticks at full size before the domain turns hostile. */
	private static final int SETTLE_TICKS = 40;
	/** Ticks spent shrinking while the terrain goes back. */
	private static final int COLLAPSE_TICKS = 20;

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
			if (absoluteTicks >= ABSOLUTE_MAX_LIFETIME || shouldCollapseDueToCaster(level, domain)) {
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

		switch (phase) {
			case EXPANDING -> tickExpanding(level, domain, data);
			case SETTLING -> DomainUVPostLinesPhaseProcedure.execute(level, domain, SETTLE_TICKS);
			case ACTIVE -> tickActive(level, domain, data, clashing);
			case COLLAPSING -> tickCollapsing(level, domain, data);
		}
	}

	private static void tickExpanding(ServerLevel level, DomainUVEntity domain, CompoundTag data) {
		int tick = data.getInt("expansionTick") + 1;
		data.putInt("expansionTick", tick);

		float progress = Math.min(1.0f, (float) tick / EXPANSION_TICKS);
		// ease-out so the shell slams outward and settles, rather than crawling
		float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
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
		if (data.getInt("domainAbsoluteTicks") % 20 == 0)
			lightInterior(level, sphere);
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

		float progress = Math.min(1.0f, (float) tick / COLLAPSE_TICKS);
		domain.setPhaseProgress(progress);
		domain.setShellRadius(domain.getTargetRadius() * (1.0f - progress));

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

	/**
	 * Keeps the inside of the domain visible.
	 *
	 * <p>The barrier blocks this replaced were {@code lightLevel(s -> 15)}, so they lit
	 * the whole interior. A carved-out air pocket is at sky-light zero, which would
	 * leave every entity in there rendering as a silhouette.
	 */
	private static void lightInterior(ServerLevel level, DomainSphere sphere) {
		if (!sphere.isUsable())
			return;
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sphere.bounds(), e -> true)) {
			if (!sphere.contains(target.getX(), target.getY(), target.getZ()))
				continue;
			target.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60, 0, true, false, false));
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
