package net.efkrdnz.jjkstrongest.domain;

/**
 * Anything that projects a domain: implemented by the domain anchor entities.
 *
 * <p>Exists so the interaction rules can be written once against "a domain" rather than
 * against two unrelated entity classes. Before this, Malevolent Shrine had no shape any
 * other code could ask about at all — its reach was a private {@code RADIUS = 100.0}
 * constant inside its own tick procedure.
 */
public interface DomainSource {

	/**
	 * The mechanics this domain runs on: size, timings, how its barrier holds, what it
	 * does to whoever is caught in it. Not its looks — see {@link DomainDefinition}.
	 */
	DomainDefinition definition();

	/** For a closed domain the shell; for an open one, how far its effects reach. */
	DomainSphere volume();

	default DomainBarrierKind barrierKind() {
		return definition().barrierKind();
	}

	default boolean isClosed() {
		return barrierKind() == DomainBarrierKind.CLOSED;
	}

	/** The shell's damage grid, or null for an open domain, which has no surface. */
	default DomainShell shell() {
		return null;
	}

	/**
	 * Whether this domain is still standing.
	 *
	 * <p>Declared rather than defaulted: both implementers are entities, so
	 * {@code Entity#isAlive} satisfies it, and leaving it abstract means a future
	 * implementer that is not an entity has to answer honestly rather than inherit "yes".
	 */
	boolean isAlive();

	/**
	 * The sorcerer holding this domain, as a UUID string, or empty.
	 *
	 * <p>Named apart from anything on {@code Entity} on purpose: this is the domain's
	 * caster, not the entity's own identity.
	 */
	String domainOwnerUUID();

	/**
	 * The radius this domain reaches at full size — not what it happens to be mid-expansion.
	 *
	 * <p>Defaults to the definition's figure; a domain whose size varies with its caster
	 * overrides it with whatever it is actually growing toward.
	 */
	default double fullRadius() {
		return definition().radius();
	}
}
