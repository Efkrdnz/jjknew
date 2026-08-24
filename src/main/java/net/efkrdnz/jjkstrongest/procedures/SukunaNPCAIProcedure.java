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
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.UUID;
import java.util.List;
import java.util.Comparator;

public class SukunaNPCAIProcedure {

	// ── existing action/state keys ────────────────────────────────────────────
	private static final String AI_STATE          = "ai_state";
	private static final String AI_COMBAT_TIMER   = "ai_combat_timer";
	private static final String AI_ACTION         = "ai_action";
	private static final String AI_STATE_LOCKED_1 = "ai_state_locked_1";
	private static final String AI_STATE_LOCKED_2 = "ai_state_locked_2";
	private static final String AI_BLOCK_TIMER    = "ai_block_timer";
	private static final String AI_COUNTER_TIMER  = "ai_counter_timer";
	private static final String AI_RCT_METER      = "ai_rct_meter";
	private static final String AI_RCT_TIMER      = "ai_rct_timer";
	private static final String AI_RCT_COOLDOWN   = "ai_rct_cooldown";
	private static final String AI_BURNOUT_TIMER  = "ai_burnout_timer";
	private static final String AI_DOMAIN_AMP     = "ai_domain_amp";
	private static final String AI_LUNGE_COOLDOWN = "ai_lunge_cooldown";
	private static final String AI_LUNGE_TELE     = "ai_lunge_telegraph";
	private static final String AI_DASH_COOLDOWN  = "ai_dash_cooldown";
	private static final String AI_DASH_TELE      = "ai_dash_telegraph";
	private static final String AI_STUCK_COUNT    = "ai_stuck_count";
	private static final String AI_LAST_X         = "ai_last_x";
	private static final String AI_LAST_Z         = "ai_last_z";
	private static final String AI_LAST_HP        = "ai_last_hp";
	private static final String AI_HP_CHECK_TIMER = "ai_hp_check_timer";
	private static final String AI_TARGET_UUID    = "ai_target_uuid";
	private static final String AI_PAUSE_TIMER    = "ai_pause_timer";
	private static final String AI_BURST_TIMER    = "ai_burst_timer";
	private static final String AI_BURST_COUNT    = "ai_burst_count";
	private static final String AI_CLEAVE_TIMER   = "ai_cleave_timer";
	private static final String AI_CLEAVE_COUNT   = "ai_cleave_count";
	private static final String AI_SC_TIMER       = "ai_sc_timer";   // slash combo
	private static final String AI_SC_COUNT       = "ai_sc_count";

	// ── behavioral mode keys (the new smart system) ───────────────────────────
	private static final String SUK_MODE      = "suk_mode";       // current mode string
	private static final String SUK_MODE_T    = "suk_mode_t";     // ticks in current mode
	private static final String SUK_FLANK_DIR = "suk_flank_dir";  // orbit direction 1/-1
	private static final String SUK_DODGE_DIR = "suk_dodge_dir";  // sidestep direction
	private static final String SUK_DODGE_CD  = "suk_dodge_cd";   // reactive dodge cooldown
	private static final String SUK_APP_SIDE  = "suk_app_side";   // approach arc side
	private static final String SUK_TICK_HP   = "suk_tick_hp";    // hp from last tick

	// mode name constants
	private static final String M_ENGAGE    = "ENGAGE";
	private static final String M_PRESSURE  = "PRESSURE";
	private static final String M_BACK_OFF  = "BACK_OFF";
	private static final String M_FLANK     = "FLANK";
	private static final String M_JUMP_OVER = "JUMP_OVER";
	private static final String M_DODGE     = "DODGE";

	// ── cooldown keys ─────────────────────────────────────────────────────────
	private static final String CD_DISMANTLE  = "ai_cd_dismantle";
	private static final String CD_FUGA       = "ai_cd_fuga";
	private static final String CD_SHRINE     = "ai_cd_shrine";
	private static final String CD_MELEE      = "ai_cd_melee";
	private static final String CD_UPPERCUT   = "ai_cd_uppercut";
	private static final String CD_CLEAVE     = "ai_cd_cleave";
	private static final String CD_SC         = "ai_cd_sc";

	// ── action strings ────────────────────────────────────────────────────────
	private static final String ACT_IDLE     = "idle";
	private static final String ACT_RCT      = "rct";
	private static final String ACT_BURNOUT  = "burnout";
	private static final String ACT_BLOCK    = "block";
	private static final String ACT_COUNTER  = "counter";

	// ── numeric constants ─────────────────────────────────────────────────────
	private static final int   BURNOUT_DURATION = 300;
	private static final int   RCT_MIN_DURATION = 50;
	private static final int   RCT_COOLDOWN     = 90;
	private static final float RCT_REGEN_RATE   = 0.7f;
	private static final float RCT_DRAIN_RATE   = 2.5f;
	private static final float RCT_HEAL_RATE    = 1.4f;
	private static final float RCT_TRIGGER_DROP = 25.0f;
	private static final int   LUNGE_COOLDOWN   = 42;
	private static final int   DASH_COOLDOWN    = 45;
	private static final int   TELE_TICKS       = 3;

