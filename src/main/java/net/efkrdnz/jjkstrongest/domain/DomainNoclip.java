package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Who is allowed to walk through a domain barrier.
 *
 * <p>This replaces the creative-mode exemption. That exemption made the barrier invisible
 * to exactly the gamemode people build and test in, so a domain looked completely broken
 * while behaving exactly as written — and there was no way to tell the two apart from
 * inside the game. An explicit toggle cannot do that to you.
 *
 * <p>Held on both logical sides and kept in step by {@code DomainNoclipPacket}. It has to
 * be: collision runs client-side too, so a server that let you through while the client
 * still clamped you would just rubber-band you back against a wall you were allowed
 * through.
 *
 * <p>Deliberately not persisted. A noclip that survives a restart is one you forget you
 * left on, and then the barrier "stops working" again.
 */
public final class DomainNoclip {

	private static final Set<UUID> EXEMPT = Collections.synchronizedSet(new HashSet<>());

	private DomainNoclip() {
	}

	public static boolean isExempt(Player player) {
		if (EXEMPT.isEmpty())
			return false;
		return EXEMPT.contains(player.getUUID());
	}

	/** @return the state it ended up in */
	public static boolean toggle(UUID id) {
		synchronized (EXEMPT) {
			if (EXEMPT.remove(id))
				return false;
			EXEMPT.add(id);
			return true;
		}
	}

	public static void set(UUID id, boolean exempt) {
		if (exempt)
			EXEMPT.add(id);
		else
			EXEMPT.remove(id);
	}

	/** Client-side reset, so a disconnect cannot leave a stale exemption behind. */
	public static void clear() {
		EXEMPT.clear();
	}
}
