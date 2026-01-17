package net.mcreator.jjkstrongest.procedures;

import org.joml.Vector3f;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;

public class GojoMarkTeleportProcedure {
	public static void execute(LevelAccessor world, Player player, Entity target) {
		if (player == null || target == null || !(world instanceof ServerLevel))
			return;
		ServerLevel serverLevel = (ServerLevel) world;
		Vec3 playerOrigin = player.position();
		// spawn particles at player origin
		for (int i = 0; i < 30; i++) {
			serverLevel.sendParticles(ParticleTypes.PORTAL, playerOrigin.x + (world.getRandom().nextDouble() - 0.5) * 1.5, playerOrigin.y + world.getRandom().nextDouble() * 2, playerOrigin.z + (world.getRandom().nextDouble() - 0.5) * 1.5, 1, 0, 0, 0,
					0.1);
		}
		// calculate teleport position: 3 blocks in front of target from PLAYER'S view angle
		Vec3 playerLook = player.getLookAngle();
		Vec3 targetPos = target.position();
		Vec3 teleportPos = targetPos.add(playerLook.scale(-3.0));
		// draw particle line from player to destination
		Vec3 playerEye = player.getEyePosition();
		int lineSteps = 20;
		for (int l = 0; l <= lineSteps; l++) {
			double lineFraction = l / (double) lineSteps;
			Vec3 linePos = playerEye.add(teleportPos.subtract(playerEye).scale(lineFraction));
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.3f, 0.6f, 1.0f), 1.0f), linePos.x, linePos.y, linePos.z, 1, 0, 0, 0, 0);
		}
		// teleport player
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
		}
		// spawn particles at destination
		for (int i = 0; i < 30; i++) {
			serverLevel.sendParticles(ParticleTypes.PORTAL, teleportPos.x + (world.getRandom().nextDouble() - 0.5) * 1.5, teleportPos.y + world.getRandom().nextDouble() * 2, teleportPos.z + (world.getRandom().nextDouble() - 0.5) * 1.5, 1, 0, 0, 0,
					0.1);
		}
		// sound
		world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
	}
}
