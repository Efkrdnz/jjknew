package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

public class DomainClashManagerProcedure {
	// uv loses hp in 15s = 300 ticks → 100hp / 300 = 0.333/tick
	private static final float UV_DRAIN_PER_TICK = 100f / 300f;
	// shrine breaks after 20 melee hits → 5hp per hit
	public static final float SHRINE_HP_PER_HIT = 5f;
	private static final float MAX_CLASH_HP = 100f;
	private static final double CLASH_DETECT_RADIUS = 130.0;
	// ticks with no rival detected before clash is considered truly over
	private static final int CLASH_END_GRACE_TICKS = 40;

	public static void execute(LevelAccessor world, Entity uvEntity, Entity shrineEntity) {
		if (world == null || uvEntity == null || shrineEntity == null)
			return;
		if (!(world instanceof ServerLevel))
			return;
		CompoundTag uvData = uvEntity.getPersistentData();
		CompoundTag shrineData = shrineEntity.getPersistentData();
		// init clash hp only on first contact — never reset mid-clash
		if (!uvData.contains("uvClashHP"))
			uvData.putFloat("uvClashHP", MAX_CLASH_HP);
		if (!shrineData.contains("shrineClashHP"))
			shrineData.putFloat("shrineClashHP", MAX_CLASH_HP);
		// mark both as clashing and reset grace counter
		uvData.putBoolean("isClashing", true);
		shrineData.putBoolean("isClashing", true);
		setSyncedClashing(uvEntity, true);
		setSyncedClashing(shrineEntity, true);
		uvData.putInt("clashLostTicks", 0);
		shrineData.putInt("clashLostTicks", 0);
		// store rival uuids
		uvData.putString("rivalUUID", shrineEntity.getStringUUID());
		shrineData.putString("rivalUUID", uvEntity.getStringUUID());
		// drain uv hp passively every tick
		float uvHP = uvData.getFloat("uvClashHP") - UV_DRAIN_PER_TICK;
		uvData.putFloat("uvClashHP", uvHP);
		// mirror onto synced data — the clash HUD reads this on the client, where
		// persistent data has always been empty
		if (uvEntity instanceof DomainUVEntity uv)
			uv.setClashHP(Math.max(0f, uvHP));
		// check uv collapse
		if (uvHP <= 0f) {
			collapseUV(world, uvEntity);
			endClashWinner(shrineEntity);
			return;
		}
		// check shrine collapse (hp drained by melee hits in DomainClashMeleeHitProcedure)
		float shrineHP = shrineData.getFloat("shrineClashHP");
		if (shrineEntity instanceof MalevolentShrineEntity shrine)
			shrine.setClashHP(Math.max(0f, shrineHP));
		if (shrineHP <= 0f) {
			collapseShrine(shrineEntity);
			endClashWinner(uvEntity);
			return;
		}
	}

	// called every tick from DomainUVEntityTickProcedure
	public static boolean detectAndRunClash(ServerLevel level, Entity uvEntity) {
		Vec3 uvPos = uvEntity.position();
		CompoundTag uvData = uvEntity.getPersistentData();
		double uvRadius = uvEntity instanceof DomainUVEntity uv ? uv.getTargetRadius() : 30.0;
		double overlapThreshold = uvRadius + 100.0;
		double thresholdSq = overlapThreshold * overlapThreshold;
		for (MalevolentShrineEntity shrine : DomainRegistry.shrinesIn(level)) {
			if (!shrine.isAlive())
				continue;
			if (uvPos.distanceToSqr(shrine.position()) <= thresholdSq) {
				// found active rival — run clash logic and reset grace counter
				execute(level, uvEntity, shrine);
				return true;
			}
		}
		// no shrine found this tick — use grace period before truly ending clash
		if (uvData.getBoolean("isClashing")) {
			int graceTicks = uvData.getInt("clashLostTicks") + 1;
			uvData.putInt("clashLostTicks", graceTicks);
			if (graceTicks >= CLASH_END_GRACE_TICKS) {
				// rival is genuinely gone — end clash and reset hp for next time
				endClashLoser(uvEntity);
			}
			// still within grace window — stay clashing, keep hp intact
			return uvData.getBoolean("isClashing");
		}
		return false;
	}

