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
 * 1.4 HP a tick. Mahoraga was worse: his guard runs from {@code baseTick()}, and vanilla runs
 * {@code baseTick()} <em>before</em> {@code serverAiStep()}, so his melee and stroll goals
 * ticked afterwards and simply undid the {@code navigation.stop()} the guard had just issued.
 * He walked up and hit you through a domain that is supposed to have taken his mind apart.
 *
 * <p>The switch that reaches all of that is {@link net.minecraft.world.entity.LivingEntity}'s
 * {@code isImmobile()}, which the three fighters override to consult {@link #isSuppressed}.
 * {@code aiStep()} reads it directly:
 *
 * <pre>{@code
 * if (this.isImmobile()) {
 *     this.jumping = false; this.xxa = 0.0F; this.zza = 0.0F;
 * } else if (this.isEffectiveAi()) {
 *     this.serverAiStep();
 * }
 * }</pre>
 *
 * <p>so sensing, both goal selectors, navigation, the three controls and
 * {@code customServerAiStep()} — which is where Sukuna's and Gojo's entire AI is invoked from —
 * all stop together.
 *
 * <p><strong>Deliberately not {@code setNoAi(true)}</strong>, which is what this used to be and
 * which left them hanging in mid-air. The whole body of {@code LivingEntity#travel} sits inside
 * {@code if (this.isControlledByLocalInstance())}; that resolves through
 * {@code Entity#isControlledByLocalInstance} to {@code isEffectiveAi()}, and {@code Mob}
 * overrides that as {@code super.isEffectiveAi() && !isNoAi()}. So {@code noAi} takes out
 * gravity, drag and the {@code move()} call itself — and with {@code move()} goes
 * {@code Entity#collide}, which is where this mod's own domain floor lives, so a frozen mob
 * could not even fall onto it. {@code isImmobile()} leaves {@code isEffectiveAi()} alone and
 * costs none of that. It is also computed live rather than serialised, so unlike {@code noAi}
 * it cannot strand a mob that was saved to disk mid-freeze.
 */
public final class DomainSuppression {

	/**
	 * Left behind by the {@code setNoAi} version of this class.
	 *
	 * <p>Only ever cleared now, never written. The three fighters call {@code setNoAi(false)}
	 * in their constructors, but that runs before NBT is read, so a mob saved while the old
	 * freeze held it would load inert and stay that way. Safe to delete once no world that
	 * ran the previous build is still in use.
	 */
	private static final String LEGACY_FROZEN = "jjk_uv_frozen";

	private DomainSuppression() {
	}

	/**
	 * Whether this mob is currently caught in a closed domain's sure-hit.
	 *
	 * <p>Read from {@code isImmobile()} on every fighter, so it runs several times a tick and
	 * has to stay this cheap.
	 */
	public static boolean isSuppressed(Mob mob) {
		return mob.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD);
	}

	/**
	 * Holds the mob still for one tick.
	 *
	 * <p>Call this from {@code baseTick()}, and on an entity with a state machine of its own
	 * call it <em>after</em> that machine has run: the clearing below has to be the last word
	 * on what the mob thinks it is doing, or a move it was committed to plays out the moment
	 * the domain drops.
	 *
	 * @return true if the mob is frozen right now
	 */
	public static boolean tick(Mob mob) {
		if (mob.level().isClientSide())
			return false;
		unstick(mob);
		if (!isSuppressed(mob))
			return false;
		mob.setTarget(null);
		mob.setAggressive(false);
		mob.getNavigation().stop();
		// Whatever had it hovering — Gojo's aerial mode, a sky combo — ends here, and now that
		// travel() is running again this is what actually drops him. His own guard used to
		// clear this, but that guard lives in customServerAiStep() and no longer runs at all.
		mob.setNoGravity(false);
		// aiStep()'s immobile branch zeroes jumping, xxa and zza for us, in this same tick,
		// just after baseTick(). It does not zero yya, and travel() still reads it.
		mob.setYya(0.0f);
		mob.setSpeed(0.0f);
		// Horizontal motion stops dead rather than coasting out the rest of a leap; vertical
		// motion is allowed to point down and nothing else, so a mob frozen on the way up
		// starts falling immediately instead of finishing an eight-block arc. The cost is that
		// hits cannot pop a frozen mob upward any more, which is the reading we want: dead
		// weight until the domain lets go of it.
		mob.setDeltaMovement(0.0, Math.min(0.0, mob.getDeltaMovement().y), 0.0);
		clearAction(mob);
		return true;
	}

	/** Frees a mob left inert by the {@code setNoAi} version. Does nothing on a clean world. */
	private static void unstick(Mob mob) {
		if (!mob.getPersistentData().getBoolean(LEGACY_FROZEN))
			return;
		mob.getPersistentData().remove(LEGACY_FROZEN);
		mob.setNoAi(false);
	}

	/**
	 * Puts the mob's own state machine back to idle.
	 *
	 * <p>Each fighter keeps its current move in persistent data, and freezing one mid-attack
	 * without clearing it means the attack resumes from wherever it got to the instant the
	 * effect wears off — a barrage that fires four seconds late, out of a mob that has not
	 * moved since. Written by key so each mob only ever touches its own.
	 *
	 * <p>Sukuna needs two of them: {@code ai_action} is his action slot, and dropping it is
	 * what stops a block or a reverse-cursed-technique burst carrying through the freeze, while
	 * {@code suk_mode} is the behavioural mode that decides whether he is mid-leap. Clearing
	 * only the first one — which is what this did at first — left a frozen Sukuna to finish his
	 * jump the moment the domain closed.
	 */
	private static void clearAction(Mob mob) {
		CompoundTag data = mob.getPersistentData();
		if (data.contains("ai_action"))
			data.putString("ai_action", "idle");
		if (data.contains("suk_mode")) {
			data.putString("suk_mode", "ENGAGE");
			data.putInt("suk_mode_t", 0);
			data.putBoolean("suk_just_jumped", false);
		}
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
