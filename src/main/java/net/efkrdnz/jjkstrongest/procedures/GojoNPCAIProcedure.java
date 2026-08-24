package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;
import net.efkrdnz.jjkstrongest.entity.LapseBlueEntity;
import net.efkrdnz.jjkstrongest.entity.ReversalRedEntity;
import net.efkrdnz.jjkstrongest.entity.HollowPurpleBigEntity;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

import java.util.UUID;
import java.util.List;

/**
 * Gojo Satoru boss AI.
 *
 * Behavioural modes
 * ─────────────────
 *  ENGAGE        — Ground approach; fires Blue/Red opportunistically.
 *  PRESSURE      — Melee range; rapid melee + Red combos.
 *  AERIAL        — Creative-flight orbit; rains Blue/Red from above.
 *  PURPLE_CHARGE — Hover in place, charge Hollow Purple, then fire.
 *  RETREAT       — Back off; fire Red while creating distance.
 *  DOMAIN        — Unlimited Void is active; Gojo holds position.
 *  DODGE         — Reactive sidestep triggered by a heavy hit.
 *
 * Infinity (in GojoEntity.hurt) halves all incoming damage unless the source
 * is Unlimited Void (INFORMATION_OVERLOAD), which instead deals 3× damage.
 *
 * RCT (Reverse Cursed Technique) provides passive healing and a burst heal
 * below 50 % HP, making the fight feel persistent rather than just tanky.
 *
 * Reactive domain: automatically deploys Unlimited Void at 35 % HP. If Gojo
 * detects a hostile domain forming (isExpanding / isPostLines) he counter-
 * deploys his own UV domain before the sure-hit wave fires — the two domains
 * clash and neutralise each other.
 */
public class GojoNPCAIProcedure {

	// ── mode names ────────────────────────────────────────────────────────────
	private static final String M_ENGAGE        = "ENGAGE";
	private static final String M_PRESSURE      = "PRESSURE";
	private static final String M_AERIAL        = "AERIAL";
	private static final String M_PURPLE_CHARGE = "PURPLE_CHARGE";
	private static final String M_RETREAT       = "RETREAT";
	private static final String M_DOMAIN        = "DOMAIN";
	private static final String M_DODGE         = "DODGE";

	// ── persistent-data keys ─────────────────────────────────────────────────
	private static final String GOJO_MODE      = "gojo_mode";
	private static final String GOJO_MODE_T    = "gojo_mode_t";
	private static final String GOJO_STATE     = "gojo_state";     // 0 / 1 / 2 (enrage tiers)
	private static final String GOJO_COMBAT_T  = "gojo_combat_t";
	private static final String GOJO_PAUSE     = "gojo_pause";
	private static final String GOJO_TARGET    = "gojo_target_uuid";
	private static final String GOJO_FLY_Y     = "gojo_fly_y";     // target Y when airborne
	private static final String GOJO_ORBIT_DIR = "gojo_orbit_dir"; // 1 or -1
	private static final String GOJO_DODGE_DIR = "gojo_dodge_dir";
	private static final String GOJO_TICK_HP   = "gojo_tick_hp";   // HP snapshot from last tick
	private static final String GOJO_RCT_BURST = "gojo_rct_burst";

	// ── cooldown keys ─────────────────────────────────────────────────────────
	private static final String CD_BLUE    = "gojo_cd_blue";
	private static final String CD_RED     = "gojo_cd_red";
	private static final String CD_PURPLE  = "gojo_cd_purple";
	private static final String CD_MELEE   = "gojo_cd_melee";
	private static final String CD_DOMAIN  = "gojo_cd_domain";
	private static final String CD_RCT     = "gojo_cd_rct";
	private static final String CD_DODGE   = "gojo_cd_dodge";
	private static final String CD_AERIAL  = "gojo_cd_aerial";  // prevents back-to-back aerial

