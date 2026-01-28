package net.mcreator.jjkstrongest.network;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.server.level.ServerPlayer;

import net.mcreator.jjkstrongest.network.SpawnDomainSlashPacket;
import net.mcreator.jjkstrongest.JjkStrongestMod;
import net.mcreator.jjkstrongest.network.DomainSlashNetworkHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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
