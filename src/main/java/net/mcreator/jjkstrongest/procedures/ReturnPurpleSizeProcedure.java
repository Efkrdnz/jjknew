package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.HollowPurpleBigEntity;

public class ReturnPurpleSizeProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		return (entity instanceof HollowPurpleBigEntity _datEntI ? _datEntI.getEntityData().get(HollowPurpleBigEntity.DATA_size10) : 0) / 10;
	}
}
