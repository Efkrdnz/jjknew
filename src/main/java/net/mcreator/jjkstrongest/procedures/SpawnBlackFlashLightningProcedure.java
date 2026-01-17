package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.BFEntityEntity;

public class SpawnBlackFlashLightningProcedure {
	public static void execute(Level world, Entity target) {
		if (world == null || target == null || world.isClientSide())
			return;
		// spawn lightning entity at target position
		Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
		BFEntityEntity lightning = new BFEntityEntity(JjkStrongestModEntities.BF_ENTITY.get(), world);
		lightning.setPos(targetPos.x, targetPos.y, targetPos.z);
		// set properties
		lightning.setNoGravity(true);
		lightning.setInvulnerable(true);
		lightning.setSilent(true);
		// DIRECTLY ATTACH TO TARGET ENTITY - this makes following 100% reliable
		lightning.getPersistentData().putString("attached_entity_uuid", target.getStringUUID());
		world.addFreshEntity(lightning);
	}
}
