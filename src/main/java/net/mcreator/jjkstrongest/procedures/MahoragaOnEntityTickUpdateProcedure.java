package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

import java.util.List;
import java.util.Comparator;

public class MahoragaOnEntityTickUpdateProcedure {
	// main tick brain
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (world == null || entity == null)
			return;
		if (!(world instanceof ServerLevel level))
			return;
		if (entity instanceof LivingEntity le) {
			if (le.isDeadOrDying() || le.getHealth() <= 0) {
				return;
			}
		}
		// timers
		decInt(entity, "maho_cd_global");
		decInt(entity, "maho_cd_barrage");
		decInt(entity, "maho_cd_slam");
		decInt(entity, "maho_cd_dash");
		decInt(entity, "maho_cd_cannon");
		decInt(entity, "maho_cd_aura");
		decInt(entity, "maho_cd_strongjump");
		decInt(entity, "maho_cd_wallstep");
		decInt(entity, "maho_nav_delay");
		decInt(entity, "maho_brain_cd");
		decInt(entity, "maho_hurt_ticks");
		decInt(entity, "maho_cd_launch");
		decInt(entity, "maho_cd_adapt_heal");
		decInt(entity, "maho_cd_hop");
		// bf consume + decay
		MahoragaBlackFlashTickProcedure.execute(level, entity);
		// regen loop
		MahoragaPressureAndRegenTickProcedure.execute(level, entity);
		// water escape interrupt
		MahoragaEscapeWaterProcedure.execute(level, entity);
		// break ticks (strong jump tunnel)
		int breakTicks = entity.getPersistentData().getInt("maho_break_ticks");
		if (breakTicks > 0) {
			MahoragaBreakBlocksByHardnessProcedure.execute(level, entity.getX(), entity.getY(), entity.getZ(), 2);
			entity.getPersistentData().putInt("maho_break_ticks", breakTicks - 1);
		}
		// clear one-shot anim
		int ac = entity.getPersistentData().getInt("maho_anim_clear");
		if (ac > 0) {
			ac--;
			entity.getPersistentData().putInt("maho_anim_clear", ac);
			if (ac == 0 && entity instanceof MahoragaEntity _m) {
				_m.setAnimation("undefined");
			}
		}
		// wall break after delay
		int wallDelay = entity.getPersistentData().getInt("maho_wall_break_delay");
		if (wallDelay > 0) {
			wallDelay--;
			entity.getPersistentData().putInt("maho_wall_break_delay", wallDelay);
			if (wallDelay == 0) {
				MahoragaBreakBlocksByHardnessProcedure.execute(level, entity.getX(), entity.getY(), entity.getZ(), 2);
			}
		}
		// acquire target if needed
		LivingEntity target = getOrFindTarget(level, entity);
		// if no target, idle safely (important: before any target.getPersistentData())
		if (target == null) {
			if (entity instanceof Mob mob)
				mob.setAggressive(false);
			entity.getPersistentData().putString("maho_state", "IDLE");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		// if target is creative/spectator, drop it safely
		if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
			if (entity instanceof Mob mob) {
				mob.setTarget(null);
				mob.setAggressive(false);
			}
			entity.getPersistentData().putString("maho_state", "IDLE");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		// launch fall cancel ticks (safe now)
		int lt = target.getPersistentData().getInt("maho_launch_ticks");
		if (lt > 0) {
			target.getPersistentData().putInt("maho_launch_ticks", lt - 1);
			target.fallDistance = 0;
		}
		if (entity instanceof Mob mob) {
			mob.setAggressive(true);
		}
		// chanting read (player nbt)
		int chantTicks = 0;
		int chanting = 0;
		if (target instanceof Player) {
			chanting = target.getPersistentData().getInt("TechniquePower");
			chantTicks = target.getPersistentData().getInt("ChantCounter");
		}
		// stuck detection
		updateStuck(entity);
		String state = entity.getPersistentData().getString("maho_state");
		if (state == null || state.isEmpty())
			state = "TARGETING";
		// hard interrupts
		if (chanting == 1 && chantTicks >= 40 && !isAttackState(state)) {
			entity.getPersistentData().putString("maho_state", "DEFENSIVE");
			entity.getPersistentData().putInt("maho_t", 0);
			state = "DEFENSIVE";
		}
		// execute states
		switch (state) {
			case "BARRAGE" -> MahoragaBarrageProcedure.execute(level, entity, target);
			case "SLAM" -> MahoragaGroundSlamProcedure.execute(level, entity, target);
			case "CANNON" -> MahoragaAirCannonProcedure.execute(level, entity, target);
			case "DASH" -> MahoragaDashStrikeProcedure.execute(level, entity, target);
			case "STRONG_JUMP" -> MahoragaStrongJumpProcedure.execute(level, entity, target);
			case "WALL_STEP" -> MahoragaWallStepProcedure.execute(level, entity, target);
			case "AURA_FARM" -> MahoragaAuraFarmProcedure.execute(level, entity, target);
			case "DEFENSIVE" -> doDefensive(level, entity, target, chanting, chantTicks);
			case "SKY_LAUNCH" -> MahoragaSkyLaunchProcedure.execute(level, entity, target);
			case "SKY_DIVE" -> MahoragaSkyDiveProcedure.execute(level, entity, target);
			default -> doTargeting(level, entity, target);
		}
	}

