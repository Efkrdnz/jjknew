package net.efkrdnz.jjkstrongest.domain;

/**
 * Whether a domain has a surface.
 *
 * <p>This is the distinction the code never made, and it is the one that decides how two
 * domains meet. Malevolent Shrine is {@link #OPEN}: it projects slashes and damage over a
 * radius from a point and contains nothing, so there is no surface to hit and nothing to
 * stop anything coming in. Unlimited Void is {@link #CLOSED}: a real shell that holds
 * people in and keeps things out.
 *
 * <p>The rule that falls out of it, and the reason this enum exists rather than a pair of
 * special cases: <em>an open domain's output cannot cross a closed barrier.</em> Whatever
 * would have crossed is stopped at the surface and spent damaging it instead. A closed
 * domain's health is therefore its barrier; an open one has no barrier to attack, so it
 * has to be beaten through its caster.
 */
public enum DomainBarrierKind {
	/** No surface. Projects its effects over a volume and cannot contain anything. */
	OPEN,
	/** A real shell: it contains what is inside and stops what is outside. */
	CLOSED
}
