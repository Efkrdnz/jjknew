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

	/** For a closed domain the shell; for an open one, how far its effects reach. */
	DomainSphere volume();

	DomainBarrierKind barrierKind();

	default boolean isClosed() {
		return barrierKind() == DomainBarrierKind.CLOSED;
	}

	/** The shell's damage grid, or null for an open domain, which has no surface. */
	default DomainShell shell() {
		return null;
	}
}
