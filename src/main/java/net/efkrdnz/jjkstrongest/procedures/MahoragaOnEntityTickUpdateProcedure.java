package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (world == null || entity == null)
			return;
		if (!(world instanceof ServerLevel level))
			return;
		if (entity instanceof LivingEntity le) {
			if (le.isDeadOrDying() || le.getHealth() <= 0)
				return;
		}

		// ── Timer decrements ──────────────────────────────────────────────────────
		decInt(entity, "maho_cd_global");
		decInt(entity, "maho_cd_barrage");
		decInt(entity, "maho_cd_slam");
		decInt(entity, "maho_cd_dash");
		decInt(entity, "maho_cd_cannon"); // gates slash too
		decInt(entity, "maho_cd_aura");
		decInt(entity, "maho_cd_strongjump");
		decInt(entity, "maho_cd_wallstep");
		decInt(entity, "maho_nav_delay");
		decInt(entity, "maho_brain_cd");
		decInt(entity, "maho_hurt_ticks");
		decInt(entity, "maho_cd_launch");
		decInt(entity, "maho_cd_roar");
		decInt(entity, "maho_cd_wheelcrash");
		decInt(entity, "maho_cd_dodge");
		decInt(entity, "maho_cd_feint");

		// ── Subsystems ────────────────────────────────────────────────────────────
		MahoragaBlackFlashTickProcedure.execute(level, entity);
		MahoragaPressureAndRegenTickProcedure.execute(level, entity);
		MahoragaEscapeWaterProcedure.execute(level, entity);

		// strong-jump tunnel break
		int breakTicks = entity.getPersistentData().getInt("maho_break_ticks");
		if (breakTicks > 0) {
			MahoragaBreakBlocksByHardnessProcedure.execute(level, entity.getX(), entity.getY(), entity.getZ(), 2);
			entity.getPersistentData().putInt("maho_break_ticks", breakTicks - 1);
		}

		// animation clear helper
		int ac = entity.getPersistentData().getInt("maho_anim_clear");
		if (ac > 0) {
			ac--;
			entity.getPersistentData().putInt("maho_anim_clear", ac);
			if (ac == 0 && entity instanceof MahoragaEntity _m)
				_m.setAnimation("undefined");
		}

		// wall break after delay
		int wallDelay = entity.getPersistentData().getInt("maho_wall_break_delay");
		if (wallDelay > 0) {
			wallDelay--;
			entity.getPersistentData().putInt("maho_wall_break_delay", wallDelay);
			if (wallDelay == 0)
				MahoragaBreakBlocksByHardnessProcedure.execute(level, entity.getX(), entity.getY(), entity.getZ(), 2);
		}

		// ── Target acquisition ────────────────────────────────────────────────────
		LivingEntity target = getOrFindTarget(entity);
		if (target == null) {
			entity.getPersistentData().putString("maho_state", "IDLE");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
			if (entity instanceof Mob mob)
				mob.setTarget(null);
			entity.getPersistentData().putString("maho_state", "IDLE");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// launch protection ticks
		int lt = target.getPersistentData().getInt("maho_launch_ticks");
		if (lt > 0) {
			target.getPersistentData().putInt("maho_launch_ticks", lt - 1);
			target.fallDistance = 0;
		}

		if (entity instanceof Mob mob)
			mob.setAggressive(true);

		// chant reading
		int chantTicks = 0;
		int chanting = 0;
		if (target instanceof Player) {
			chanting = target.getPersistentData().getInt("TechniquePower");
			chantTicks = target.getPersistentData().getInt("ChantCounter");
		}

		updateStuck(entity);

		// ── Phase transitions ─────────────────────────────────────────────────────
		checkPhaseTransition(level, entity);

		String state = entity.getPersistentData().getString("maho_state");
		if (state == null || state.isEmpty())
			state = "TARGETING";

		// ── Reactive dodge interrupt ──────────────────────────────────────────────
		// Fires once per hit event (cd_dodge gates re-triggering).
		// Does not interrupt committed attacks, sky combos, or defensive strafing.
		int hurtTicks = entity.getPersistentData().getInt("maho_hurt_ticks");
		if (hurtTicks > 0
				&& !isAttackState(state)
				&& !"DODGE".equals(state)
				&& !"FEINT".equals(state)
				&& !"DEFENSIVE".equals(state)
				&& entity.getPersistentData().getInt("maho_cd_dodge") <= 0
				&& level.getRandom().nextDouble() < 0.65) {
			entity.getPersistentData().putString("maho_state", "DODGE");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_dodge", 40); // block re-trigger this hit
			state = "DODGE";
		}

		// ── Chanting interrupt ────────────────────────────────────────────────────
		if (chanting == 1 && chantTicks >= 40 && !isAttackState(state)) {
			entity.getPersistentData().putString("maho_state", "DEFENSIVE");
			entity.getPersistentData().putInt("maho_t", 0);
			state = "DEFENSIVE";
		}

		// ── Unlimited Void freeze ─────────────────────────────────────────────────
		// While INFORMATION_OVERLOAD is active, Mahoraga is overwhelmed and stands
		// completely still. Wheel-spin adaptation continues via the event handler;
		// movement is suppressed here until the effect is resisted or adapted to.
		if (entity instanceof LivingEntity _uvLe &&
				_uvLe.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD.get())) {
			if (entity instanceof Mob _uvMob)
				_uvMob.getNavigation().stop();
			entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
			return;
		}

		// ── State dispatch ────────────────────────────────────────────────────────
		switch (state) {
			case "BARRAGE"     -> MahoragaBarrageProcedure.execute(level, entity, target);
			case "SLAM"        -> MahoragaGroundSlamProcedure.execute(level, entity, target);
			case "CANNON"      -> doCannonOrSlash(level, entity, target);
			case "DASH"        -> MahoragaDashStrikeProcedure.execute(level, entity, target);
			case "STRONG_JUMP" -> MahoragaStrongJumpProcedure.execute(level, entity, target);
			case "WALL_STEP"   -> MahoragaWallStepProcedure.execute(level, entity, target);
			case "AURA_FARM"   -> MahoragaAuraFarmProcedure.execute(level, entity, target);
			case "DEFENSIVE"   -> doDefensive(level, entity, target, chanting, chantTicks);
			case "SKY_LAUNCH"  -> MahoragaSkyLaunchProcedure.execute(level, entity, target);
			case "SKY_DIVE"    -> MahoragaSkyDiveProcedure.execute(level, entity, target);
			case "ROAR"        -> MahoragaRoarProcedure.execute(level, entity, target);
			case "WHEEL_CRASH" -> MahoragaWheelCrashProcedure.execute(level, entity, target);
			case "DODGE"       -> MahoragaDodgeProcedure.execute(level, entity, target);
			case "FEINT"       -> MahoragaFeintProcedure.execute(level, entity, target);
			default            -> doTargeting(level, entity, target);
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// Phase system
	// ═══════════════════════════════════════════════════════════════════════════

	// Phase 1: >75% HP — methodical, learning the player
	// Phase 2: 50-75% HP — ROAR + FEINT + DODGE unlocked, faster nav
	// Phase 3: 25-50% HP — WHEEL_CRASH, direct velocity pursuit, heavy zigzag
	// Phase 4: <25%  HP — maximum speed, fastest reaction, relentless
	private static int getPhase(Entity entity) {
		if (!(entity instanceof LivingEntity le))
			return 1;
		float ratio = le.getHealth() / le.getMaxHealth();
		if (ratio > 0.75f) return 1;
		if (ratio > 0.50f) return 2;
		if (ratio > 0.25f) return 3;
		return 4;
	}

	private static void checkPhaseTransition(ServerLevel level, Entity entity) {
		int phase = getPhase(entity);
		int lastPhase = entity.getPersistentData().getInt("maho_phase");
		if (phase <= lastPhase)
			return; // phase only escalates
		entity.getPersistentData().putInt("maho_phase", phase);
		updateSpeedForPhase(entity, phase);
		MahoragaWheelSpinProcedure.execute(level, entity);
		if (entity instanceof LivingEntity le)
			le.invulnerableTime = 30;
		// force an immediate ROAR on phase 2+ transition
		String cur = entity.getPersistentData().getString("maho_state");
		if (phase >= 2 && !"ROAR".equals(cur) && !isAttackState(cur)) {
			entity.getPersistentData().putString("maho_state", "ROAR");
			entity.getPersistentData().putInt("maho_t", 0);
			entity.getPersistentData().putInt("maho_cd_roar", 0);
		}
	}

	private static void updateSpeedForPhase(Entity entity, int phase) {
		if (!(entity instanceof LivingEntity le))
			return;
		AttributeInstance attr = le.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attr == null)
			return;
		double speed = switch (phase) {
			case 2 -> 0.46;
			case 3 -> 0.52;
			case 4 -> 0.60;
			default -> 0.40;
		};
		attr.setBaseValue(speed);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// Cannon / slash dispatch
	// ═══════════════════════════════════════════════════════════════════════════

	private static void doCannonOrSlash(ServerLevel level, Entity entity, LivingEntity target) {
		boolean slashUnlocked = isFullAdaptedToDamage(entity, "jjk_strongest:dismantle")
				|| isFullAdaptedToDamage(entity, "jjk_strongest:cleave");
		if (slashUnlocked) {
			MahoragaRangedSlashProcedure.execute(level, entity, target);
		} else {
			MahoragaAirCannonProcedure.execute(level, entity, target);
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// TARGETING — movement + attack selection
	// ═══════════════════════════════════════════════════════════════════════════

	private static void doTargeting(ServerLevel level, Entity entity, LivingEntity target) {
		double dx = target.getX() - entity.getX();
		double dy = (target.getY() + target.getBbHeight() * 0.6) - (entity.getY() + 1.4);
		double dz = target.getZ() - entity.getZ();
		double dist  = Math.sqrt(dx * dx + dy * dy + dz * dz);
		double dist2d = Math.sqrt(dx * dx + dz * dz);
		int phase = getPhase(entity);

		MahoragaFaceTargetProcedure.execute(entity, target);

		// ── Zigzag oscillation ────────────────────────────────────────────────────
		// Sine-wave timer: full cycle every ~45 ticks (0.14 rad/tick)
		int zt = entity.getPersistentData().getInt("maho_zigzag_t") + 1;
		entity.getPersistentData().putInt("maho_zigzag_t", zt);
		double zigzag = Math.sin(zt * 0.14); // −1 … +1

		// Perpendicular unit vector (left-of-approach)
		double perpX = (dist2d > 0.001) ? -dz / dist2d : 1.0;
		double perpZ = (dist2d > 0.001) ?  dx / dist2d : 0.0;

		// Strafe blend: how much lateral vs direct movement
		// Close → go straight in. Medium → mild. Far → heavy weave.
		double strafeFactor;
		if      (dist2d < 3.5) strafeFactor = 0.00;
		else if (dist2d < 7.0) strafeFactor = 0.28;
		else                   strafeFactor = 0.52;

		// ── Movement ──────────────────────────────────────────────────────────────
		if (entity instanceof Mob mob) {
			if (phase >= 3 && dist2d > 2.5) {
				// ── Direct velocity mode (phase 3-4) ─────────────────────────────
				// Bypasses pathfinder for snappier pursuit; we handle obstacles ourselves.
				mob.getNavigation().stop();
				double spd = (phase == 4) ? 0.70 : 0.58;

				double approachX = (dx / dist2d) * spd;
				double approachZ = (dz / dist2d) * spd;
				double strafeX   = perpX * strafeFactor * spd * zigzag;
				double strafeZ   = perpZ * strafeFactor * spd * zigzag;

				if (entity.horizontalCollision) {
					// Pick a steering side when first hitting a wall, keep it until clear
					int steerDir = entity.getPersistentData().getInt("maho_steer_dir");
					if (steerDir == 0) {
						steerDir = level.getRandom().nextBoolean() ? 1 : -1;
						entity.getPersistentData().putInt("maho_steer_dir", steerDir);
					}
					// Nudge perpendicular + a small upward hop to clear lips and steps
					entity.setDeltaMovement(
						entity.getDeltaMovement().x + perpX * steerDir * 0.55,
						entity.getDeltaMovement().y + 0.12,
						entity.getDeltaMovement().z + perpZ * steerDir * 0.55);
				} else {
					entity.getPersistentData().putInt("maho_steer_dir", 0); // reset when free
					entity.setDeltaMovement(
						approachX + strafeX,
						entity.getDeltaMovement().y,
						approachZ + strafeZ);
				}
				entity.hurtMarked = true;

			} else {
				// ── Pathfinder mode (phase 1-2) ───────────────────────────────────
				// Navigate toward a zigzagging waypoint beside the target so the
				// engine's own A* handles obstacle avoidance while we get lateral weave.
				int nav = entity.getPersistentData().getInt("maho_nav_delay");
				if (nav <= 0) {
					double navSpeed = 1.10 + (phase - 1) * 0.10; // 1.10 → 1.20

					// Lateral offset shrinks as we close distance (don't circle at melee range)
					double lateralMag = (dist2d > 10) ? 3.5 : (dist2d > 5) ? 2.0 : 0.0;
					double lateralOffset = zigzag * lateralMag;
					double wpX = target.getX() + perpX * lateralOffset;
					double wpZ = target.getZ() + perpZ * lateralOffset;

					mob.getNavigation().moveTo(wpX, target.getY(), wpZ, navSpeed);
					entity.getPersistentData().putInt("maho_nav_delay", phase >= 2 ? 4 : 6);
				}
			}
		}

		// ── Brain tick refractory ─────────────────────────────────────────────────
		int brain = entity.getPersistentData().getInt("maho_brain_cd");
		if (brain > 0)
			return;
		int brainRecharge = phase >= 4 ? 3 : phase >= 3 ? 5 : 7;
		entity.getPersistentData().putInt("maho_brain_cd", brainRecharge);

		// ── Traversal (height gap / stuck / wall) ─────────────────────────────────
		if (dy > 3.0 && dist < 18 && entity.onGround()
				&& entity.getPersistentData().getInt("maho_cd_strongjump") <= 0) {
			entity.getPersistentData().putString("maho_state", "STRONG_JUMP");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (entity.getPersistentData().getInt("maho_stuck_ticks") > 15
				&& entity.getPersistentData().getInt("maho_cd_strongjump") <= 0) {
			entity.getPersistentData().putString("maho_state", "STRONG_JUMP");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (entity.horizontalCollision && dy > 2.0
				&& entity.getPersistentData().getInt("maho_cd_wallstep") <= 0) {
			entity.getPersistentData().putString("maho_state", "WALL_STEP");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// Aura farm only in phase 1-2 — at phase 3+ Mahoraga never stops
		if (phase <= 2 && entity.getPersistentData().getInt("maho_cd_aura") <= 0
				&& dist > 6 && level.getRandom().nextDouble() < 0.02) {
			entity.getPersistentData().putString("maho_state", "AURA_FARM");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		if (entity.getPersistentData().getInt("maho_cd_global") > 0)
			return;

		// ── Attack selection ──────────────────────────────────────────────────────

		// WHEEL_CRASH — phase 3+ signature, medium range
		if (phase >= 3 && dist >= 4.0 && dist <= 18.0
				&& entity.getPersistentData().getInt("maho_cd_wheelcrash") <= 0
				&& level.getRandom().nextDouble() < 0.28) {
			entity.getPersistentData().putString("maho_state", "WHEEL_CRASH");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// BARRAGE — point-blank multi-hit
		if (dist <= 3.2 && entity.getPersistentData().getInt("maho_cd_barrage") <= 0) {
			entity.getPersistentData().putString("maho_state", "BARRAGE");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// SKY_LAUNCH — aerial combo starter
		if (dist <= 4.4 && target.onGround()
				&& entity.getPersistentData().getInt("maho_cd_launch") <= 0) {
			double chance = phase >= 3 ? 0.38 : 0.22;
			if (level.getRandom().nextDouble() < chance) {
				entity.getPersistentData().putString("maho_state", "SKY_LAUNCH");
				entity.getPersistentData().putInt("maho_t", 0);
				return;
			}
		}

		// FEINT — phase 2+, medium range mind-game
		if (phase >= 2 && dist >= 4.0 && dist <= 12.0
				&& entity.getPersistentData().getInt("maho_cd_feint") <= 0
				&& level.getRandom().nextDouble() < 0.18) {
			entity.getPersistentData().putString("maho_state", "FEINT");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// ROAR — phase 2+, telegraphed shockwave
		if (phase >= 2 && dist <= 8.5
				&& entity.getPersistentData().getInt("maho_cd_roar") <= 0
				&& level.getRandom().nextDouble() < 0.22) {
			entity.getPersistentData().putString("maho_state", "ROAR");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// SLAM — hop + AoE
		if (dist <= 6.0 && entity.getPersistentData().getInt("maho_cd_slam") <= 0) {
			entity.getPersistentData().putString("maho_state", "SLAM");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// CANNON / SLASH — mid range
		if (dist <= 11.5 && entity.getPersistentData().getInt("maho_cd_cannon") <= 0) {
			entity.getPersistentData().putString("maho_state", "CANNON");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		// DASH — gap-close strike
		if (dist <= 12.5 && entity.getPersistentData().getInt("maho_cd_dash") <= 0) {
			entity.getPersistentData().putString("maho_state", "DASH");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}

		entity.getPersistentData().putString("maho_state", "TARGETING");
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// DEFENSIVE — strafe and counter while player is chanting
	// ═══════════════════════════════════════════════════════════════════════════

	private static void doDefensive(ServerLevel level, Entity entity, LivingEntity target, int chanting, int chantTicks) {
		MahoragaFaceTargetProcedure.execute(entity, target);
		if (chanting != 1) {
			int t = entity.getPersistentData().getInt("maho_t");
			t++;
			entity.getPersistentData().putInt("maho_t", t);
			if (t >= 10) {
				entity.getPersistentData().putString("maho_state", "TARGETING");
				entity.getPersistentData().putInt("maho_t", 0);
			}
			return;
		}
		if (chantTicks >= 100 && entity.getPersistentData().getInt("maho_cd_dash") <= 0) {
			entity.getPersistentData().putString("maho_state", "DASH");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (chantTicks >= 60 && entity.getPersistentData().getInt("maho_cd_cannon") <= 0
				&& level.getRandom().nextDouble() < 0.2) {
			entity.getPersistentData().putString("maho_state", "CANNON");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		int dir = entity.getPersistentData().getInt("maho_strafe_dir");
		if (dir == 0)
			dir = 1;
		int flip = entity.getPersistentData().getInt("maho_strafe_flip");
		flip++;
		if (flip >= 30) {
			flip = 0;
			dir = -dir;
		}
		entity.getPersistentData().putInt("maho_strafe_dir", dir);
		entity.getPersistentData().putInt("maho_strafe_flip", flip);
		double dx = entity.getX() - target.getX();
		double dz = entity.getZ() - target.getZ();
		double dist = Math.sqrt(dx * dx + dz * dz);
		double px = -(target.getZ() - entity.getZ());
		double pz =  (target.getX() - entity.getX());
		double plen = Math.sqrt(px * px + pz * pz);
		if (plen > 0.001) { px /= plen; pz /= plen; }
		double keep   = 7.5;
		double pushOut = (dist < keep)       ? 0.35 : 0.0;
		double pullIn  = (dist > keep + 2.0) ? 0.25 : 0.0;
		double vx = px * 0.35 * dir + dx / Math.max(dist, 0.001) * pushOut - dx / Math.max(dist, 0.001) * pullIn;
		double vz = pz * 0.35 * dir + dz / Math.max(dist, 0.001) * pushOut - dz / Math.max(dist, 0.001) * pullIn;
		entity.setDeltaMovement(vx, entity.getDeltaMovement().y * 0.2, vz);
		entity.hurtMarked = true;
		if (entity.getPersistentData().getInt("maho_cd_global") <= 0)
			entity.getPersistentData().putInt("maho_cd_global", 4);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// Helpers
	// ═══════════════════════════════════════════════════════════════════════════

	// Any state where interruption would break an animation or physics sequence
	private static boolean isAttackState(String s) {
		return "BARRAGE".equals(s)     || "SLAM".equals(s)        || "CANNON".equals(s)
			|| "DASH".equals(s)        || "STRONG_JUMP".equals(s) || "WALL_STEP".equals(s)
			|| "ROAR".equals(s)        || "WHEEL_CRASH".equals(s)
			|| "SKY_LAUNCH".equals(s)  || "SKY_DIVE".equals(s)
			|| "DODGE".equals(s)       || "FEINT".equals(s);
	}

	private static LivingEntity getOrFindTarget(Entity entity) {
		if (entity instanceof Mob mob) {
			LivingEntity t = mob.getTarget();
			if (t != null && t.isAlive())
				return t;
		}
		return null;
	}

	private static void decInt(Entity e, String key) {
		int v = e.getPersistentData().getInt(key);
		if (v > 0)
			e.getPersistentData().putInt(key, v - 1);
	}

	private static void updateStuck(Entity e) {
		double lx = e.getPersistentData().getDouble("maho_lastx");
		double ly = e.getPersistentData().getDouble("maho_lasty");
		double lz = e.getPersistentData().getDouble("maho_lastz");
		double dx = e.getX() - lx;
		double dy = e.getY() - ly;
		double dz = e.getZ() - lz;
		double d2 = dx * dx + dy * dy + dz * dz;
		int stuck = e.getPersistentData().getInt("maho_stuck_ticks");
		stuck = (d2 < 0.0025) ? stuck + 1 : 0;
		e.getPersistentData().putInt("maho_stuck_ticks", stuck);
		e.getPersistentData().putDouble("maho_lastx", e.getX());
		e.getPersistentData().putDouble("maho_lasty", e.getY());
		e.getPersistentData().putDouble("maho_lastz", e.getZ());
	}

	private static boolean isFullAdaptedToDamage(Entity e, String damageId) {
		return e.getPersistentData().getInt("maho_adapt_dmg_" + damageId)
				>= MahoragaConstantsProcedure.FULL_SPINS;
	}
}
