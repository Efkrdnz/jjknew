package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.MalevolentShrineEntity;

public class MalevolentShrineSummonProcedure {
	// spawn malevolent shrine domain 4 blocks behind player
	public static void execute(Level world, Entity player) {
		if (world == null || player == null || world.isClientSide())
			return;
		// get player facing direction
		double yaw = Math.toRadians(player.getYRot() + 180);
		double offsetX = -Math.sin(yaw) * 4.0;
		double offsetZ = Math.cos(yaw) * 4.0;
		// spawn position 4 blocks behind player
		double spawnX = player.getX() + offsetX;
		double spawnY = player.getY();
		double spawnZ = player.getZ() + offsetZ;
		// spawn domain entity
		if (world instanceof ServerLevel serverLevel) {
			MalevolentShrineEntity domain = JjkStrongestModEntities.MALEVOLENT_SHRINE.get().spawn(serverLevel, BlockPos.containing(spawnX, spawnY, spawnZ), MobSpawnType.MOB_SUMMONED);
			if (domain != null) {
				// store owner uuid
				domain.getPersistentData().putString("ownerUUID", player.getStringUUID());
				// store cast Y level for block destruction reference
				domain.getPersistentData().putDouble("domainCastY", spawnY);
				// initialize lifetime counter (20 seconds = 400 ticks)
				domain.getPersistentData().putInt("domainLifetimeTicks", 0);
				// initialize destruction progress
				domain.getPersistentData().putInt("destructionProgress", 0);
			}
		}
	}
}