	// ═════════════════════════════════════════════════════════════════════════
	// MAIN ENTRY
	// ═════════════════════════════════════════════════════════════════════════
	public static void execute(Level world, Entity entity) {
		if (entity == null || world.isClientSide())
			return;
		if (!(entity instanceof PathfinderMob mob))
			return;

		tickCooldowns(entity);
		tickCombatTimer(entity);
		updateState(world, entity, mob);

		LivingEntity target = getOrRefindTarget(world, entity, mob);
		if (target == null) {
			entity.getPersistentData().putString(AI_ACTION, ACT_IDLE);
			if (mob.getHealth() < mob.getMaxHealth())
				mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + 1.5f));
			return;
		}

		// always face target before everything else
		faceTarget(entity, target);

		// ── reactive dodge on HP drop (detected per-tick) ─────────────────────
		if (entity instanceof LivingEntity le) {
			float prevHp = entity.getPersistentData().getFloat(SUK_TICK_HP);
			float curHp  = le.getHealth();
			if (prevHp > 0 && curHp < prevHp - 2.5f) {
				maybeReactiveDodge(entity);
			}
			entity.getPersistentData().putFloat(SUK_TICK_HP, curHp);
		}

		checkDomainAMP(entity, target);
		checkRCT(entity, mob);
		tickBlockState(entity, mob, target, world);
		tickBurnout(entity);
		tickRCT(entity, mob);

		int pause = entity.getPersistentData().getInt(AI_PAUSE_TIMER);
		if (pause > 0) {
			entity.getPersistentData().putInt(AI_PAUSE_TIMER, pause - 1);
			applyBrake(entity);
			return;
		}

		// ── Unlimited Void freeze ─────────────────────────────────────────────────
		// While INFORMATION_OVERLOAD is active, Sukuna is overwhelmed and stands
		// completely still — his domain should have fired before this point if he
		// detected the domain expanding; if it didn't, he takes the full suppression.
		if (entity instanceof LivingEntity _uvLe &&
				_uvLe.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD.get())) {
			mob.getNavigation().stop();
			entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
			return;
		}

		// ── Pre-emptive domain counter ────────────────────────────────────────────
		// Scan for an Unlimited Void that is still forming (isExpanding / isPostLines).
		// If found, Sukuna deploys Malevolent Shrine before the sure-hit fires.
		checkForHostileDomain(world, entity, mob);

		handleMovement(world, entity, mob, target);
		handleAbilities(world, entity, mob, target);
	}

	// ═════════════════════════════════════════════════════════════════════════
	// STATE MANAGEMENT
	// ═════════════════════════════════════════════════════════════════════════
	private static void tickCombatTimer(Entity entity) {
		entity.getPersistentData().putInt(AI_COMBAT_TIMER,
				entity.getPersistentData().getInt(AI_COMBAT_TIMER) + 1);
	}

	private static void updateState(Level world, Entity entity, PathfinderMob mob) {
		int   timer  = entity.getPersistentData().getInt(AI_COMBAT_TIMER);
		float hpPct  = mob.getHealth() / mob.getMaxHealth();
		if (!entity.getPersistentData().getBoolean(AI_STATE_LOCKED_1)) {
			if (timer >= 120 || hpPct <= 0.70f) {
				entity.getPersistentData().putInt(AI_STATE, 1);
				entity.getPersistentData().putBoolean(AI_STATE_LOCKED_1, true);
				entity.getPersistentData().putInt(AI_PAUSE_TIMER, 8);
				playSound(world, entity, "entity.ender_dragon.growl", 1.0f, 1.2f);
			}
		}
		if (!entity.getPersistentData().getBoolean(AI_STATE_LOCKED_2)) {
			if (timer >= 400 || hpPct <= 0.35f) {
				entity.getPersistentData().putInt(AI_STATE, 2);
				entity.getPersistentData().putBoolean(AI_STATE_LOCKED_2, true);
				entity.getPersistentData().putInt(AI_PAUSE_TIMER, 14);
				playSound(world, entity, "jjk_strongest:sukuna_domain_act", 1.0f, 1.0f);
			}
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// TARGET
	// ═════════════════════════════════════════════════════════════════════════
	private static LivingEntity getOrRefindTarget(Level world, Entity entity, PathfinderMob mob) {
		LivingEntity current = mob.getTarget();
		if (current instanceof Player p && (p.isCreative() || p.isSpectator())) {
			mob.setTarget(null);
			current = null;
		}
		if (current != null && current.isAlive()) {
			entity.getPersistentData().putString(AI_TARGET_UUID, current.getStringUUID());
			return current;
		}
		String stored = entity.getPersistentData().getString(AI_TARGET_UUID);
		if (!stored.isEmpty()) {
			try {
				UUID uuid = UUID.fromString(stored);
				for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class,
						AABB.ofSize(entity.position(), 200, 200, 200),
						e -> e.isAlive() && e != entity)) {
					if (e.getUUID().equals(uuid)) { mob.setTarget(e); return e; }
				}
			} catch (IllegalArgumentException ignored) {}
		}
		List<Player> players = world.getEntitiesOfClass(Player.class,
				AABB.ofSize(entity.position(), 200, 200, 200),
				e -> e.isAlive() && !e.isCreative() && !e.isSpectator());
		if (!players.isEmpty()) {
			players.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity)));
			mob.setTarget(players.get(0));
			entity.getPersistentData().putString(AI_TARGET_UUID, players.get(0).getStringUUID());
			return players.get(0);
		}
		List<LivingEntity> any = world.getEntitiesOfClass(LivingEntity.class,
				AABB.ofSize(entity.position(), 200, 200, 200),
				e -> e.isAlive() && e != entity && e.getClass() != entity.getClass());
		if (!any.isEmpty()) {
			any.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity)));
			mob.setTarget(any.get(0));
			entity.getPersistentData().putString(AI_TARGET_UUID, any.get(0).getStringUUID());
			return any.get(0);
		}
		return null;
	}

	// ═════════════════════════════════════════════════════════════════════════
	// DOMAIN AMP / RCT / BLOCK / BURNOUT  (unchanged logic, tightened numbers)
	// ═════════════════════════════════════════════════════════════════════════
	private static void checkDomainAMP(Entity entity, LivingEntity target) {
		if (ACT_RCT.equals(entity.getPersistentData().getString(AI_ACTION))) return;
		boolean tInfinity  = target.hasEffect(JjkStrongestModMobEffects.INFINITY.get());
		boolean hasAMP     = entity.getPersistentData().getBoolean(AI_DOMAIN_AMP);
		if (tInfinity && !hasAMP)  { ActivateDomainAMPProcedure.execute(entity);   entity.getPersistentData().putBoolean(AI_DOMAIN_AMP, true);  }
		if (!tInfinity && hasAMP)  { DeactivateDomainAMPProcedure.execute(entity); entity.getPersistentData().putBoolean(AI_DOMAIN_AMP, false); }
	}

	private static void checkRCT(Entity entity, PathfinderMob mob) {
		if (ACT_RCT.equals(entity.getPersistentData().getString(AI_ACTION))) return;
		if (entity.getPersistentData().getInt(AI_RCT_COOLDOWN) > 0) return;
		float curHp = mob.getHealth(), maxHp = mob.getMaxHealth();
		int checkT  = entity.getPersistentData().getInt(AI_HP_CHECK_TIMER) + 1;
		entity.getPersistentData().putInt(AI_HP_CHECK_TIMER, checkT);
		if (checkT >= 60) {
			entity.getPersistentData().putInt(AI_HP_CHECK_TIMER, 0);
			float last = entity.getPersistentData().getFloat(AI_LAST_HP);
			if (last - curHp > RCT_TRIGGER_DROP) { triggerRCT(entity); }
			entity.getPersistentData().putFloat(AI_LAST_HP, curHp);
		}
		if (curHp / maxHp < 0.35f && entity.getPersistentData().getFloat(AI_RCT_METER) > 40)
			triggerRCT(entity);
	}

	private static void triggerRCT(Entity entity) {
		entity.getPersistentData().putString(AI_ACTION, ACT_RCT);
		entity.getPersistentData().putInt(AI_RCT_TIMER, RCT_MIN_DURATION);
		entity.getPersistentData().putInt(AI_RCT_COOLDOWN, RCT_COOLDOWN);
	}

	private static void tickRCT(Entity entity, PathfinderMob mob) {
		if (!ACT_RCT.equals(entity.getPersistentData().getString(AI_ACTION))) {
			float m = Math.min(100f, entity.getPersistentData().getFloat(AI_RCT_METER) + RCT_REGEN_RATE);
			entity.getPersistentData().putFloat(AI_RCT_METER, m);
			return;
		}
		float m = entity.getPersistentData().getFloat(AI_RCT_METER) - RCT_DRAIN_RATE;
		entity.getPersistentData().putFloat(AI_RCT_METER, Math.max(0, m));
		mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + RCT_HEAL_RATE));
		int t = entity.getPersistentData().getInt(AI_RCT_TIMER) - 1;
		entity.getPersistentData().putInt(AI_RCT_TIMER, t);
		if (m <= 0 && t <= 0) entity.getPersistentData().putString(AI_ACTION, ACT_IDLE);
	}

	private static void tickBlockState(Entity entity, PathfinderMob mob, LivingEntity target, Level world) {
		if (entity.getPersistentData().getBoolean("is_blocking")) {
			entity.getPersistentData().putString(AI_ACTION, ACT_BLOCK);
			int bt = entity.getPersistentData().getInt(AI_BLOCK_TIMER) - 1;
			entity.getPersistentData().putInt(AI_BLOCK_TIMER, bt);
			if (bt <= 0) {
				entity.getPersistentData().putBoolean("is_blocking", false);
				entity.getPersistentData().putInt(AI_COUNTER_TIMER, 22);
				entity.getPersistentData().putString(AI_ACTION, ACT_COUNTER);
			}
			return;
		}
		int ct = entity.getPersistentData().getInt(AI_COUNTER_TIMER);
		if (ct > 0) {
			entity.getPersistentData().putInt(AI_COUNTER_TIMER, ct - 1);
			entity.getPersistentData().putString(AI_ACTION, ACT_COUNTER);
			if (entity.distanceTo(target) <= 4.5 && entity.getPersistentData().getInt(CD_MELEE) <= 0) {
				MeleePunchProcedure.execute(world, entity);
				entity.getPersistentData().putInt(CD_MELEE, 8);
			}
			if (ct <= 1) entity.getPersistentData().putString(AI_ACTION, ACT_IDLE);
		}
	}

	private static void tickBurnout(Entity entity) {
		int b = entity.getPersistentData().getInt(AI_BURNOUT_TIMER);
		if (b <= 0) return;
		entity.getPersistentData().putInt(AI_BURNOUT_TIMER, b - 1);
		entity.getPersistentData().putString(AI_ACTION, ACT_BURNOUT);
		if (b <= 1) {
			entity.getPersistentData().putString(AI_ACTION, ACT_IDLE);
			entity.getPersistentData().putInt(CD_SHRINE, 1200);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// MOVEMENT — routes to special actions or behavioral mode
	// ═════════════════════════════════════════════════════════════════════════
	private static void handleMovement(Level world, Entity entity, PathfinderMob mob, LivingEntity target) {
		String action  = entity.getPersistentData().getString(AI_ACTION);
		double dist    = entity.distanceTo(target);
		double vertGap = target.getY() - entity.getY();

		// ── locked actions override mode ───────────────────────────────────
		if (ACT_RCT.equals(action)) {
			if (dist < 14) {
				Vec3 away = entity.position().subtract(target.position()).normalize().scale(0.22);
				entity.setDeltaMovement(away.x, entity.getDeltaMovement().y, away.z);
				entity.hurtMarked = true;
			} else { applyBrake(entity); }
			return;
		}
		if (ACT_BURNOUT.equals(action)) { if (dist > 5) moveToward(entity, target, 0.28); return; }
		if (ACT_BLOCK.equals(action))   { applyBrake(entity); return; }
		if (ACT_COUNTER.equals(action)) { moveToward(entity, target, 0.75); return; }

		// ── aerial pursuit ────────────────────────────────────────────────
		if (vertGap > 4) { handleAerial(world, entity, mob, target); return; }

		// ── lunge telegraph ───────────────────────────────────────────────
		int lt = entity.getPersistentData().getInt(AI_LUNGE_TELE);
		if (lt > 0) {
			entity.getPersistentData().putInt(AI_LUNGE_TELE, lt - 1);
			applyBrake(entity);
			if (lt == 1) applyLunge(entity, target);
			return;
		}

		// ── behavioral mode ───────────────────────────────────────────────
		tickMode(world, entity, mob, target);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// BEHAVIORAL MODE DISPATCHER
	// ─────────────────────────────────────────────────────────────────────────
	private static void tickMode(Level world, Entity entity, PathfinderMob mob, LivingEntity target) {
		String mode = entity.getPersistentData().getString(SUK_MODE);
		if (mode.isEmpty()) { mode = M_ENGAGE; entity.getPersistentData().putString(SUK_MODE, mode); }

		int t = entity.getPersistentData().getInt(SUK_MODE_T) + 1;
		entity.getPersistentData().putInt(SUK_MODE_T, t);

		int    state = entity.getPersistentData().getInt(AI_STATE);
		double dist  = entity.distanceTo(target);

		switch (mode) {
			case M_ENGAGE    -> modeEngage   (world, entity, mob, target, t, dist, state);
			case M_PRESSURE  -> modePressure (world, entity, mob, target, t, dist, state);
			case M_BACK_OFF  -> modeBackOff  (entity, mob, target, t, dist);
			case M_FLANK     -> modeFlank    (entity, mob, target, t, dist, state);
			case M_JUMP_OVER -> modeJumpOver (world, entity, mob, target, t, dist);
			case M_DODGE     -> modeDodge    (entity, mob, target, t);
			default          -> modeEngage   (world, entity, mob, target, t, dist, state);
		}
	}

	// ── ENGAGE: arc toward target from one side — never a straight charge ─────
	private static void modeEngage(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist, int state) {
		double dx  = target.getX() - entity.getX();
		double dz  = target.getZ() - entity.getZ();
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len > 0.01) {
			double perpX = -dz / len;
			double perpZ =  dx / len;
			int side = entity.getPersistentData().getInt(SUK_APP_SIDE);
			if (side == 0) {
				side = entity.level().random.nextBoolean() ? 1 : -1;
				entity.getPersistentData().putInt(SUK_APP_SIDE, side);
			}
			// waypoint is target's position offset to one side — A* handles obstacles
			double offset = dist > 8 ? 2.5 : (dist > 4 ? 1.2 : 0);
			mob.getNavigation().moveTo(
					target.getX() + perpX * side * offset,
					target.getY(),
					target.getZ() + perpZ * side * offset,
					1.0);
		}
		// lunge in state 2 to close large gaps explosively
		if (state >= 2 && dist > 14 && entity.getPersistentData().getInt(AI_LUNGE_COOLDOWN) <= 0) {
			entity.getPersistentData().putInt(AI_LUNGE_TELE, TELE_TICKS);
			entity.getPersistentData().putInt(AI_LUNGE_COOLDOWN, LUNGE_COOLDOWN);
		}
		// transition
		if (dist <= 3.2) switchMode(entity, M_PRESSURE);
		else if (t >= 55) switchMode(entity, chooseMode(entity, dist, state, world));
	}

	// ── PRESSURE: maintain melee range, micro-adjust position ────────────────
	private static void modePressure(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist, int state) {
		mob.getNavigation().stop();
		if (dist > 4.5) {
			// re-close distance with a quick burst
			moveToward(entity, target, 0.52);
		} else if (dist < 1.8) {
			// too close — shuffle back slightly so attacks have room
			Vec3 away = entity.position().subtract(target.position()).normalize().scale(0.18);
			entity.setDeltaMovement(away.x, entity.getDeltaMovement().y, away.z);
			entity.hurtMarked = true;
		} else {
			applyBrake(entity);
		}
		// transition: leave pressure after 30-50 ticks to mix up tactics
		int minT = 28 + entity.level().random.nextInt(22);
		if (t >= minT) {
			double r = entity.level().random.nextDouble();
			if (r < 0.30)      switchMode(entity, M_BACK_OFF);
			else if (r < 0.55) switchMode(entity, M_FLANK);
			else if (r < 0.70 && state >= 1) switchMode(entity, M_JUMP_OVER);
			else               switchMode(entity, M_ENGAGE);
		}
	}

	// ── BACK_OFF: deliberately retreat — creates distance for ranged follow-up
	private static void modeBackOff(Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		mob.getNavigation().stop();
		double dx  = entity.getX() - target.getX();  // away from target
		double dz  = entity.getZ() - target.getZ();
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len > 0.01) {
			entity.setDeltaMovement(dx / len * 0.40, entity.getDeltaMovement().y, dz / len * 0.40);
			entity.hurtMarked = true;
		}
		// transition when far enough or timer expires
		if (dist > 12 || t >= 28) switchMode(entity, M_FLANK);
	}

	// ── FLANK: orbit target at ~7 blocks, circling CW or CCW ─────────────────
	// The key smart behaviour: Sukuna never stands still — he's always moving
	// so the player has to constantly re-aim.  Attacks fire during the orbit.
	private static void modeFlank(Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist, int state) {
		mob.getNavigation().stop();

		// radial vector: from target to entity
		double dx     = entity.getX() - target.getX();
		double dz     = entity.getZ() - target.getZ();
		double dist2d = Math.sqrt(dx * dx + dz * dz);
		if (dist2d < 0.01) { switchMode(entity, M_ENGAGE); return; }

		int dir = entity.getPersistentData().getInt(SUK_FLANK_DIR);
		if (dir == 0) {
			dir = entity.level().random.nextBoolean() ? 1 : -1;
			entity.getPersistentData().putInt(SUK_FLANK_DIR, dir);
		}

		double radX  =  dx / dist2d;          // unit away-from-target
		double radZ  =  dz / dist2d;
		double tangX = -radZ * dir;            // unit tangent (orbit direction)
		double tangZ =  radX * dir;

		// radial correction: hold ~7 block orbit radius
		double desired     = 7.0;
		double radialErr   = dist2d - desired; // +ve = too far out, -ve = too close
		double correction  = -Math.max(-0.28, Math.min(0.28, radialErr * 0.13));
		// correction sign: if too far (err>0) → correction<0 → radX*correction moves toward target ✓

		double orbSpeed = state >= 2 ? 0.48 : 0.40;
		entity.setDeltaMovement(
				tangX * orbSpeed + radX * correction,
				entity.getDeltaMovement().y,
				tangZ * orbSpeed + radZ * correction);
		entity.hurtMarked = true;

		// transition
		int duration = 38 + entity.level().random.nextInt(28);
		if (t >= duration) {
			double r = entity.level().random.nextDouble();
			entity.getPersistentData().putInt(SUK_FLANK_DIR, 0); // pick new direction next time
			if (r < 0.45)      switchMode(entity, M_ENGAGE);
			else if (r < 0.65) switchMode(entity, M_PRESSURE);
			else               switchMode(entity, M_BACK_OFF);
		}
	}

	// ── JUMP_OVER: leap over/past the target and land behind them ─────────────
	// Disorients the player — they expect a frontal attack but Sukuna ends up
	// behind them and immediately launches a melee combo on landing.
	private static void modeJumpOver(Level world, Entity entity, PathfinderMob mob,
			LivingEntity target, int t, double dist) {
		mob.getNavigation().stop();

		if (t == 1) {
			// launch: aim for a point 3 blocks past target in the same approach direction
			double dx  = target.getX() - entity.getX();
			double dz  = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			if (len < 0.01) { switchMode(entity, M_PRESSURE); return; }
			// overshoot factor so we land behind, not on top of, target
			double overshoot = (len + 3.0) / len;
			entity.setDeltaMovement(
					dx / len * overshoot * 0.52,
					1.55,
					dz / len * overshoot * 0.52);
			entity.hurtMarked = true;
		}
		// once we hit the ground after the jump, switch to pressure and flag immediate combo
		if (t > 5 && mob.onGround()) {
			entity.getPersistentData().putBoolean("suk_just_jumped", true);
			switchMode(entity, M_PRESSURE);
			return;
		}
		// safety: bail if airborne too long (hit a ceiling etc.)
		if (t >= 30) switchMode(entity, M_ENGAGE);
	}

	// ── DODGE: reactive sidestep, alternates left/right each time ────────────
	private static void modeDodge(Entity entity, PathfinderMob mob,
			LivingEntity target, int t) {
		mob.getNavigation().stop();

		double dx  = target.getX() - entity.getX();
		double dz  = target.getZ() - entity.getZ();
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len < 0.01) { switchMode(entity, M_ENGAGE); return; }

		double perpX = -dz / len;
		double perpZ =  dx / len;
		int    dir   = entity.getPersistentData().getInt(SUK_DODGE_DIR);

		if (t == 1) {
			// explosive initial burst sideways + slight backward
			double backX = -dx / len * 0.18;
			double backZ = -dz / len * 0.18;
			entity.setDeltaMovement(perpX * dir * 0.90 + backX, 0.22, perpZ * dir * 0.90 + backZ);
		} else if (t <= 8) {
			// carry momentum with gentle perpendicular top-up
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(
					cur.x * 0.72 + perpX * dir * 0.05,
					cur.y,
					cur.z * 0.72 + perpZ * dir * 0.05);
		} else {
			// decelerate
			Vec3 cur = entity.getDeltaMovement();
			entity.setDeltaMovement(cur.x * 0.35, cur.y, cur.z * 0.35);
		}
		entity.hurtMarked = true;

		if (t >= 13) switchMode(entity, entity.distanceTo(target) <= 5 ? M_PRESSURE : M_ENGAGE);
	}

	// ── reactive dodge on HP drop ──────────────────────────────────────────────
	private static void maybeReactiveDodge(Entity entity) {
		if (entity.getPersistentData().getInt(SUK_DODGE_CD) > 0) return;
		String cur = entity.getPersistentData().getString(SUK_MODE);
		if (M_DODGE.equals(cur) || M_JUMP_OVER.equals(cur)) return;
		// flip dodge direction each time so it alternates L/R
		int dir = entity.getPersistentData().getInt(SUK_DODGE_DIR);
		entity.getPersistentData().putInt(SUK_DODGE_DIR, dir >= 0 ? -1 : 1);
		switchMode(entity, M_DODGE);
		entity.getPersistentData().putInt(SUK_DODGE_CD, 38);
	}

	// ── mode helpers ───────────────────────────────────────────────────────────
	private static void switchMode(Entity entity, String newMode) {
		entity.getPersistentData().putString(SUK_MODE, newMode);
		entity.getPersistentData().putInt(SUK_MODE_T, 0);
		entity.getPersistentData().putInt(SUK_APP_SIDE, 0);   // reset arc side on every switch
	}

	private static String chooseMode(Entity entity, double dist, int state, Level world) {
		double r = world.getRandom().nextDouble();
		if (state == 0) {
			return dist > 8 ? M_ENGAGE : M_PRESSURE;
		} else if (state == 1) {
			if (dist > 10) return r < 0.55 ? M_ENGAGE : M_FLANK;
			if (dist > 4)  return r < 0.35 ? M_PRESSURE : (r < 0.65 ? M_FLANK : M_BACK_OFF);
			return r < 0.50 ? M_PRESSURE : (r < 0.80 ? M_JUMP_OVER : M_FLANK);
		} else { // state 2 — most aggressive
			if (dist > 12) return r < 0.45 ? M_ENGAGE : M_FLANK;
			if (dist > 4)  return r < 0.30 ? M_PRESSURE : (r < 0.55 ? M_FLANK : (r < 0.75 ? M_BACK_OFF : M_JUMP_OVER));
			return r < 0.35 ? M_PRESSURE : (r < 0.65 ? M_JUMP_OVER : M_BACK_OFF);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// AERIAL PURSUIT
	// ═════════════════════════════════════════════════════════════════════════
	private static void handleAerial(Level world, Entity entity, PathfinderMob mob, LivingEntity target) {
		int dt = entity.getPersistentData().getInt(AI_DASH_TELE);
		if (dt > 0) {
			entity.getPersistentData().putInt(AI_DASH_TELE, dt - 1);
			applyBrake(entity);
			if (dt == 1) {
				double dx = target.getX()-entity.getX(), dy = (target.getY()+target.getBbHeight()*0.5)-(entity.getY()+entity.getBbHeight()*0.5), dz = target.getZ()-entity.getZ();
				double l = Math.sqrt(dx*dx+dy*dy+dz*dz); if (l < 0.01) l = 1;
				entity.setDeltaMovement(dx/l*3.2, dy/l*3.2, dz/l*3.2); entity.hurtMarked = true;
			}
			return;
		}
		double vg = target.getY() - entity.getY();
		int dc = entity.getPersistentData().getInt(AI_DASH_COOLDOWN);
		if (vg <= 15) {
			applyLeap(entity, target);
			entity.getPersistentData().putInt(AI_DASH_COOLDOWN, 22);
		} else if (dc <= 0) {
			entity.getPersistentData().putInt(AI_DASH_TELE, TELE_TICKS);
			entity.getPersistentData().putInt(AI_DASH_COOLDOWN, DASH_COOLDOWN);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// ABILITIES — mode-aware attack selection
	// ═════════════════════════════════════════════════════════════════════════
	private static void handleAbilities(Level world, Entity entity, PathfinderMob mob, LivingEntity target) {
		String action = entity.getPersistentData().getString(AI_ACTION);
		if (ACT_RCT.equals(action) || ACT_BLOCK.equals(action) || ACT_BURNOUT.equals(action))
			return;

		// always advance in-progress multi-tick attacks
		tickCleave(world, entity, target);
		tickSlashCombo(world, entity, mob, target);
		tickBurst(world, entity, target);

		if (entity.getPersistentData().getBoolean(AI_DOMAIN_AMP)) {
			tryMelee(world, entity, mob, target);
			return;
		}

		String mode    = entity.getPersistentData().getString(SUK_MODE);
		int    state   = entity.getPersistentData().getInt(AI_STATE);
		double dist    = entity.distanceTo(target);
		double vertGap = target.getY() - entity.getY();

		// no new attacks during the jump arc or dodge
		if (M_JUMP_OVER.equals(mode) || M_DODGE.equals(mode)) return;

		// post-jump-landing free combo (cooldown ignored — this is the payoff)
		if (entity.getPersistentData().getBoolean("suk_just_jumped")) {
			entity.getPersistentData().putBoolean("suk_just_jumped", false);
			entity.getPersistentData().putInt(AI_SC_COUNT, 4);
			entity.getPersistentData().putInt(AI_SC_TIMER, 1);
			return;
		}

		// shrine deploy
		if (state >= 1 && entity.getPersistentData().getInt(CD_SHRINE) <= 0
				&& entity.getPersistentData().getInt(AI_BURNOUT_TIMER) <= 0
				&& !DomainCollapseManualProcedure.hasActiveDomain(world, entity)) {
			MalevolentShrineSummonProcedure.execute(world, entity);
			playSound(world, entity, "jjk_strongest:sukuna_domain_act", 1.0f, 1.0f);
			entity.getPersistentData().putInt(CD_SHRINE, 9999);
			entity.getPersistentData().putBoolean("ai_had_domain", true);
		}
		checkDomainFuga(world, entity, target, state);

		// aerial shots
		if (vertGap > 4 && dist > 15) {
			faceTarget(entity, target);
			if (entity.getPersistentData().getInt(CD_DISMANTLE) <= 0)      fireDismantle(world, entity, false);
			else if (entity.getPersistentData().getInt(CD_FUGA) <= 0)      fireFuga(entity, state);
			return;
		}

		// ── mode-specific attack logic ────────────────────────────────────────
		if (M_PRESSURE.equals(mode)) {
			if (dist <= 3.5) {
				if (comboReady(entity)) startSlashCombo(entity);
				else tryMelee(world, entity, mob, target);
			} else if (dist <= 8 && state >= 1) {
				// punish backing-away with cleave fan
				if (cleaveReady(entity)) startCleave(entity);
				else if (entity.getPersistentData().getInt(CD_DISMANTLE) <= 0) fireDismantle(world, entity, false);
			}
			return;
		}

		if (M_BACK_OFF.equals(mode)) {
			// fire while retreating — keep pressure without being close
			if (entity.getPersistentData().getInt(CD_DISMANTLE) <= 0) fireDismantle(world, entity, false);
			return;
		}

		if (M_FLANK.equals(mode)) {
			// dismantle and burst while circling
			if (entity.getPersistentData().getInt(CD_DISMANTLE) <= 0) {
				faceTarget(entity, target); fireDismantle(world, entity, false);
			}
			if (state >= 1 && entity.getPersistentData().getInt(AI_BURST_TIMER) <= 0
					&& entity.level().random.nextInt(100) < (state >= 2 ? 8 : 5)) {
				entity.getPersistentData().putInt(AI_BURST_COUNT, 5 + entity.level().random.nextInt(4));
				entity.getPersistentData().putInt(AI_BURST_TIMER, 1);
			}
			if (state >= 2 && dist > 8 && entity.getPersistentData().getInt(CD_FUGA) <= 0) {
				faceTarget(entity, target); fireFuga(entity, state);
			}
			return;
		}

		// ── ENGAGE / default: standard attack ladder ───────────────────────────
		if (dist <= 3.5) {
			if (comboReady(entity)) startSlashCombo(entity);
			else tryMelee(world, entity, mob, target);
			return;
		}
		// burst trigger
		if (state >= 1 && entity.getPersistentData().getInt(AI_BURST_TIMER) <= 0
				&& entity.level().random.nextInt(100) < (state >= 2 ? 8 : 5)) {
			entity.getPersistentData().putInt(AI_BURST_COUNT, 5 + entity.level().random.nextInt(4));
			entity.getPersistentData().putInt(AI_BURST_TIMER, 1);
		}
		if (dist <= 16) {
			if (state >= 1 && cleaveReady(entity)) { startCleave(entity); return; }
			if (entity.getPersistentData().getInt(CD_DISMANTLE) <= 0 && (state >= 1 || dist > 12)) {
				fireDismantle(world, entity, false); return;
			}
		}
		if (dist > 16) {
			if (entity.getPersistentData().getInt(CD_DISMANTLE) <= 0) { fireDismantle(world, entity, false); return; }
			if (entity.getPersistentData().getInt(CD_FUGA) <= 0 && state >= 1) { fireFuga(entity, state); return; }
		}
		if (state >= 2 && dist > 10 && entity.getPersistentData().getInt(CD_FUGA) <= 0)
			fireFuga(entity, state);
	}

	// ═════════════════════════════════════════════════════════════════════════
	// MULTI-TICK ATTACKS
	// ═════════════════════════════════════════════════════════════════════════

	// ── 3-shot cleave fan ─────────────────────────────────────────────────────
	private static boolean cleaveReady(Entity entity) {
		return entity.getPersistentData().getInt(CD_CLEAVE) <= 0
				&& entity.getPersistentData().getInt(AI_CLEAVE_COUNT) <= 0
				&& entity.getPersistentData().getInt(AI_CLEAVE_TIMER) <= 0;
	}

	private static void startCleave(Entity entity) {
		entity.getPersistentData().putInt(AI_CLEAVE_COUNT, 3);
		entity.getPersistentData().putInt(AI_CLEAVE_TIMER, 1);
		entity.getPersistentData().putInt(CD_CLEAVE, 65);
	}

	private static void tickCleave(Level world, Entity entity, LivingEntity target) {
		int ct = entity.getPersistentData().getInt(AI_CLEAVE_TIMER);
		int cc = entity.getPersistentData().getInt(AI_CLEAVE_COUNT);
		if (ct <= 0 || cc <= 0) return;
		ct--;
		entity.getPersistentData().putInt(AI_CLEAVE_TIMER, ct);
		if (ct > 0) return;

		faceTarget(entity, target);
		int   idx   = 3 - cc;  // 0, 1, 2
		float yOff  = (idx == 0) ? -12f : (idx == 2) ? 12f : 0f;
		float origY = entity.getYRot(), origYO = entity.yRotO;
		entity.setYRot(origY + yOff); entity.setYHeadRot(origY + yOff); entity.yRotO = origY + yOff;
		entity.getPersistentData().putDouble("TechniquePower", 1.5);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		ShootDismantleTravelProcedure.execute(world, entity,
				ReturnOutputDismantleProcedure.execute(world, entity), 1.5, false);
		entity.setYRot(origY); entity.setYHeadRot(origY); entity.yRotO = origYO;
		entity.getPersistentData().putInt(AI_CLEAVE_TIMER, 4);
		entity.getPersistentData().putInt(AI_CLEAVE_COUNT, cc - 1);
		entity.getPersistentData().putInt(CD_DISMANTLE, 4);
	}

	// ── 3-hit slash combo (close range) ──────────────────────────────────────
	private static boolean comboReady(Entity entity) {
		return entity.getPersistentData().getInt(CD_SC) <= 0
				&& entity.getPersistentData().getInt(AI_SC_COUNT) <= 0
				&& entity.getPersistentData().getInt(AI_SC_TIMER) <= 0;
	}

	private static void startSlashCombo(Entity entity) {
		entity.getPersistentData().putInt(AI_SC_COUNT, 3);
		entity.getPersistentData().putInt(AI_SC_TIMER, 1);
		entity.getPersistentData().putInt(CD_SC, 30);
	}

	private static void tickSlashCombo(Level world, Entity entity, PathfinderMob mob, LivingEntity target) {
		int st = entity.getPersistentData().getInt(AI_SC_TIMER);
		int sc = entity.getPersistentData().getInt(AI_SC_COUNT);
		if (st <= 0 || sc <= 0) return;
		st--;
		entity.getPersistentData().putInt(AI_SC_TIMER, st);
		if (st > 0) return;
		faceTarget(entity, target);
		moveToward(entity, target, 0.65);
		if (entity.distanceTo(target) <= 4.5) {
			MeleePunchProcedure.execute(world, entity);
			entity.getPersistentData().putInt(CD_MELEE, 4);
		}
		entity.getPersistentData().putInt(AI_SC_TIMER, 4);
		entity.getPersistentData().putInt(AI_SC_COUNT, sc - 1);
	}

	// ── dismantle burst (rapid-fire) ──────────────────────────────────────────
	private static void tickBurst(Level world, Entity entity, LivingEntity target) {
		int bt = entity.getPersistentData().getInt(AI_BURST_TIMER);
		int bc = entity.getPersistentData().getInt(AI_BURST_COUNT);
		if (bt <= 0 || bc <= 0) return;
		bt--;
		entity.getPersistentData().putInt(AI_BURST_TIMER, bt);
		if (bt > 0) return;
		faceTarget(entity, target);
		entity.getPersistentData().putDouble("TechniquePower", 1.3);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		ShootDismantleTravelProcedure.execute(world, entity,
				ReturnOutputDismantleProcedure.execute(world, entity), 1.3, false);
		int gap = 3 + entity.level().random.nextInt(3);
		entity.getPersistentData().putInt(AI_BURST_TIMER, gap);
		entity.getPersistentData().putInt(AI_BURST_COUNT, bc - 1);
		entity.getPersistentData().putInt(CD_DISMANTLE, gap + 2);
	}

	// ── regular melee ─────────────────────────────────────────────────────────
	private static void tryMelee(Level world, Entity entity, PathfinderMob mob, LivingEntity target) {
		if (entity.getPersistentData().getInt(CD_MELEE) <= 0) {
			MeleePunchProcedure.execute(world, entity);
			entity.getPersistentData().putInt(CD_MELEE, 10);
		} else if (entity.getPersistentData().getInt(CD_UPPERCUT) <= 0) {
			MeleeUppercutProcedure.execute(world, entity);
			entity.getPersistentData().putInt(CD_UPPERCUT, 20);
		}
	}

	// ── fire helpers ──────────────────────────────────────────────────────────
	private static void fireDismantle(Level world, Entity entity, boolean breakBlocks) {
		entity.getPersistentData().putDouble("TechniquePower", 1.5);
		entity.getPersistentData().putDouble("ChantCounter", 0);
		ShootDismantleTravelProcedure.execute(world, entity,
				ReturnOutputDismantleProcedure.execute(world, entity), 1.5, breakBlocks);
		entity.getPersistentData().putInt(CD_DISMANTLE, 22);
	}

	private static void fireFuga(Entity entity, int state) {
		FlameArrowShootExecuteProcedure.execute(entity);
		entity.getPersistentData().putInt(CD_FUGA, state >= 2 ? 150 : 360);
	}

	// ═════════════════════════════════════════════════════════════════════════
	// DOMAIN FUGA CHECK
	// ═════════════════════════════════════════════════════════════════════════
	private static void checkDomainFuga(Level world, Entity entity, LivingEntity target, int state) {
		if (!(world instanceof net.minecraft.server.level.ServerLevel sl)) return;
		for (MalevolentShrineEntity shrine : sl.getEntitiesOfClass(MalevolentShrineEntity.class,
				AABB.ofSize(entity.position(), 260, 260, 260), e -> true)) {
			CompoundTag d = shrine.getPersistentData();
			if (!d.getString("ownerUUID").equals(entity.getStringUUID())) continue;
			if (d.getBoolean("fugaTriggered")) continue;
			int trig = state >= 2 ? 280 : 400;
			if (d.getInt("domainLifetimeTicks") >= trig && target.isAlive()) {
				FugaDomainExplosionExecuteProcedure.execute(world, entity);
				entity.getPersistentData().putInt(AI_BURNOUT_TIMER, BURNOUT_DURATION);
				entity.getPersistentData().putString(AI_ACTION, ACT_BURNOUT);
			}
			return;
		}
		if (entity.getPersistentData().getBoolean("ai_had_domain")) {
			if (!DomainCollapseManualProcedure.hasActiveDomain(world, entity)) {
				entity.getPersistentData().putBoolean("ai_had_domain", false);
				if (entity.getPersistentData().getInt(AI_BURNOUT_TIMER) <= 0) {
					entity.getPersistentData().putInt(AI_BURNOUT_TIMER, BURNOUT_DURATION);
					entity.getPersistentData().putString(AI_ACTION, ACT_BURNOUT);
				}
			}
		} else if (DomainCollapseManualProcedure.hasActiveDomain(world, entity)) {
			entity.getPersistentData().putBoolean("ai_had_domain", true);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// PRE-EMPTIVE DOMAIN COUNTER
	// ═════════════════════════════════════════════════════════════════════════
	/**
	 * Scans for a hostile Unlimited Void that is still forming (isExpanding or
	 * isPostLines phase). If found, Sukuna immediately deploys Malevolent Shrine
	 * so the two domains clash before the sure-hit wave of Unlimited Void fires.
	 * CD_SHRINE is set to 9999 so the normal ability loop won't double-fire.
	 */
	private static void checkForHostileDomain(Level world, Entity entity, PathfinderMob mob) {
		if (!(world instanceof net.minecraft.server.level.ServerLevel sl)) return;
		// don't fire if shrine is already on cooldown / burning out
		if (entity.getPersistentData().getInt(CD_SHRINE) > 0) return;
		if (entity.getPersistentData().getInt(AI_BURNOUT_TIMER) > 0) return;
		// don't fire if Sukuna already has an active domain
		if (DomainCollapseManualProcedure.hasActiveDomain(world, entity)) return;
		AABB scanBox = AABB.ofSize(entity.position(), 140, 140, 140);
		for (DomainUVEntity domain : sl.getEntitiesOfClass(DomainUVEntity.class, scanBox, e -> true)) {
			CompoundTag dData = domain.getPersistentData();
			// ignore domains Sukuna himself owns
			if (entity.getStringUUID().equals(dData.getString("ownerUUID"))) continue;
			// react during isExpanding (first 40 ticks) or isPostLines — before isActive
			if (dData.getBoolean("isExpanding") || dData.getBoolean("isPostLines")
					|| dData.getBoolean("isActive") || dData.getBoolean("isClashing")) {
				MalevolentShrineSummonProcedure.execute(world, entity);
				playSound(world, entity, "jjk_strongest:sukuna_domain_act", 1.0f, 1.0f);
				// lock the CD so the normal shrine check won't re-fire
				entity.getPersistentData().putInt(CD_SHRINE, 9999);
				entity.getPersistentData().putBoolean("ai_had_domain", true);
				return;
			}
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// MOVEMENT PRIMITIVES
	// ═════════════════════════════════════════════════════════════════════════
	private static void faceTarget(Entity entity, LivingEntity target) {
		double dx    = target.getX() - entity.getX();
		double dz    = target.getZ() - entity.getZ();
		double dy    = (target.getY() + target.getBbHeight() * 0.5) - (entity.getY() + entity.getBbHeight() * 0.5);
		double hDist = Math.sqrt(dx * dx + dz * dz);
		float  yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float  pitch = (float) -Math.toDegrees(Math.atan2(dy, hDist));
		entity.setYRot(yaw); entity.setYHeadRot(yaw); entity.yRotO = yaw; entity.setXRot(pitch);
	}

	private static void moveToward(Entity entity, LivingEntity target, double speed) {
		double dx = target.getX()-entity.getX(), dz = target.getZ()-entity.getZ();
		double l  = Math.sqrt(dx*dx+dz*dz); if (l < 0.01) return;
		entity.setDeltaMovement(dx/l*speed, entity.getDeltaMovement().y, dz/l*speed);
		entity.hurtMarked = true;
	}

	private static void applyBrake(Entity entity) {
		Vec3 v = entity.getDeltaMovement();
		entity.setDeltaMovement(v.x * 0.2, v.y, v.z * 0.2);
	}

	private static void applyLeap(Entity entity, LivingEntity target) {
		double dx = target.getX()-entity.getX(), dz = target.getZ()-entity.getZ();
		double l  = Math.sqrt(dx*dx+dz*dz); if (l < 0.01) l = 1;
		entity.setDeltaMovement(dx/l*1.9, 1.1, dz/l*1.9); entity.hurtMarked = true;
	}

	private static void applyLunge(Entity entity, LivingEntity target) {
		double dx = target.getX()-entity.getX(), dz = target.getZ()-entity.getZ();
		double l  = Math.sqrt(dx*dx+dz*dz); if (l < 0.01) l = 1;
		entity.setDeltaMovement(dx/l*2.8, 0.35, dz/l*2.8); entity.hurtMarked = true;
	}

	// ═════════════════════════════════════════════════════════════════════════
	// COOLDOWN TICKING
	// ═════════════════════════════════════════════════════════════════════════
	private static void tickCooldowns(Entity entity) {
		for (String k : new String[]{ CD_DISMANTLE, CD_FUGA, CD_SHRINE, CD_MELEE, CD_UPPERCUT,
				CD_CLEAVE, CD_SC, AI_LUNGE_COOLDOWN, AI_DASH_COOLDOWN, AI_RCT_COOLDOWN, SUK_DODGE_CD }) {
			int v = entity.getPersistentData().getInt(k);
			if (v > 0) entity.getPersistentData().putInt(k, v - 1);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// SOUND HELPER
	// ═════════════════════════════════════════════════════════════════════════
	private static void playSound(Level world, Entity entity, String id, float vol, float pitch) {
		if (!world.isClientSide())
			world.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
					BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id)), SoundSource.HOSTILE, vol, pitch);
	}
}
