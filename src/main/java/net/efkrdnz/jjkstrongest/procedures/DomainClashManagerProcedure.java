package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.List;

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
		uvData.putInt("clashLostTicks", 0);
		shrineData.putInt("clashLostTicks", 0);
		// store rival uuids
		uvData.putString("rivalUUID", shrineEntity.getStringUUID());
		shrineData.putString("rivalUUID", uvEntity.getStringUUID());
		// drain uv hp passively every tick
		float uvHP = uvData.getFloat("uvClashHP") - UV_DRAIN_PER_TICK;
		uvData.putFloat("uvClashHP", uvHP);
		// check uv collapse
		if (uvHP <= 0f) {
			collapseUV(world, uvEntity);
			endClashWinner(shrineEntity);
			return;
		}
		// check shrine collapse (hp drained by melee hits in DomainClashMeleeHitProcedure)
		float shrineHP = shrineData.getFloat("shrineClashHP");
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
		double uvRadius = uvData.getDouble("domainRadius");
		AABB searchBox = new AABB(uvPos.x - CLASH_DETECT_RADIUS, uvPos.y - CLASH_DETECT_RADIUS, uvPos.z - CLASH_DETECT_RADIUS, uvPos.x + CLASH_DETECT_RADIUS, uvPos.y + CLASH_DETECT_RADIUS, uvPos.z + CLASH_DETECT_RADIUS);
		List<MalevolentShrineEntity> shrines = level.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> e.isAlive());
		for (MalevolentShrineEntity shrine : shrines) {
			double distSq = uvPos.distanceToSqr(shrine.position());
			double overlapThreshold = uvRadius + 100.0;
			if (distSq <= overlapThreshold * overlapThreshold) {
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

	// check if a position is inside UV's barrier — used by shrine to filter targets
	public static boolean isPosInsideUV(ServerLevel level, double px, double py, double pz) {
		AABB searchBox = new AABB(px - 150, py - 150, pz - 150, px + 150, py + 150, pz + 150);
		List<DomainUVEntity> uvDomains = level.getEntitiesOfClass(DomainUVEntity.class, searchBox, e -> e.isAlive());
		for (DomainUVEntity uv : uvDomains) {
			CompoundTag data = uv.getPersistentData();
			if (!data.getBoolean("isActive") && !data.getBoolean("isClashing"))
				continue;
			double radius = data.getDouble("domainRadius");
			if (uv.position().distanceToSqr(px, py, pz) <= radius * radius)
				return true;
		}
		return false;
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
		List<DomainUVEntity> list = level.getEntitiesOfClass(DomainUVEntity.class, new AABB(-30000, -512, -30000, 30000, 512, 30000), e -> e.isAlive());
		for (DomainUVEntity uv : list) {
			if (uv.getPersistentData().getString("ownerUUID").equals(ownerUUID))
				return uv;
		}
		return null;
	}

	public static MalevolentShrineEntity findShrineByOwner(ServerLevel level, String ownerUUID) {
		List<MalevolentShrineEntity> list = level.getEntitiesOfClass(MalevolentShrineEntity.class, new AABB(-30000, -512, -30000, 30000, 512, 30000), e -> e.isAlive());
		for (MalevolentShrineEntity shrine : list) {
			if (shrine.getPersistentData().getString("ownerUUID").equals(ownerUUID))
				return shrine;
		}
		return null;
	}

	// collapse uv — block restore handled by DomainUVEntityTickProcedure
	private static void collapseUV(LevelAccessor world, Entity uvEntity) {
		uvEntity.getPersistentData().putInt("duration", 0);
		uvEntity.getPersistentData().putBoolean("isClashing", false);
		uvEntity.getPersistentData().putBoolean("isExpanding", false);
		uvEntity.getPersistentData().putBoolean("isPostLines", false);
		uvEntity.getPersistentData().putBoolean("isActive", true);
		uvEntity.getPersistentData().remove("uvClashHP");
	}

	// collapse shrine — handled by MalevolentShrineTickProcedure
	private static void collapseShrine(Entity shrineEntity) {
		shrineEntity.getPersistentData().putInt("domainLifetimeTicks", 600);
		shrineEntity.getPersistentData().putBoolean("isClashing", false);
		shrineEntity.getPersistentData().remove("shrineClashHP");
	}

	// winner — clear clash flag, keep remaining hp in case of future clash
	private static void endClashWinner(Entity domainEntity) {
		CompoundTag data = domainEntity.getPersistentData();
		data.putBoolean("isClashing", false);
		data.putInt("clashLostTicks", 0);
		data.remove("rivalUUID");
		// hp intentionally NOT reset — winner keeps their remaining clash hp
	}

	// rival genuinely disappeared — safe to reset hp for next clash
	private static void endClashLoser(Entity domainEntity) {
		CompoundTag data = domainEntity.getPersistentData();
		data.putBoolean("isClashing", false);
		data.putInt("clashLostTicks", 0);
		data.remove("rivalUUID");
		data.remove("uvClashHP");
		data.remove("shrineClashHP");
	}
}
