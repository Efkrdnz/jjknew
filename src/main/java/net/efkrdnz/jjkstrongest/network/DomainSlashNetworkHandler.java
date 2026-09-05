package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DomainSlashNetworkHandler {
	// helper method to send packets
	public static void sendToPlayer(ServerPlayer player, SpawnDomainSlashPacket packet) {
		PacketDistributor.sendToPlayer(player, packet);
	}

	/**
	 * One broadcast for everyone in range, instead of one packet per player.
	 *
	 * <p>The shrine emits sixty to eighty slashes a tick and used to send each of them
	 * separately to each watching player — with four people nearby that is over three
	 * hundred packets a tick for a purely visual effect.
	 */
	public static void sendToNearby(ServerLevel level, double x, double y, double z, double radius, SpawnDomainSlashPacket packet) {
		PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, packet);
	}
}
