package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class InfinityLockRotationProcedure {
	public static void execute(Entity entity) {
		if (entity == null || !(entity instanceof Projectile))
			return;
		CompoundTag nbt = entity.getPersistentData();
		if (nbt.contains("infinityOriginalYaw")) {
			entity.setYRot(nbt.getFloat("infinityOriginalYaw"));
			entity.setXRot(nbt.getFloat("infinityOriginalPitch"));
			entity.yRotO = nbt.getFloat("infinityOriginalYaw");
			entity.xRotO = nbt.getFloat("infinityOriginalPitch");
		}
	}
}
