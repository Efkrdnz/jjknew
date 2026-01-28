package net.mcreator.jjkstrongest.client;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;

import net.mcreator.jjkstrongest.client.MalevolentShrineSlashManager;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class MalevolentShrineSlashManager {
	private static final List<DomainSlash> SLASHES = new ArrayList<>();
	private static final int MAX_SLASHES = 300; // cap for performance

	public static class DomainSlash {
		public Vec3 position;
		public Vec3 direction;
		public float length;
		public float width;
		public int style;
		public float roll;
		public float seed;
		public float colorR, colorG, colorB;
		public int age;
		public int maxAge;
		public String domainUUID;

		public DomainSlash(Vec3 pos, Vec3 dir, float len, float wid, int sty, float rol, float sed, float r, float g, float b, int maxLife, String uuid) {
			this.position = pos;
			this.direction = dir.normalize();
			this.length = len;
			this.width = wid;
			this.style = sty;
			this.roll = rol;
			this.seed = sed;
			this.colorR = r;
			this.colorG = g;
			this.colorB = b;
			this.age = 0;
			this.maxAge = maxLife;
			this.domainUUID = uuid;
		}

		public void tick() {
			age++;
		}

		public boolean isExpired() {
			return age >= maxAge;
		}

		public float getAlpha() {
			// expansion for first 3 ticks
			float expandProgress = Math.min(age / 3.0f, 1.0f);
			expandProgress = 1.0f - (1.0f - expandProgress) * (1.0f - expandProgress);
			// fade out in last 4 ticks
			float fadeStartAge = maxAge - 4.0f;
			float fadeAlpha = 1.0f;
			if (age > fadeStartAge) {
				float fadeProgress = (age - fadeStartAge) / 4.0f;
				fadeAlpha = 1.0f - fadeProgress;
			}
			return expandProgress * fadeAlpha;
		}

		public float getCurrentLength() {
			float expandProgress = Math.min(age / 3.0f, 1.0f);
			expandProgress = 1.0f - (1.0f - expandProgress) * (1.0f - expandProgress);
			return length * expandProgress;
		}
	}

	// add slash to render queue
	public static void addSlash(Vec3 position, Vec3 direction, float length, float width, int style, float roll, float seed, float r, float g, float b, int lifetime, String domainUUID) {
		// remove old slashes if at cap
		if (SLASHES.size() >= MAX_SLASHES) {
			SLASHES.remove(0);
		}
		SLASHES.add(new DomainSlash(position, direction, length, width, style, roll, seed, r, g, b, lifetime, domainUUID));
	}

	// tick all slashes
	public static void tick() {
		Iterator<DomainSlash> iterator = SLASHES.iterator();
		while (iterator.hasNext()) {
			DomainSlash slash = iterator.next();
			slash.tick();
			if (slash.isExpired()) {
				iterator.remove();
			}
		}
	}

	// get all active slashes
	public static List<DomainSlash> getActiveSlashes() {
		return SLASHES;
	}

	// clear slashes for a specific domain
	public static void clearDomain(String domainUUID) {
		SLASHES.removeIf(slash -> slash.domainUUID.equals(domainUUID));
	}

	// clear all
	public static void clearAll() {
		SLASHES.clear();
	}
}
