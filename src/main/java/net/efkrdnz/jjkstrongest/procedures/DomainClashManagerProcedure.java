package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.UUID;

public class DomainClashManagerProcedure {
	/**
	 * What an open domain's presence costs a closed barrier each tick, spread evenly over
	 * the whole surface. Tuned so a shrine left unopposed shatters a full shell in about
	 * twenty-two seconds — a Void now has to be defended, not merely cast.
	 */
	private static final float PRESSURE_PER_TICK = DomainShell.FULL / 440f;
	private static final float MAX_CLASH_HP = 100f;
	/** Damage the shrine's caster can absorb before they lose their grip on the domain. */
	public static final float SHRINE_HOLD_POOL = 60f;
	// ticks with no rival detected before clash is considered truly over
	public static final int CLASH_END_GRACE_TICKS = 40;

	public static void execute(LevelAccessor world, Entity uvEntity, Entity shrineEntity) {
		if (world == null || uvEntity == null || shrineEntity == null)
			return;
		if (!(world instanceof ServerLevel))
			return;
		CompoundTag uvData = uvEntity.getPersistentData();
		CompoundTag shrineData = shrineEntity.getPersistentData();
		// The shrine's hold on its own domain is set once, on first contact.
		if (!shrineData.contains("shrineClashHP"))
			shrineData.putFloat("shrineClashHP", SHRINE_HOLD_POOL);
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
		// The Void's clash health IS its barrier. An open domain has no surface of its own,
		// so all it can do is lean on the closed one — evenly, from every side at once,
		// which is why the shell gives way as a piece rather than holing somewhere.
		float uvHP = MAX_CLASH_HP;
		if (uvEntity instanceof DomainUVEntity uv) {
			DomainShell shell = uv.shell();
			if (shell != null) {
				shell.applyPressure(PRESSURE_PER_TICK);
				uvHP = shell.totalIntegrity() * MAX_CLASH_HP;
			}
			// mirror onto synced data — the clash HUD reads this on the client, where
			// persistent data has always been empty
			uv.setClashHP(Math.max(0f, uvHP));
		}
		if (uvHP <= 0f) {
			collapseUV(world, uvEntity);
			endClashWinner(shrineEntity);
			return;
		}
		// The shrine has no barrier to break, so it holds only as long as its caster does.
		float shrineHP = shrineData.getFloat("shrineClashHP");
		if (shrineEntity instanceof MalevolentShrineEntity shrine)
			shrine.setClashHP(Math.max(0f, shrineHP / SHRINE_HOLD_POOL * MAX_CLASH_HP));
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

	/**
	 * The shrine is beaten through its caster: every point of damage they take is hold the
	 * domain no longer has. Replaces a flat count of twenty melee hits, which cared how
	 * often the caster was hit and not at all how hard.
	 */
	public static void onShrineOwnerHurt(ServerLevel level, Entity victim, float amount) {
		if (victim == null || amount <= 0f)
			return;
		MalevolentShrineEntity shrine = DomainRegistry.shrineByOwner(level, victim.getStringUUID());
		if (shrine == null)
			return;
		CompoundTag data = shrine.getPersistentData();
		float hp = (data.contains("shrineClashHP") ? data.getFloat("shrineClashHP") : SHRINE_HOLD_POOL) - amount;
		data.putFloat("shrineClashHP", hp);
		shrine.setClashHP(Math.max(0f, hp / SHRINE_HOLD_POOL * MAX_CLASH_HP));
		// The shrine has no barrier to break, so this is how it loses: its caster is worn
		// down until they cannot hold it. Collapsing it is a phase change now, not a
		// lifetime counter set to a number the tick procedure used to compare against.
		if (hp <= 0f)
			MalevolentShrineTickProcedure.beginCollapse(shrine);
	}

	/**
	 * The Void this domain is actually locked with.
	 *
	 * <p>Resolved from the recorded {@code rivalUUID} rather than by taking the first live
	 * Void in the level — with two domains open, the old behaviour could shield people
	 * inside one of them from a shrine that was fighting the other.
	 */
	public static DomainUVEntity rivalVoid(ServerLevel level, Entity domainEntity) {
		String rivalUUID = domainEntity.getPersistentData().getString("rivalUUID");
		if (!rivalUUID.isEmpty()) {
			try {
				if (level.getEntity(UUID.fromString(rivalUUID)) instanceof DomainUVEntity uv && uv.isAlive())
					return uv;
			} catch (IllegalArgumentException malformedUUID) {
				// fall through to the scan below
			}
		}
		for (DomainUVEntity uv : DomainRegistry.voidsIn(level)) {
			if (uv.isAlive() && uv.volume().isUsable())
				return uv;
		}
		return null;
	}

	/** Convenience for callers that only need the shape. */
	public static DomainSphere rivalVoidSphere(ServerLevel level, Entity domainEntity) {
		DomainUVEntity rival = rivalVoid(level, domainEntity);
		return rival != null ? rival.volume() : null;
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
		setSyncedClashing(uvEntity, false);
		if (uvEntity instanceof DomainUVEntity uv)
			DomainUVEntityTickProcedure.beginCollapse(uv);
	}

	// collapse shrine — the fade is driven by MalevolentShrineTickProcedure's COLLAPSING
	// phase, exactly as collapseUV above hands off to the Void's
	private static void collapseShrine(Entity shrineEntity) {
		shrineEntity.getPersistentData().putBoolean("isClashing", false);
		shrineEntity.getPersistentData().remove("shrineClashHP");
		setSyncedClashing(shrineEntity, false);
		// This used to write domainLifetimeTicks = 600 and rely on the tick procedure
		// noticing it had run out — which only worked while 600 happened to be the number
		// that procedure compared against.
		if (shrineEntity instanceof MalevolentShrineEntity shrine)
			MalevolentShrineTickProcedure.beginCollapse(shrine);
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
