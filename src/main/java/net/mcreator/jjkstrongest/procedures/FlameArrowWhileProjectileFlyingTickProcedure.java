package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

public class FlameArrowWhileProjectileFlyingTickProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity immediatesourceentity) {
		if (immediatesourceentity == null)
			return;
		immediatesourceentity.setNoGravity(true);
		if (world instanceof ServerLevel _level)
			_level.sendParticles(ParticleTypes.FLAME, x, y, z, 10, 0.15, 0.15, 0.15, 0.1);
		immediatesourceentity.setDeltaMovement(new Vec3((immediatesourceentity.getPersistentData().getDouble("rx")), (immediatesourceentity.getPersistentData().getDouble("ry")), (immediatesourceentity.getPersistentData().getDouble("rz"))));
	}
}
