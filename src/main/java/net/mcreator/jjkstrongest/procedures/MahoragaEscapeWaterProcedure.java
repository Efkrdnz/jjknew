package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.cd;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class MahoragaEscapeWaterProcedure {
	// forces a jump-out impulse when in water
	public static void execute(LevelAccessor world, Entity entity) {
		if (world == null || entity == null)
			return;
		if (!(entity instanceof LivingEntity living))
			return;
		double cd = entity.getPersistentData().getDouble("maho_water_cd");
		if (cd > 0) {
			entity.getPersistentData().putDouble("maho_water_cd", cd - 1);
			return;
		}
		if (!living.isInWaterOrBubble())
			return;
		Entity target = null;
		if (entity instanceof Mob mob)
			target = mob.getTarget();
		Vec3 dir = entity.getLookAngle();
		if (target != null) {
			double dx = target.getX() - entity.getX();
			double dz = target.getZ() - entity.getZ();
			double len = Math.sqrt(dx * dx + dz * dz);
			if (len > 0.001) {
				dir = new Vec3(dx / len, 0, dz / len);
			}
		}
		double forward = 0.55;
		double up = 0.85;
		Vec3 cur = entity.getDeltaMovement();
		entity.setDeltaMovement(cur.x * 0.2 + dir.x * forward, Math.max(cur.y, up), cur.z * 0.2 + dir.z * forward);
		entity.hurtMarked = true;
		entity.getPersistentData().putDouble("maho_water_cd", 8);
	}
}
