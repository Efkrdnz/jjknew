package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaHopUpProcedure {
	// short hop upward toward airborne target
	public static void execute(LevelAccessor world, Entity entity, LivingEntity target) {
		if (world == null || entity == null || target == null)
			return;
		if (!entity.onGround())
			return;
		double dx = target.getX() - entity.getX();
		double dz = target.getZ() - entity.getZ();
		double dy = (target.getY() + target.getBbHeight() * 0.5) - (entity.getY() + entity.getBbHeight() * 0.5);
		double len = Math.sqrt(dx * dx + dz * dz);
		double dirx = entity.getLookAngle().x;
		double dirz = entity.getLookAngle().z;
		if (len > 0.001) {
			dirx = dx / len;
			dirz = dz / len;
		}
		double yboost = 0.75 + Math.min(0.85, Math.max(0, dy) / 12.0);
		if (entity instanceof MahoragaEntity _m)
			_m.setAnimation("jump");
		entity.setDeltaMovement(dirx * 0.65, yboost, dirz * 0.65);
		entity.hurtMarked = true;
		entity.getPersistentData().putInt("maho_cd_hop", 18);
		entity.getPersistentData().putInt("maho_cd_global", 6);
	}
}
