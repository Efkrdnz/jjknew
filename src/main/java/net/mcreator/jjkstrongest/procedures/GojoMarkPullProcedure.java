package net.mcreator.jjkstrongest.procedures;

import org.joml.Vector3f;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;

public class GojoMarkPullProcedure {
	public static void execute(LevelAccessor world, Player player, Entity target) {
		if (player == null || target == null || !(world instanceof ServerLevel))
			return;
		ServerLevel serverLevel = (ServerLevel) world;
		// calculate teleport destination (3 blocks in front of player)
		Vec3 playerLook = player.getLookAngle();
		Vec3 playerEye = player.getEyePosition();
		Vec3 teleportPos = playerEye.add(playerLook.scale(3.0));
		Vec3 targetCurrentPos = target.position().add(0, target.getBbHeight() / 2, 0);
		// check blocks along path for hardness
		Vec3 pullDirection = teleportPos.subtract(targetCurrentPos).normalize();
		double totalDistance = targetCurrentPos.distanceTo(teleportPos);
		int steps = (int) Math.ceil(totalDistance);
		boolean pathClear = true;
		for (int i = 0; i <= steps; i++) {
			double fraction = i / (double) steps;
			Vec3 checkPos = targetCurrentPos.add(pullDirection.scale(totalDistance * fraction));
			BlockPos blockPos = new BlockPos((int) checkPos.x, (int) checkPos.y, (int) checkPos.z);
			BlockState blockState = world.getBlockState(blockPos);
			if (!blockState.isAir()) {
				float hardness = blockState.getDestroySpeed(world, blockPos);
				if (hardness < 0 || hardness > 1.5f) {
					pathClear = false;
					break;
				} else if (hardness >= 0) {
					world.destroyBlock(blockPos, true);
				}
			}
		}
		if (!pathClear) {
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cPath blocked by hard material!"), true);
			return;
		}
		// spawn particles at origin
		for (int i = 0; i < 30; i++) {
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.2f, 0.4f, 1.0f), 1.5f), targetCurrentPos.x + (world.getRandom().nextDouble() - 0.5) * 1.5, targetCurrentPos.y + (world.getRandom().nextDouble() - 0.5) * 1.5,
					targetCurrentPos.z + (world.getRandom().nextDouble() - 0.5) * 1.5, 1, 0, 0, 0, 0.1);
		}
		// draw particle line from origin to destination
		int lineSteps = 20;
		for (int l = 0; l <= lineSteps; l++) {
			double lineFraction = l / (double) lineSteps;
			Vec3 linePos = targetCurrentPos.add(teleportPos.subtract(targetCurrentPos).scale(lineFraction));
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.3f, 0.6f, 1.0f), 1.0f), linePos.x, linePos.y, linePos.z, 1, 0, 0, 0, 0);
		}
		// teleport target to destination
		target.teleportTo(teleportPos.x, teleportPos.y - target.getBbHeight() / 2, teleportPos.z);
		target.setDeltaMovement(0, 0, 0);
		// spawn particles at destination
		for (int i = 0; i < 30; i++) {
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.2f, 0.4f, 1.0f), 1.5f), teleportPos.x + (world.getRandom().nextDouble() - 0.5) * 1.5, teleportPos.y + (world.getRandom().nextDouble() - 0.5) * 1.5,
					teleportPos.z + (world.getRandom().nextDouble() - 0.5) * 1.5, 1, 0, 0, 0, 0.1);
		}
		// sound effect
		world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);
	}
}