	// check if a position is inside UV's barrier — used by the shrine to filter targets
	public static boolean isPosInsideUV(ServerLevel level, double px, double py, double pz) {
		return DomainRegistry.isInside(level, px, py, pz);
	}

	/**
	 * The rival domain's sphere, resolved once so a caller looping over many candidate
	 * points does not repeat the lookup for each one.
	 */
	public static DomainSphere rivalVoidSphere(ServerLevel level) {
		for (DomainUVEntity uv : DomainRegistry.voidsIn(level)) {
			if (!uv.isAlive())
				continue;
			DomainSphere sphere = uv.sphere();
			if (sphere.isUsable())
				return sphere;
		}
		return null;
	}

	// drain shrine hp from a melee hit — called by DomainClashMeleeHitProcedure
	public static void onMeleeHitShrineOwner(ServerLevel level, Entity attacker, Entity victim) {
		if (attacker == null || victim == null)
			return;
		DomainUVEntity uvEntity = findUVByOwner(level, attacker.getStringUUID());
		if (uvEntity == null)
			return;
		if (!uvEntity.getPersistentData().getBoolean("isClashing"))
			return;
		MalevolentShrineEntity shrine = findShrineByOwner(level, victim.getStringUUID());
		if (shrine == null)
			return;
		if (!shrine.getPersistentData().getBoolean("isClashing"))
			return;
		float shrineHP = shrine.getPersistentData().getFloat("shrineClashHP") - SHRINE_HP_PER_HIT;
		shrine.getPersistentData().putFloat("shrineClashHP", shrineHP);
	}

	public static DomainUVEntity findUVByOwner(ServerLevel level, String ownerUUID) {
		return DomainRegistry.voidByOwner(level, ownerUUID);
	}

	public static MalevolentShrineEntity findShrineByOwner(ServerLevel level, String ownerUUID) {
		return DomainRegistry.shrineByOwner(level, ownerUUID);
	}

	// collapse uv — the shrinking shell and the terrain restore are driven by
	// DomainUVEntityTickProcedure's COLLAPSING phase
	private static void collapseUV(LevelAccessor world, Entity uvEntity) {
		uvEntity.getPersistentData().putInt("duration", 0);
		uvEntity.getPersistentData().putBoolean("isClashing", false);
		uvEntity.getPersistentData().remove("uvClashHP");
		setSyncedClashing(uvEntity, false);
		if (uvEntity instanceof DomainUVEntity uv)
			DomainUVEntityTickProcedure.beginCollapse(uv);
	}

	// collapse shrine — handled by MalevolentShrineTickProcedure
	private static void collapseShrine(Entity shrineEntity) {
		shrineEntity.getPersistentData().putInt("domainLifetimeTicks", 600);
		shrineEntity.getPersistentData().putBoolean("isClashing", false);
		shrineEntity.getPersistentData().remove("shrineClashHP");
		setSyncedClashing(shrineEntity, false);
	}

	// winner — clear clash flag, keep remaining hp in case of future clash
	private static void endClashWinner(Entity domainEntity) {
		CompoundTag data = domainEntity.getPersistentData();
		data.putBoolean("isClashing", false);
		setSyncedClashing(domainEntity, false);
		data.putInt("clashLostTicks", 0);
		data.remove("rivalUUID");
		// hp intentionally NOT reset — winner keeps their remaining clash hp
	}

	// rival genuinely disappeared — safe to reset hp for next clash
	private static void endClashLoser(Entity domainEntity) {
		CompoundTag data = domainEntity.getPersistentData();
		data.putBoolean("isClashing", false);
		setSyncedClashing(domainEntity, false);
		data.putInt("clashLostTicks", 0);
		data.remove("rivalUUID");
		data.remove("uvClashHP");
		data.remove("shrineClashHP");
	}

	/** Keeps the client-visible clash flag in step with the server-side one. */
	private static void setSyncedClashing(Entity domainEntity, boolean clashing) {
		if (domainEntity instanceof DomainUVEntity uv)
			uv.setClashing(clashing);
		else if (domainEntity instanceof MalevolentShrineEntity shrine)
			shrine.setClashing(clashing);
	}
}
