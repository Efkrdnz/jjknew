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

	/**
	 * True while the domain is a closed room, which is from the moment it is cast.
	 *
	 * <p>EXPANDING used to be excluded, on the reasoning that a shell still growing is not
	 * yet a wall. In play that gave you a forty-tick window to simply walk out of a domain
	 * being opened on you, which is the opposite of what a sure-hit is. The room is its final
	 * size for collision from the first tick now — see {@code DomainUVEntity.sphere()} — and
	 * only the visible wall rushes out to meet it.
	 *
	 * <p>COLLAPSING is still open: the shell is breaking and the way out is the point.
	 */
	public boolean isSealed() {
		return this == EXPANDING || this == SETTLING || this == ACTIVE;
	}
}
