package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import java.util.List;

public class CleaveConeDamageProcedure {
	public static void execute(LevelAccessor world, Entity entity, double range, float coneDegrees, float damage) {
		if (entity == null)
			return;
		if (!(world instanceof Level lvl))
			return;
		Vec3 origin = entity.getEyePosition();
		Vec3 look = entity.getLookAngle().normalize();
		double cosLimit = Math.cos(Math.toRadians(coneDegrees * 0.5));
		AABB box = new AABB(entity.getX() - range, entity.getY() - range, entity.getZ() - range, entity.getX() + range, entity.getY() + range, entity.getZ() + range);
		List<LivingEntity> list = lvl.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != entity);
		DamageSource ds = new DamageSource(lvl.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_cleave"))), entity);
		for (LivingEntity le : list) {
			Vec3 to = le.getEyePosition().subtract(origin);
			double dist = to.length();
			if (dist <= 0.001 || dist > range)
				continue;
			Vec3 dir = to.scale(1.0 / dist);
			double dot = look.dot(dir);
			if (dot >= cosLimit) {
				le.hurt(ds, damage);
			}
		}
	}
}
