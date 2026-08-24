package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.HollowPurpleBigEntity;
import net.mcreator.jjkstrongest.entity.HollowPurpleProjectileEntity;

public class ReturnPurpleSizeProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		if (entity instanceof HollowPurpleBigEntity _datEntI)
			return _datEntI.getEntityData().get(HollowPurpleBigEntity.DATA_size10) / 10.0;
		if (entity instanceof HollowPurpleProjectileEntity _datEntI)
			return _datEntI.getEntityData().get(HollowPurpleProjectileEntity.DATA_size10) / 10.0;
		return 0;
	}
}