	private static void doTargeting(ServerLevel level, Entity entity, LivingEntity target) {
		double dx = target.getX() - entity.getX();
		double dy = target.getY() - entity.getY();
		double dz = target.getZ() - entity.getZ();
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		// vertical pursuit when target is above (open air hover)
		if (dy > 4.0) {
			// if very high, prefer cannon sometimes
			if (dy > 10.0 && entity.getPersistentData().getInt("maho_cd_cannon") <= 0) {
				entity.getPersistentData().putString("maho_state", "CANNON");
				entity.getPersistentData().putInt("maho_t", 0);
				return;
			}
			if (entity.getPersistentData().getInt("maho_cd_strongjump") <= 0) {
				entity.getPersistentData().putString("maho_state", "STRONG_JUMP");
				entity.getPersistentData().putInt("maho_t", 0);
				return;
			}
			if (entity.getPersistentData().getInt("maho_cd_hop") <= 0) {
				MahoragaHopUpProcedure.execute(level, entity, target);
				return;
			}
		}
		MahoragaFaceTargetProcedure.execute(entity, target);
		if (entity instanceof Mob mob) {
			int nav = entity.getPersistentData().getInt("maho_nav_delay");
			if (nav <= 0) {
				mob.getNavigation().moveTo(target, 1.15);
				entity.getPersistentData().putInt("maho_nav_delay", 6);
			}
		}
		int brain = entity.getPersistentData().getInt("maho_brain_cd");
		if (brain > 0)
			return;
		entity.getPersistentData().putInt("maho_brain_cd", 7);
		if (entity.getPersistentData().getInt("maho_stuck_ticks") > 15) {
			if (entity.getPersistentData().getInt("maho_cd_strongjump") <= 0) {
				entity.getPersistentData().putString("maho_state", "STRONG_JUMP");
				entity.getPersistentData().putInt("maho_t", 0);
				return;
			}
		}
		if (entity.horizontalCollision && dy > 2.0 && entity.getPersistentData().getInt("maho_cd_wallstep") <= 0) {
			entity.getPersistentData().putString("maho_state", "WALL_STEP");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (entity.getPersistentData().getInt("maho_cd_aura") <= 0 && dist > 6 && level.getRandom().nextDouble() < 0.02) {
			entity.getPersistentData().putString("maho_state", "AURA_FARM");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (entity.getPersistentData().getInt("maho_cd_global") > 0)
			return;
		if (dist <= 3.2 && entity.getPersistentData().getInt("maho_cd_barrage") <= 0) {
			entity.getPersistentData().putString("maho_state", "BARRAGE");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (dist <= 6.0 && entity.getPersistentData().getInt("maho_cd_slam") <= 0) {
			entity.getPersistentData().putString("maho_state", "SLAM");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (dist <= 11.5 && entity.getPersistentData().getInt("maho_cd_cannon") <= 0) {
			entity.getPersistentData().putString("maho_state", "CANNON");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (dist <= 12.5 && entity.getPersistentData().getInt("maho_cd_dash") <= 0) {
			entity.getPersistentData().putString("maho_state", "DASH");
			entity.getPersistentData().putInt("maho_t", 0);
			return;
		}
		if (dist <= 4.4 && target.onGround() && entity.getPersistentData().getInt("maho_cd_launch") <= 0) {
			if (level.getRandom().nextDouble() < 0.22) {
				entity.getPersistentData().putString("maho_state", "SKY_LAUNCH");
				entity.getPersistentData().putInt("maho_t", 0);
				return;
			}
		}
		entity.getPersistentData().putString("maho_state", "TARGETING");
	}

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
		if (chantTicks >= 60 && entity.getPersistentData().getInt("maho_cd_cannon") <= 0 && level.getRandom().nextDouble() < 0.2) {
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
		double pz = (target.getX() - entity.getX());
		double plen = Math.sqrt(px * px + pz * pz);
		if (plen > 0.001) {
			px /= plen;
			pz /= plen;
		}
		double keep = 7.5;
		double pushOut = (dist < keep) ? 0.35 : 0.0;
		double pullIn = (dist > keep + 2.0) ? 0.25 : 0.0;
		double vx = px * 0.35 * dir + dx / Math.max(dist, 0.001) * pushOut - dx / Math.max(dist, 0.001) * pullIn;
		double vz = pz * 0.35 * dir + dz / Math.max(dist, 0.001) * pushOut - dz / Math.max(dist, 0.001) * pullIn;
		entity.setDeltaMovement(vx, entity.getDeltaMovement().y * 0.2, vz);
		entity.hurtMarked = true;
		if (entity.getPersistentData().getInt("maho_cd_global") <= 0)
			entity.getPersistentData().putInt("maho_cd_global", 4);
	}

	private static boolean isAttackState(String s) {
		return "BARRAGE".equals(s) || "SLAM".equals(s) || "CANNON".equals(s) || "DASH".equals(s) || "STRONG_JUMP".equals(s) || "WALL_STEP".equals(s) || "SKY_LAUNCH".equals(s) || "SKY_DIVE".equals(s);
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
		if (d2 < 0.0025)
			stuck++;
		else
			stuck = 0;
		e.getPersistentData().putInt("maho_stuck_ticks", stuck);
		e.getPersistentData().putDouble("maho_lastx", e.getX());
		e.getPersistentData().putDouble("maho_lasty", e.getY());
		e.getPersistentData().putDouble("maho_lastz", e.getZ());
	}

	private static LivingEntity getOrFindTarget(ServerLevel level, Entity entity) {
		LivingEntity target = null;
		if (entity instanceof Mob mob)
			target = mob.getTarget();
		if (target != null && target.isAlive())
			return target;
		double r = 28;
		AABB box = new AABB(entity.blockPosition()).inflate(r, r, r);
		List<Player> players = level.getEntitiesOfClass(Player.class, box, p -> p.isAlive() && !p.isSpectator() && !p.isCreative()).stream().sorted(Comparator.comparingDouble(p -> p.distanceToSqr(entity))).toList();
		if (!players.isEmpty()) {
			Player p = players.get(0);
			if (entity instanceof Mob mob)
				mob.setTarget(p);
			return p;
		}
		return null;
	}
}
