package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DomainSlashNetworkHandler {
	// helper method to send packets
	public static void sendToPlayer(ServerPlayer player, SpawnDomainSlashPacket packet) {
		PacketDistributor.sendToPlayer(player, packet);
	}

	// helper to send to all nearby players
	public static void sendToNearby(ServerPlayer player, double x, double y, double z, double radius, SpawnDomainSlashPacket packet) {
		if (player.level() instanceof ServerLevel level)
			PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, packet);
	}
}
