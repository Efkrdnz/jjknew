package net.efkrdnz.jjkstrongest.domain;

/**
 * How one technique's barrier holds up. Carried by {@link DomainDefinition}; read by
 * {@link DomainShell}.
 *
 * <p>Top-level rather than nested in the definition on purpose: this is the one part of a
 * definition that is pure arithmetic, with nothing from Minecraft in it, which keeps the
 * shell testable outside the game.
 *
 * <p>A hole and a crack are deliberately two different things, on two different clocks.
 * Integrity is the crack: continuous, drives the visuals, heals at {@code regenPerTick}. A
 * hole is discrete — a cell that reached zero — and lasts exactly {@code holeTicks} however
 * fast the crack behind it fades. Tying the two together meant a hole sealed on the first
 * healing tick, three seconds after it opened, while still looking wide open for another
 * seventeen.
 *
 * <p>Starting integrity is deliberately <em>not</em> here. {@link DomainShell#FULL} is 255
 * because a cell is quantised to one byte on the wire, so it belongs to the sync format
 * rather than to any one technique. A tougher barrier is a slower regen and a higher
 * resistance, not a bigger number.
 *
 * @param regenHoldTicks     ticks a cell must go undamaged before it starts healing
 * @param regenPerTick       points a resting cell recovers each tick
 * @param pressureResistance divides incoming rival-domain pressure; 1 is no resistance
 * @param holeTicks          how long a hole stays open once a cell is driven to zero
 */
public record DomainShellProfile(int regenHoldTicks, float regenPerTick, float pressureResistance, int holeTicks) {
}
