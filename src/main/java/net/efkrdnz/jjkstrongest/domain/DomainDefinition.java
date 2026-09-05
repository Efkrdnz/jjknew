package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

import javax.annotation.Nullable;

/**
 * Everything two domains genuinely have in common, and nothing else.
 *
 * <p>The line this draws is deliberate and it is the whole point of the type:
 * <strong>mechanics are shared, looks are not</strong>. How big a domain is, how long it
 * takes to open, whether it has a surface, how that surface fails and what it does to the
 * people caught inside are the same questions for every technique, and answering them in
 * one place is what lets a new domain be a definition plus a renderer rather than a second
 * copy of the phase machine.
 *
 * <p>What is <em>not</em> here: shaders, render types, palettes, textures, models,
 * particles, cast sounds, screen overlays, sky and fog. Unlimited Void and Malevolent
 * Shrine share how long a barrier holds and how it gives out; they share nothing whatever
 * about what that looks like. A domain that borrowed another's visuals would stop being a
 * different technique.
 *
 * @param id             stable name, for logs and save data
 * @param barrierKind    whether this domain has a surface at all
 * @param radius         the sphere's radius in blocks at full size
 * @param floorOffset    where the floor plane sits relative to the anchor's feet;
 *                       ignored by open domains, which have no interior to stand in
 * @param expansionTicks ticks spent growing to full size
 * @param settleTicks    ticks at full size before the domain turns hostile
 * @param collapseTicks  ticks spent shrinking while the world goes back
 * @param durationTicks  ticks the domain stays hostile once settled
 * @param maxLifetimeTicks a hard stop, whatever else is going on
 * @param shell          how the surface holds and fails; null for an open domain
 * @param collapse       when a damaged domain gives out
 * @param sureHit        the effect applied to everyone caught inside; null for a domain
 *                       whose output is not an effect, like the Shrine's slashes
 */
public record DomainDefinition(String id, DomainBarrierKind barrierKind, float radius, float floorOffset, int expansionTicks, int settleTicks, int collapseTicks, int durationTicks,
		int maxLifetimeTicks, @Nullable DomainShellProfile shell, CollapseRules collapse, @Nullable SureHit sureHit) {

	/**
	 * @param breachThreshold  holes tolerated before the grace clock starts
	 * @param destabiliseTicks grace between the barrier being holed and the domain failing
	 */
	public record CollapseRules(int breachThreshold, int destabiliseTicks) {
	}

	/**
	 * @param effect        what everyone inside is given
	 * @param amplifier     effect level, zero-based
	 * @param durationTicks how long each application lasts
	 * @param cadenceTicks  how often it is reapplied
	 */
	public record SureHit(Holder<MobEffect> effect, int amplifier, int durationTicks, int cadenceTicks) {
	}

	public boolean isClosed() {
		return barrierKind == DomainBarrierKind.CLOSED;
	}

	/** Ticks from cast to the domain turning hostile. */
	public int ticksToHostile() {
		return expansionTicks + settleTicks;
	}

	/**
	 * Unlimited Void.
	 *
	 * <p>Every number here was a private constant somewhere else: the radius and floor
	 * offset on the entity, the four timings in the tick procedure, the regen pair inside
	 * {@link DomainShell}, and the sure-hit's effect and level inline in the argument list
	 * of the procedure that applies it.
	 */
	public static final DomainDefinition UNLIMITED_VOID = new DomainDefinition("unlimited_void", DomainBarrierKind.CLOSED, 30.0f, -1.0f, 40, 40, 20, 600, 1200,
			new DomainShellProfile(60, 0.75f, 1.0f), new CollapseRules(0, 80), new SureHit(JjkStrongestModMobEffects.INFORMATION_OVERLOAD, 1, 200, 20));

	/**
	 * Malevolent Shrine.
	 *
	 * <p>No shell, because there is no surface: an open domain is beaten through its
	 * caster, not through a barrier. No sure-hit effect either — its output is the slashes,
	 * which are their own system. What it does share is the shape of a life: it opens, it
	 * settles, it runs, it closes.
	 */
	public static final DomainDefinition MALEVOLENT_SHRINE = new DomainDefinition("malevolent_shrine", DomainBarrierKind.OPEN, 100.0f, 0.0f, 40, 0, 20, 560, 1200, null, new CollapseRules(0, 0), null);
}
