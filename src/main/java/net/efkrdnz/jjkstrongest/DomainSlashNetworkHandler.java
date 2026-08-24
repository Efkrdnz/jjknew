package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.server.level.ServerPlayer;

import net.efkrdnz.jjkstrongest.network.SpawnDomainSlashPacket;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;
import net.efkrdnz.jjkstrongest.network.DomainSlashNetworkHandler;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DomainSlashNetworkHandler {
	private static boolean initialized = false;

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		if (!initialized) {
			// register our custom packet using MCreator's system
			JjkStrongestMod.addNetworkMessage(SpawnDomainSlashPacket.class, SpawnDomainSlashPacket::encode, SpawnDomainSlashPacket::new, SpawnDomainSlashPacket::handle);
			initialized = true;
			System.out.println("[JJK Strongest] Domain slash network packet registered");
		}
	}

	// helper method to send packets
	public static void sendToPlayer(ServerPlayer player, SpawnDomainSlashPacket packet) {
		JjkStrongestMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	// helper to send to all nearby players
	public static void sendToNearby(ServerPlayer player, double x, double y, double z, double radius, SpawnDomainSlashPacket packet) {
		JjkStrongestMod.PACKET_HANDLER.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, radius, player.level().dimension())), packet);
	}
}
