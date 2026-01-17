package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.HollowPurpleProjectileEntity;

public class ReturnPurpleSizeProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		return (entity instanceof HollowPurpleProjectileEntity _datEntI ? _datEntI.getEntityData().get(HollowPurpleProjectileEntity.DATA_size10) : 0) / 10;
	}
}