	// ═════════════════════════════════════════════════════════════════════════
	// MAIN ENTRY
	// ═════════════════════════════════════════════════════════════════════════
	public static void execute(Level world, Entity entity) {
		if (entity == null || world.isClientSide())
			return;
		if (!(entity instanceof PathfinderMob mob))
			return;
		if (entity instanceof LivingEntity le && (le.isDeadOrDying() || le.getHealth() <= 0))
			return;

		tickCooldowns(entity);
		tickCombat(entity);
		updateState(world, entity, mob);

		LivingEntity target = getOrRefindTarget(world, entity, mob);
		if (target == null) {
			// no target — drift to ground and recover via RCT
			mob.setNoGravity(false);
			doRCT(world, entity, mob);
			return;
		}

		faceTarget(entity, target);

		// ── reactive dodge on heavy HP drop ──────────────────────────────────
		if (entity instanceof LivingEntity gle) {
			float prevHp = entity.getPersistentData().getFloat(GOJO_TICK_HP);
			float curHp  = gle.getHealth();
			if (prevHp > 0 && curHp < prevHp - 8.0f
					&& entity.getPersistentData().getInt(CD_DODGE) <= 0) {
				entity.getPersistentData().putInt(CD_DODGE, 50);
				switchMode(entity, M_DODGE);
			}
			entity.getPersistentData().putFloat(GOJO_TICK_HP, curHp);
		}

		// ── pause (brief stagger after state transitions / domain deploy) ─────
		int pause = entity.getPersistentData().getInt(GOJO_PAUSE);
		if (pause > 0) {
			entity.getPersistentData().putInt(GOJO_PAUSE, pause - 1);
			applyBrake(entity);
			return;
		}

		// ── Unlimited Void suppression ────────────────────────────────────────
		if (entity instanceof LivingEntity uvLe
				&& uvLe.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD)) {
			mob.setNoGravity(false);
			mob.getNavigation().stop();
			entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
			return;
		}

		// ── pre-emptive domain counter ────────────────────────────────────────
		checkHostileDomain(world, entity, mob);

		// ── RCT passive / burst heal ──────────────────────────────────────────
		doRCT(world, entity, mob);

		// ── mode dispatch ─────────────────────────────────────────────────────
		String mode = entity.getPersistentData().getString(GOJO_MODE);
		if (mode.isEmpty()) mode = M_ENGAGE;
		int t = entity.getPersistentData().getInt(GOJO_MODE_T);
		entity.getPersistentData().putInt(GOJO_MODE_T, t + 1);

		double dist = entity.distanceTo(target);

		switch (mode) {
			case M_ENGAGE        -> modeEngage(world, entity, mob, target, t, dist);
			case M_PRESSURE      -> modePressure(world, entity, mob, target, t, dist);
			case M_AERIAL        -> modeAerial(world, entity, mob, target, t, dist);
			case M_PURPLE_CHARGE -> modePurpleCharge(world, entity, mob, target, t, dist);
			case M_RETREAT       -> modeRetreat(world, entity, mob, target, t, dist);
			case M_DOMAIN        -> modeDomain(world, entity, mob, target, t);
			case M_DODGE         -> modeDodge(world, entity, mob, target, t);
			default              -> switchMode(entity, M_ENGAGE);
		}

