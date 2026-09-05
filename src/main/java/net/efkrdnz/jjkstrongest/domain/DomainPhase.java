package net.efkrdnz.jjkstrongest.domain;

/**
 * The stages a domain moves through.
 *
 * <p>This replaces the three overlapping booleans the old tick procedure carried
 * ({@code isExpanding}, {@code isPostLines}, {@code isActive}), which could be all
 * false at once — or, after a clash, all true — with no way to tell which reading
 * was intended.
 */
public enum DomainPhase {
	/** The shell is growing and the interior is being carved out. */
	EXPANDING,
	/** Full size; the opening flourish plays and the sure-hit has not started. */
	SETTLING,
	/** Full size and hostile. */
	ACTIVE,
	/** Shutting down; the carve is being put back. */
	COLLAPSING;

	private static final DomainPhase[] VALUES = values();

	public static DomainPhase byOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : EXPANDING;
	}

	/** True once the shell is at full size, i.e. the domain is a closed room. */
	public boolean isSealed() {
		return this == SETTLING || this == ACTIVE;
	}
}
