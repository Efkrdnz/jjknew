package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

public class MalevolentShrineSummonProcedure {
	// spawn malevolent shrine domain 4 blocks behind player
	public static void execute(Level world, Entity player) {
		if (world == null || player == null)
			return;
		// get player facing direction
		double yaw = Math.toRadians(player.getYRot() + 180);
		double offsetX = -Math.sin(yaw) * 4.0;
		double offsetZ = Math.cos(yaw) * 4.0;
		// spawn position 4 blocks behind player
		execute(world, player, player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
	}

	/**
	 * Summons at a chosen point rather than behind the caster.
	 *
	 * <p>The shrine's own placement is derived from the owner's yaw, which is right when a
	 * sorcerer opens one around themselves and useless when something wants one put
	 * somewhere specific — a rival spawned across the field to clash with, most of all.
	 */
	public static void execute(Level world, Entity player, double spawnX, double spawnY, double spawnZ) {
		if (world == null || player == null || world.isClientSide())
			return;
		// spawn domain entity
		if (world instanceof ServerLevel serverLevel) {
			MalevolentShrineEntity domain = JjkStrongestModEntities.MALEVOLENT_SHRINE.get().spawn(serverLevel, BlockPos.containing(spawnX, spawnY, spawnZ), MobSpawnType.MOB_SUMMONED);
			if (domain != null) {
				// store owner uuid — synced, because the client needs it too
				domain.setDomainOwnerUUID(player.getStringUUID());
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