		// ── reactive domain trigger (checked every tick outside of dodge/domain) ─
		if (!M_DOMAIN.equals(mode) && !M_DODGE.equals(mode)) {
			checkReactiveDomain(world, entity, mob, target);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// MODES
	// ═════════════════════════════════════════════════════════════════════════

	// ── ENGAGE: Ground approach with opportunistic technique fire ─────────────
	private static void modeEngage(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		landIfFlying(entity, mob);

		int state = entity.getPersistentData().getInt(GOJO_STATE);
		double speed = 0.95 + state * 0.08;

		if (dist > 3.5) {
			mob.getNavigation().moveTo(target, speed);
		} else {
			switchMode(entity, M_PRESSURE);
			return;
		}

		// Blue when far (pull target in)
		if (dist > 16 && entity.getPersistentData().getInt(CD_BLUE) <= 0) {
			fireBlue(world, entity, target);
		}
		// Red at medium range (push target off-balance)
		if (dist > 8 && dist < 22 && entity.getPersistentData().getInt(CD_RED) <= 0) {
			fireRed(world, entity, target);
		}

		// Transition: go aerial after some time on the ground
		if (t >= 55 && entity.getPersistentData().getInt(CD_AERIAL) <= 0
				&& world.random.nextDouble() < 0.35) {
			switchMode(entity, M_AERIAL);
			return;
		}
		// Transition: charge Purple if in range and CD allows
		if (t >= 45 && dist > 10 && entity.getPersistentData().getInt(CD_PURPLE) <= 0
				&& world.random.nextDouble() < 0.20) {
			switchMode(entity, M_PURPLE_CHARGE);
			return;
		}
		// Safety timeout
		if (t >= 90) {
			switchMode(entity, world.random.nextBoolean() ? M_AERIAL : M_PRESSURE);
		}
	}

	// ── PRESSURE: Up-close melee and Red combos ───────────────────────────────
	private static void modePressure(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		landIfFlying(entity, mob);

		int state = entity.getPersistentData().getInt(GOJO_STATE);
		double chaseSpeed = 1.0 + state * 0.1;

		if (dist > 5.5) {
			mob.getNavigation().moveTo(target, chaseSpeed);
		} else if (dist < 1.5) {
			// Micro back-off so there's room to swing
			Vec3 away = entity.position().subtract(target.position()).normalize().scale(0.22);
			entity.setDeltaMovement(away.x, entity.getDeltaMovement().y, away.z);
			entity.hurtMarked = true;
		} else {
			mob.getNavigation().stop();
		}

		// Melee swing
		if (dist < 3.2 && entity.getPersistentData().getInt(CD_MELEE) <= 0) {
			MeleePunchProcedure.execute(world, entity);
			entity.getPersistentData().putInt(CD_MELEE, 10);
		}
		// Red to punctuate the melee sequence
		if (dist < 6.0 && entity.getPersistentData().getInt(CD_RED) <= 0) {
			fireRed(world, entity, target);
		}
		// Blue if target tries to kite away
		if (dist > 8 && entity.getPersistentData().getInt(CD_BLUE) <= 0) {
			fireBlue(world, entity, target);
		}

		// Transition
		if (t >= 35) {
			double r = world.random.nextDouble();
			if (r < 0.28 && entity.getPersistentData().getInt(CD_AERIAL) <= 0) {
				switchMode(entity, M_AERIAL);
				return;
			} else if (r < 0.48) {
				switchMode(entity, M_RETREAT);
				return;
			} else if (r < 0.58 && entity.getPersistentData().getInt(CD_PURPLE) <= 0) {
				switchMode(entity, M_PURPLE_CHARGE);
				return;
			}
		}
		if (t >= 75) switchMode(entity, M_ENGAGE);
	}

	// ── AERIAL: Fly up and rain Blue / Red on the target ─────────────────────
	// Uses creative-flight-style velocity control.  The entity's noGravity flag
	// is set true while airborne and restored when the mode ends.
	private static void modeAerial(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		int state = entity.getPersistentData().getInt(GOJO_STATE);

		if (t == 1) {
			// Pick flight height and orbit direction for this aerial pass
			double flyH = 10.0 + state * 3.0 + world.random.nextDouble() * 5.0;
			entity.getPersistentData().putDouble(GOJO_FLY_Y, target.getY() + flyH);
			entity.getPersistentData().putInt(GOJO_ORBIT_DIR, world.random.nextBoolean() ? 1 : -1);
		}

		mob.setNoGravity(true);
		mob.getNavigation().stop();

		// ── Vertical correction ───────────────────────────────────────────────
		double flyTargetY = entity.getPersistentData().getDouble(GOJO_FLY_Y);
		double vy = Math.max(-0.45, Math.min(0.45, (flyTargetY - entity.getY()) * 0.12));

		// ── Horizontal orbit above target ─────────────────────────────────────
		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double hDist = Math.sqrt(dx * dx + dz * dz);
		double idealR = 7.0 + state * 1.5;  // orbit radius
		int orbitDir = entity.getPersistentData().getInt(GOJO_ORBIT_DIR);

		double vx, vz;
		if (hDist < 0.1) {
			vx = orbitDir * 0.22;
			vz = 0;
		} else if (hDist > idealR + 4) {
			// Fly inward
			vx = dx / hDist * 0.30;
			vz = dz / hDist * 0.30;
		} else if (hDist < idealR - 4) {
			// Fly outward
			vx = -dx / hDist * 0.25;
			vz = -dz / hDist * 0.25;
		} else {
			// Orbit: tangent + small radial correction
			double perpX = -dz / hDist;
			double perpZ =  dx / hDist;
			double radial = (hDist - idealR) * 0.04;
			vx = perpX * 0.26 * orbitDir + dx / hDist * radial;
			vz = perpZ * 0.26 * orbitDir + dz / hDist * radial;
		}

		entity.setDeltaMovement(vx, vy, vz);
		entity.hurtMarked = true;

		// ── Technique fire from above ─────────────────────────────────────────
		// Blue every ~40 ticks to keep pulling target off balance
		if (entity.getPersistentData().getInt(CD_BLUE) <= 0 && t % 40 == 5) {
			fireBlue(world, entity, target);
		}
		// Red alternating with Blue
		if (entity.getPersistentData().getInt(CD_RED) <= 0 && t % 30 == 15) {
			fireRed(world, entity, target);
		}

		// ── Transitions ───────────────────────────────────────────────────────
		int aerialDur = 110 + state * 30;

		// Drop down to fire Purple if charged enough
		if (t >= 50 && entity.getPersistentData().getInt(CD_PURPLE) <= 0
				&& world.random.nextDouble() < 0.015) {
			landIfFlying(entity, mob);
			entity.getPersistentData().putInt(CD_AERIAL, 180);
			switchMode(entity, M_PURPLE_CHARGE);
			return;
		}
		if (t >= aerialDur) {
			landIfFlying(entity, mob);
			entity.getPersistentData().putInt(CD_AERIAL, 200);
			switchMode(entity, world.random.nextBoolean() ? M_ENGAGE : M_PRESSURE);
		}
	}

	// ── PURPLE_CHARGE: Hover, build up, then release Hollow Purple ───────────
	private static void modePurpleCharge(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		int state = entity.getPersistentData().getInt(GOJO_STATE);

		if (t == 1) {
			// Rise to a fixed charge height
			entity.getPersistentData().putDouble(GOJO_FLY_Y, target.getY() + 9.0);
			playSound(world, entity, "entity.ender_dragon.flap", 1.0f, 1.8f);
		}

		mob.setNoGravity(true);
		mob.getNavigation().stop();

		// Hover — gentle Y correction + very slow horizontal drift to stay in range
		double flyTargetY = entity.getPersistentData().getDouble(GOJO_FLY_Y);
		double vy = Math.max(-0.20, Math.min(0.20, (flyTargetY - entity.getY()) * 0.10));

		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double hDist = Math.sqrt(dx * dx + dz * dz);
		double vx = 0, vz = 0;
		if (hDist > 16 && hDist > 0.1) {
			vx = dx / hDist * 0.09;
			vz = dz / hDist * 0.09;
		} else if (hDist < 8 && hDist > 0.1) {
			vx = -dx / hDist * 0.07;
			vz = -dz / hDist * 0.07;
		}
		entity.setDeltaMovement(vx, vy, vz);
		entity.hurtMarked = true;

		// Charge time shortens at higher state (state 2 is 10 ticks faster)
		int chargeTime = 55 - state * 10;

		// Warning sound halfway through to telegraph the attack
		if (t == chargeTime / 2) {
			playSound(world, entity, "entity.elder_guardian.curse", 0.8f, 0.7f);
		}

		if (t >= chargeTime) {
			fireHollowPurple(world, entity, target);
			landIfFlying(entity, mob);
			entity.getPersistentData().putInt(GOJO_PAUSE, 12);
			switchMode(entity, M_RETREAT);
		}
	}

	// ── RETREAT: Back off while poking with Red ───────────────────────────────
	private static void modeRetreat(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		landIfFlying(entity, mob);

		if (dist < 22) {
			double dx = entity.getX() - target.getX();
			double dz = entity.getZ() - target.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			if (len > 0.01) {
				entity.setDeltaMovement(dx / len * 0.40, entity.getDeltaMovement().y, dz / len * 0.40);
				entity.hurtMarked = true;
			}
		} else {
			applyBrake(entity);
		}

		// Fire Red while retreating for pressure
		if (entity.getPersistentData().getInt(CD_RED) <= 0 && t % 18 == 0) {
			fireRed(world, entity, target);
		}
		// Blue to pull target into an awkward position
		if (entity.getPersistentData().getInt(CD_BLUE) <= 0 && t == 8) {
			fireBlue(world, entity, target);
		}

		if (t >= 40 || dist >= 24) {
			boolean goAerial = entity.getPersistentData().getInt(CD_AERIAL) <= 0
					&& world.random.nextDouble() < 0.40;
			switchMode(entity, goAerial ? M_AERIAL : M_ENGAGE);
		}
	}

	// ── DOMAIN: Unlimited Void is active — hold position ────────────────────
	private static void modeDomain(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t) {
		mob.setNoGravity(false);
		mob.getNavigation().stop();
		applyBrake(entity);

		// Once the domain collapses, resume combat
		if (t > 10 && !DomainCollapseManualProcedure.hasActiveDomain(world, entity)) {
			entity.getPersistentData().putInt(GOJO_PAUSE, 20);
			switchMode(entity, M_ENGAGE);
		}
		// Safety cap in case hasActiveDomain misses
		if (t >= 720) switchMode(entity, M_ENGAGE);
	}

	// ── DODGE: Reactive sidestep triggered by a big hit ──────────────────────
	private static void modeDodge(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t) {
		landIfFlying(entity, mob);
		mob.getNavigation().stop();

		if (t == 1) {
			entity.getPersistentData().putInt(GOJO_DODGE_DIR,
					world.random.nextBoolean() ? 1 : -1);
		}

		int dir = entity.getPersistentData().getInt(GOJO_DODGE_DIR);
		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double len = Math.sqrt(dx * dx + dz * dz);

		if (t <= 8 && len > 0.01) {
			double perpX = -dz / len;
			double perpZ =  dx / len;
			entity.setDeltaMovement(
					perpX * 0.88 * dir + (-dx / len) * 0.15,
					0.20,
					perpZ * 0.88 * dir + (-dz / len) * 0.15);
			entity.hurtMarked = true;
		} else {
			Vec3 v = entity.getDeltaMovement();
			entity.setDeltaMovement(v.x * 0.45, v.y, v.z * 0.45);
		}

		if (t >= 15) switchMode(entity, M_PRESSURE);
	}

	// ═════════════════════════════════════════════════════════════════════════
	// TECHNIQUE FIRE HELPERS
	// ═════════════════════════════════════════════════════════════════════════

	/**
	 * Lapse Blue — singularity placed at the target's position that continuously
	 * pulls them toward its centre.  The entity's display name is stored as the
	 * caster exclusion tag so Gojo himself is never pulled.
	 */
	private static void fireBlue(Level world, Entity entity, LivingEntity target) {
		LapseBlueEntity blue = new LapseBlueEntity(
				JjkStrongestModEntities.LAPSE_BLUE.get(), world);
		// Spawn at target's position (+1 Y so it centres on the body)
		blue.setPos(target.getX(), target.getY() + 1.0, target.getZ());
		blue.getPersistentData().putDouble("TechniquePower", techniquePower(entity));
		blue.getPersistentData().putString("caster", entity.getDisplayName().getString());
		// stay=true → LapseBlueMoveProcedure is skipped; the orb is stationary
		blue.getPersistentData().putBoolean("stay", true);
		blue.setPersistenceRequired();
		world.addFreshEntity(blue);
		LapseBlueOnInitialEntitySpawnProcedure.execute(blue);
		entity.getPersistentData().putInt(CD_BLUE, 110);
		playSound(world, entity, "entity.elder_guardian.curse", 0.9f, 1.4f);
	}

	/**
	 * Reversal Red — repulsion orb fired directly at the target.  Speed is set
	 * via deltaMovement; the entity's own tick procedure takes over from there.
	 */
	private static void fireRed(Level world, Entity entity, LivingEntity target) {
		ReversalRedEntity red = new ReversalRedEntity(
				JjkStrongestModEntities.REVERSAL_RED.get(), world);
		double dx = target.getX() - entity.getX();
		double dy = (target.getY() + 1.0) - (entity.getY() + 1.5);
		double dz = target.getZ() - entity.getZ();
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len < 0.01) return;
		// Spawn a short distance in front of Gojo so it doesn't hit him
		red.setPos(entity.getX() + dx / len * 1.5,
				entity.getY() + 1.5,
				entity.getZ() + dz / len * 1.5);
		Vec3 motion = new Vec3(dx / len * 1.6, dy / len * 1.6, dz / len * 1.6);
		red.setDeltaMovement(motion);
		red.getPersistentData().putDouble("TechniquePower", Math.max(1.6, techniquePower(entity)));
		red.getPersistentData().putString("caster", entity.getDisplayName().getString());
		red.getPersistentData().putString("state", "move");
		red.getPersistentData().putDouble("RedX", motion.x);
		red.getPersistentData().putDouble("RedY", motion.y);
		red.getPersistentData().putDouble("RedZ", motion.z);
		red.setPersistenceRequired();
		world.addFreshEntity(red);
		ReversalRedOnInitialEntitySpawnProcedure.execute(red);
		entity.getPersistentData().putInt(CD_RED, 85);
		playSound(world, entity, "entity.lightning_bolt.impact", 0.6f, 1.5f);
	}

