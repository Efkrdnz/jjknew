package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

/**
 * What an NPC does while Information Overload is on it, which is nothing at all.
 *
 * <p>Every fighter in this mod already carried its own overload guard, and every one of them
 * leaked, because a guard part way down an AI procedure only stops the part of the tick below
 * it. Sukuna still turned to face you, still counter-punched out of his block and still healed
 * 1.4 HP a tick through RCT. Mahoraga was worse: his guard runs from {@code baseTick()}, and
 * vanilla runs {@code baseTick()} <em>before</em> {@code serverAiStep()}, so his melee and
 * stroll goals ticked afterwards and simply undid the {@code navigation.stop()} the guard had
 * just issued. He walked up and hit you through a domain that is supposed to have taken his
 * mind apart.
 *
 * <p>The one call that reaches goals is {@link Mob#setNoAi}: {@code Mob.isEffectiveAi()} is
 * {@code super.isEffectiveAi() && !isNoAi()}, and that gate sits above sensing, the target and
 * goal selectors, navigation, {@code customServerAiStep()} and the three controls. Setting it
 * stops all of them at once — including {@code customServerAiStep()}, which is where Sukuna's
 * and Gojo's entire AI is invoked from, so their leaks close with the same line.
 *
 * <p>{@code noAi} is serialised to NBT, which is the sharp edge here: a mob saved mid-freeze
 * would otherwise load permanently inert. So the freeze is only ever ours to lift if we know
 * we set it, which is what {@link #FROZEN} records, and the restore runs from
 * {@code baseTick()} — the one part of the tick vanilla runs regardless of {@code noAi}. A mob
 * that comes back from disk still frozen heals itself on the first tick after the effect goes.
 */
public final class DomainSuppression {

	/**
	 * Our own note that <em>we</em> are the ones holding this mob still.
	 *
	 * <p>Without it the restore would have to guess: a mob spawned with {@code NoAi:1} by a
	 * command or a spawn egg has every right to stay that way, and clearing the flag blindly
	 * once the effect ran out would quietly hand it its AI back.
	 */
	private static final String FROZEN = "jjk_uv_frozen";

	private DomainSuppression() {
	}

	/** Whether this mob is currently caught in a closed domain's sure-hit. */
	public static boolean isSuppressed(Mob mob) {
		return mob.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD);
	}

	/**
	 * Holds the mob still for one tick, or gives it back its AI.
	 *
	 * <p>Call this from {@code baseTick()}, and on an entity with a state machine of its own
	 * call it <em>after</em> that machine has run: the per-tick clearing below has to be the
	 * last word on what the mob thinks it is doing, or a hit landed this tick can still flip
	 * it into a dodge that plays out the moment the domain drops.
	 *
	 * @return true if the mob is frozen right now
	 */
	public static boolean tick(Mob mob) {
		if (mob.level().isClientSide())
			return false;
		if (!isSuppressed(mob)) {
			if (mob.getPersistentData().getBoolean(FROZEN)) {
				mob.getPersistentData().remove(FROZEN);
				mob.setNoAi(false);
			}
			return false;
		}
		if (!mob.getPersistentData().getBoolean(FROZEN)) {
			mob.getPersistentData().putBoolean(FROZEN, true);
			mob.setNoAi(true);
		}
		mob.setTarget(null);
		mob.setAggressive(false);
		mob.getNavigation().stop();
		// Anything that had it hovering — a launch, a sky combo — ends here. It falls.
		mob.setNoGravity(false);
		mob.setJumping(false);
		// travel() runs from aiStep() whether or not the AI does, and it accelerates off
		// these three. Zeroing the velocity alone would leave the mob creeping forward on
		// the last movement input its controls managed to set, decaying at 0.98 a tick.
		mob.setXxa(0.0f);
		mob.setYya(0.0f);
		mob.setZza(0.0f);
		mob.setSpeed(0.0f);
		mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
		clearAction(mob);
		return true;
	}

	/**
	 * Puts the mob's own state machine back to idle.
	 *
	 * <p>Each fighter keeps its current move in persistent data, and freezing one mid-attack
	 * without clearing it means the attack resumes from wherever it got to the instant the
	 * effect wears off — a barrage that fires four seconds late, out of a mob that has not
	 * moved since. Written by key so each mob only ever touches its own.
	 */
	private static void clearAction(Mob mob) {
		CompoundTag data = mob.getPersistentData();
		if (data.contains("ai_action"))
			data.putString("ai_action", "idle");
		if (data.contains("gojo_mode")) {
			data.putString("gojo_mode", "ENGAGE");
			data.putInt("gojo_mode_t", 0);
		}
		if (data.contains("maho_state")) {
			data.putString("maho_state", "IDLE");
			data.putInt("maho_t", 0);
		}
	}
}
