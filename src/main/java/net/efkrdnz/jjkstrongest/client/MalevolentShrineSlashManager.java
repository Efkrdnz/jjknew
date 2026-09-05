package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The cuts currently in the air, client side.
 *
 * <p>A slash is born by packet and lives for a fixed number of ticks. What it looks like at
 * any moment is a function of how far through that life it is, worked out here and handed
 * to the shader as four bytes; the renderer draws every live one in a single batch.
 */
@OnlyIn(Dist.CLIENT)
public class MalevolentShrineSlashManager {
	private static final List<DomainSlash> SLASHES = new ArrayList<>();
	/** Hard cap. The shrine settles around 220 live, so this is headroom, not a governor. */
	private static final int MAX_SLASHES = 400;

	/** Style codes, shared with the server's spawn code and the shader's decoding. */
	public static final int STYLE_CLEAVE = 0;
	public static final int STYLE_DISMANTLE = 1;
	public static final int STYLE_STRIKE = 2;

	/** Ticks a blade takes to draw itself tip to tip: very fast, not instant. A strike is instant. */
	private static final float SWEEP_TICKS = 1.5f;

	public static class DomainSlash {
		/** The blade's CENTRE. Its origin is half a length back along the direction. */
		public final Vec3 position;
		public final Vec3 direction;
		public final float length;
		public final float width;
		public final int style;
		/** Per-slash brightness jitter, 0..1, derived from the packet's roll. */
		public final float jitter;
		public final float seed;
		public final String domainUUID;
		public int age;
		public final int maxAge;

		public DomainSlash(Vec3 pos, Vec3 dir, float len, float wid, int sty, float roll, float sed, int maxLife, String uuid) {
			this.position = pos;
			this.direction = dir.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
			this.length = len;
			this.width = wid;
			this.style = sty;
			this.jitter = (float) (roll / (Math.PI * 2.0) - Math.floor(roll / (Math.PI * 2.0)));
			this.seed = sed;
			this.age = 0;
			this.maxAge = Math.max(1, maxLife);
			this.domainUUID = uuid;
		}

		public void tick() {
			age++;
		}

		public boolean isExpired() {
			return age >= maxAge;
		}

		/** 0 at birth, 1 at death, smooth between frames. */
		public float progress(float partialTick) {
			return Mth.clamp((age + partialTick) / maxAge, 0.0f, 1.0f);
		}

		/** How far along the blade the leading edge has drawn, 0..1. Eased out, so it snaps. */
		public float sweep(float partialTick) {
			if (style == STYLE_STRIKE)
				return 1.0f;
			float p = Mth.clamp((age + partialTick) / SWEEP_TICKS, 0.0f, 1.0f);
			return 1.0f - (1.0f - p) * (1.0f - p);
		}
	}

	public static void addSlash(Vec3 position, Vec3 direction, float length, float width, int style, float roll, float seed, float r, float g, float b, int lifetime, String domainUUID) {
		// The colour arguments are what the packet has always carried; the look is decided by
		// style now and they are ignored. Kept so the packet codec does not have to change.
		if (SLASHES.size() >= MAX_SLASHES)
			SLASHES.remove(0);
		SLASHES.add(new DomainSlash(position, direction, length, width, style, roll, seed, lifetime, domainUUID));
	}

	public static void tick() {
		Iterator<DomainSlash> iterator = SLASHES.iterator();
		while (iterator.hasNext()) {
			DomainSlash slash = iterator.next();
			slash.tick();
			if (slash.isExpired())
				iterator.remove();
		}
	}

	public static List<DomainSlash> getActiveSlashes() {
		return SLASHES;
	}

	public static void clearDomain(String domainUUID) {
		SLASHES.removeIf(slash -> slash.domainUUID.equals(domainUUID));
	}

	public static void clearAll() {
		SLASHES.clear();
	}
}