	/**
	 * Hollow Purple — the converged Blue+Red void fired after PURPLE_CHARGE.
	 * A HollowPurpleBig entity is launched at high speed toward the target.
	 */
	private static void fireHollowPurple(Level world, Entity entity, LivingEntity target) {
		HollowPurpleBigEntity purple = new HollowPurpleBigEntity(
				JjkStrongestModEntities.HOLLOW_PURPLE_BIG.get(), world);
		double dx = target.getX() - entity.getX();
		double dy = (target.getY() + 1.0) - (entity.getY() + 1.5);
		double dz = target.getZ() - entity.getZ();
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len < 0.01) return;
		Vec3 motion = new Vec3(dx / len * 2.0, dy / len * 2.0, dz / len * 2.0);
		purple.setPos(entity.getX() + dx / len * 3.0,
				entity.getY() + 1.5 + dy / len * 3.0,
				entity.getZ() + dz / len * 3.0);
		purple.setDeltaMovement(motion);
		double tp = techniquePower(entity) * 1.5;
		purple.getPersistentData().putDouble("TechniquePower", tp);
		purple.getPersistentData().putString("caster", entity.getDisplayName().getString());
		purple.getPersistentData().putString("state", "move");
		purple.getPersistentData().putDouble("IA", 50);
		purple.getPersistentData().putDouble("PurpleX", motion.x);
		purple.getPersistentData().putDouble("PurpleY", motion.y);
		purple.getPersistentData().putDouble("PurpleZ", motion.z);
		purple.getEntityData().set(HollowPurpleBigEntity.DATA_size10, (int) Math.max(18, 10 * tp));
		purple.setPersistenceRequired();
		world.addFreshEntity(purple);
		entity.getPersistentData().putInt(CD_PURPLE, 420);
		playSound(world, entity, "entity.ender_dragon.death", 0.45f, 0.9f);
	}

	/**
	 * Deploy Unlimited Void — spawns DomainUVEntity at Gojo's feet.
	 * Domain type 0 = UV (same as the player's domain).
	 */
	private static void deployDomain(Level world, Entity entity, PathfinderMob mob) {
		DomainExpansionStartProcedure.execute(world,
				entity.getX(), entity.getY(), entity.getZ(), entity, 0);
		entity.getPersistentData().putInt(CD_DOMAIN, 9999);
		entity.getPersistentData().putInt(GOJO_PAUSE, 15);
		playSound(world, entity, "entity.ender_dragon.growl", 1.0f, 0.8f);
		switchMode(entity, M_DOMAIN);
	}

	// ═════════════════════════════════════════════════════════════════════════
	// REACTIVE DOMAIN COUNTER
	// ═════════════════════════════════════════════════════════════════════════
	/**
	 * Scans for any hostile domain in range.  If found, Gojo counter-deploys
	 * Unlimited Void so Shrine/UV can enter the clash manager immediately.
	 */
	private static void checkHostileDomain(Level world, Entity entity, PathfinderMob mob) {
		if (!(world instanceof ServerLevel sl)) return;
		if (entity.getPersistentData().getInt(CD_DOMAIN) > 0) return;
		if (DomainCollapseManualProcedure.hasActiveDomain(world, entity)) return;

		AABB scanBox = AABB.ofSize(entity.position(), 140, 140, 140);
		for (net.efkrdnz.jjkstrongest.entity.DomainUVEntity domain
				: sl.getEntitiesOfClass(net.efkrdnz.jjkstrongest.entity.DomainUVEntity.class, scanBox, e -> true)) {
			CompoundTag d = domain.getPersistentData();
			if (entity.getStringUUID().equals(d.getString("ownerUUID"))) continue;
			if (d.getBoolean("isExpanding") || d.getBoolean("isPostLines")
					|| d.getBoolean("isActive") || d.getBoolean("isClashing")) {
				deployDomain(world, entity, mob);
				return;
			}
		}
		for (MalevolentShrineEntity shrine
				: sl.getEntitiesOfClass(MalevolentShrineEntity.class, scanBox, e -> true)) {
			CompoundTag d = shrine.getPersistentData();
			if (entity.getStringUUID().equals(d.getString("ownerUUID"))) continue;
			deployDomain(world, entity, mob);
			return;
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// REACTIVE DOMAIN TRIGGER (HP threshold)
	// ═════════════════════════════════════════════════════════════════════════
	private static void checkReactiveDomain(Level world, Entity entity,
			PathfinderMob mob, LivingEntity target) {
		if (entity.getPersistentData().getInt(CD_DOMAIN) > 0) return;
		if (DomainCollapseManualProcedure.hasActiveDomain(world, entity)) return;
		if (!(entity instanceof LivingEntity le)) return;

		float hpPct = le.getHealth() / le.getMaxHealth();

		// First domain at 35 % HP
		if (!entity.getPersistentData().getBoolean("gojo_domain_used")
				&& hpPct <= 0.35f) {
			entity.getPersistentData().putBoolean("gojo_domain_used", true);
			deployDomain(world, entity, mob);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// RCT — Reverse Cursed Technique
	// ═════════════════════════════════════════════════════════════════════════
	private static void doRCT(Level world, Entity entity, PathfinderMob mob) {
		if (!(entity instanceof LivingEntity le)) return;

		// Passive trickle: 0.5 HP every 2 seconds
		if (world.getGameTime() % 40 == 0 && le.getHealth() < le.getMaxHealth()) {
			le.setHealth(Math.min(le.getMaxHealth(), le.getHealth() + 0.5f));
		}

		// Burst heal below 50 % HP
		float hpPct = le.getHealth() / le.getMaxHealth();
		if (hpPct < 0.50f && entity.getPersistentData().getInt(CD_RCT) <= 0) {
			entity.getPersistentData().putInt(GOJO_RCT_BURST, 30);
			entity.getPersistentData().putInt(CD_RCT, 220);
			playSound(world, entity, "entity.player.levelup", 0.5f, 1.6f);
		}

		int burstTick = entity.getPersistentData().getInt(GOJO_RCT_BURST);
		if (burstTick > 0) {
			le.setHealth(Math.min(le.getMaxHealth(), le.getHealth() + 0.55f));
			entity.getPersistentData().putInt(GOJO_RCT_BURST, burstTick - 1);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// STATE / PHASE
	// ═════════════════════════════════════════════════════════════════════════
	private static void updateState(Level world, Entity entity, PathfinderMob mob) {
		float hpPct = mob.getHealth() / mob.getMaxHealth();
		if (!entity.getPersistentData().getBoolean("gojo_s1_locked") && hpPct <= 0.60f) {
			entity.getPersistentData().putInt(GOJO_STATE, 1);
			entity.getPersistentData().putBoolean("gojo_s1_locked", true);
			entity.getPersistentData().putInt(GOJO_PAUSE, 8);
			playSound(world, entity, "entity.ender_dragon.growl", 1.0f, 1.3f);
		}
		if (!entity.getPersistentData().getBoolean("gojo_s2_locked") && hpPct <= 0.30f) {
			entity.getPersistentData().putInt(GOJO_STATE, 2);
			entity.getPersistentData().putBoolean("gojo_s2_locked", true);
			entity.getPersistentData().putInt(GOJO_PAUSE, 12);
			playSound(world, entity, "entity.ender_dragon.growl", 1.0f, 0.7f);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// TARGET
	// ═════════════════════════════════════════════════════════════════════════
	private static LivingEntity getOrRefindTarget(Level world, Entity entity, PathfinderMob mob) {
		LivingEntity cur = mob.getTarget();
		if (cur instanceof Player p && (p.isCreative() || p.isSpectator())) {
			mob.setTarget(null);
			cur = null;
		}
		if (cur != null && cur.isAlive()) {
			entity.getPersistentData().putString(GOJO_TARGET, cur.getStringUUID());
			return cur;
		}
		// Fall back to stored UUID
		String stored = entity.getPersistentData().getString(GOJO_TARGET);
		if (!stored.isEmpty()) {
			try {
				String uuid = stored;
				for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class,
						AABB.ofSize(entity.position(), 256, 64, 256))) {
					if (e.getStringUUID().equals(uuid) && e.isAlive()) {
						mob.setTarget(e);
						return e;
					}
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	// ═════════════════════════════════════════════════════════════════════════
	// MOVEMENT / UTILITY
	// ═════════════════════════════════════════════════════════════════════════
	private static void switchMode(Entity entity, String newMode) {
		entity.getPersistentData().putString(GOJO_MODE, newMode);
		entity.getPersistentData().putInt(GOJO_MODE_T, 0);
	}

	private static void landIfFlying(Entity entity, PathfinderMob mob) {
		mob.setNoGravity(false);
	}

	/** Returns TechniquePower scalar based on current enrage state. */
	private static double techniquePower(Entity entity) {
		int state = entity.getPersistentData().getInt(GOJO_STATE);
		return 1.0 + state * 0.5; // 1.0 / 1.5 / 2.0
	}

	private static void faceTarget(Entity entity, LivingEntity target) {
		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double dy = (target.getY() + target.getBbHeight() * 0.5)
				- (entity.getY() + entity.getBbHeight() * 0.5);
		double hd = Math.sqrt(dx * dx + dz * dz);
		float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, hd));
		entity.setYRot(yaw);
		entity.setYHeadRot(yaw);
		entity.yRotO = yaw;
		entity.setXRot(pitch);
	}

	private static void applyBrake(Entity entity) {
		Vec3 v = entity.getDeltaMovement();
		entity.setDeltaMovement(v.x * 0.18, v.y, v.z * 0.18);
	}

	private static void tickCooldowns(Entity entity) {
		decInt(entity, CD_BLUE);
		decInt(entity, CD_RED);
		decInt(entity, CD_PURPLE);
		decInt(entity, CD_MELEE);
		decInt(entity, CD_DOMAIN);
		decInt(entity, CD_RCT);
		decInt(entity, CD_DODGE);
		decInt(entity, CD_AERIAL);
	}

	private static void decInt(Entity entity, String key) {
		int v = entity.getPersistentData().getInt(key);
		if (v > 0) entity.getPersistentData().putInt(key, v - 1);
	}

	private static void tickCombat(Entity entity) {
		entity.getPersistentData().putInt(GOJO_COMBAT_T,
				entity.getPersistentData().getInt(GOJO_COMBAT_T) + 1);
	}

	private static void playSound(Level world, Entity entity,
			String soundId, float volume, float pitch) {
		world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
				BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)),
				SoundSource.HOSTILE, volume, pitch);
	}
}
