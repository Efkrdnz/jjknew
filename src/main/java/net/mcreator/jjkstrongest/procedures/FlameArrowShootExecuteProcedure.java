package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.FlameArrowEntity;

public class FlameArrowShootExecuteProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			Entity _shootFrom = entity;
			Level projectileLevel = _shootFrom.level();
			if (!projectileLevel.isClientSide()) {
				Projectile _entityToSpawn = new Object() {
					public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
						AbstractArrow entityToSpawn = new FlameArrowEntity(JjkStrongestModEntities.FLAME_ARROW.get(), level);
						entityToSpawn.setOwner(shooter);
						entityToSpawn.setBaseDamage(damage);
						entityToSpawn.setKnockback(knockback);
						entityToSpawn.setSilent(true);
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, entity, 0, 1);
				_entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
				_entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 1, 0);
				_entityToSpawn.getPersistentData().putDouble("rx", (4 * entity.getLookAngle().x));
				_entityToSpawn.getPersistentData().putDouble("ry", (4 * entity.getLookAngle().y));
				_entityToSpawn.getPersistentData().putDouble("rz", (4 * entity.getLookAngle().z));
				projectileLevel.addFreshEntity(_entityToSpawn);
			}
		}
	}
}
