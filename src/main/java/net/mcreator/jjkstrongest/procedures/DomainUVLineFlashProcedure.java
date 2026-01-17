package net.mcreator.jjkstrongest.procedures;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;

import java.util.UUID;

public class DomainUVLineFlashProcedure {
	public static void execute(LevelAccessor world, Entity domainEntity) {
		if (!(world instanceof ServerLevel level))
			return;
		if (domainEntity == null)
			return;
		CompoundTag data = domainEntity.getPersistentData();
		if (!data.getBoolean("isPostLines"))
			return;
		UUID ownerUUID = null;
		try {
			String owner = data.getString("ownerUUID");
			if (owner != null && !owner.isEmpty())
				ownerUUID = UUID.fromString(owner);
		} catch (Exception ignored) {
		}
		if (ownerUUID == null)
			return;
		Entity caster = level.getEntity(ownerUUID);
		if (caster == null)
			return;
		Vec3 from = caster.getEyePosition();
		int rays = 45;
		for (int i = 0; i < rays; i++) {
			Vec3 dir = randomDirectionForwardish(level, caster.getLookAngle());
			spawnLine(level, from, dir, 28.0, 0.8);
		}
	}

	private static Vec3 randomDirectionForwardish(ServerLevel level, Vec3 forward) {
		// slightly biased forward cone
		double rx = (level.random.nextDouble() - 0.5) * 0.9;
		double ry = (level.random.nextDouble() - 0.5) * 0.5;
		double rz = (level.random.nextDouble() - 0.5) * 0.9;
		Vec3 dir = forward.add(rx, ry, rz);
		double len = dir.length();
		if (len < 0.001)
			return forward;
		return dir.scale(1.0 / len);
	}

	private static void spawnLine(ServerLevel level, Vec3 from, Vec3 dir, double length, double step) {
		int mode = level.random.nextInt(3);
		Vector3f color = mode == 0 ? new Vector3f(1.0f, 0.2f, 0.9f) : (mode == 1 ? new Vector3f(0.9f, 0.0f, 1.0f) : new Vector3f(1.0f, 0.0f, 0.0f));
		float size = 1.6f;
		DustParticleOptions dust = new DustParticleOptions(color, size);
		for (double s = 0; s <= length; s += step) {
			Vec3 p = from.add(dir.scale(s));
			level.sendParticles(dust, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}
	}
}
