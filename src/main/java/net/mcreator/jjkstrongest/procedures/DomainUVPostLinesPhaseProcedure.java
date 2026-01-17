package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class DomainUVPostLinesPhaseProcedure {
	public static void execute(LevelAccessor world, Entity domainEntity) {
		if (!(world instanceof ServerLevel level))
			return;
		if (domainEntity == null)
			return;
		CompoundTag data = domainEntity.getPersistentData();
		if (!data.getBoolean("isPostLines"))
			return;
		int postTick = data.getInt("postTick");
		Vec3 center = domainEntity.position();
		double radius = data.getDouble("domainRadius");
		double captureRadius = data.getDouble("captureRadius");
		UUID ownerUUID = null;
		try {
			String owner = data.getString("ownerUUID");
			if (owner != null && !owner.isEmpty())
				ownerUUID = UUID.fromString(owner);
		} catch (Exception ignored) {
		}
		if (postTick == 30) {
			applyBlindness(level, center, captureRadius, ownerUUID);
		}
		postTick++;
		data.putInt("postTick", postTick);
		// after 40 ticks -> domain becomes active
		if (postTick >= 40) {
			data.putBoolean("isPostLines", false);
			data.putBoolean("isActive", true);
		}
	}

	private static void applyBlindness(ServerLevel level, Vec3 center, double r, UUID ownerUUID) {
		AABB box = new AABB(center.x - r, center.y - r, center.z - r, center.x + r, center.y + r, center.z + r);
		double r2 = r * r;
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> true)) {
			if (ownerUUID != null && target.getUUID().equals(ownerUUID))
				continue;
			if (target instanceof Player p && (p.isCreative() || p.isSpectator()))
				continue;
			if (target.position().distanceToSqr(center) > r2)
				continue;
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 15, 0, true, false, false));
		}
	}
}
