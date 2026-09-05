package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.JjkStrongestMod;
import net.efkrdnz.jjkstrongest.client.DomainShellClient;

/**
 * The barrier's per-direction integrity, on its way to the people who can see it.
 *
 * <p>Everything else about a domain's shape rides {@link net.minecraft.network.syncher.SynchedEntityData},
 * but there is no serializer for a byte array and the grid changes wholesale every tick a
 * rival is pressing on it, so a delta channel would buy nothing. One flat snapshot of 512
 * bytes at 5 Hz is a rounding error next to the slash traffic already going out.
 */
public record DomainShellSyncPacket(int entityId, int version, byte[] cells) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DomainShellSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JjkStrongestMod.MODID, "domain_shell_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DomainShellSyncPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> {
		buffer.writeVarInt(packet.entityId());
		buffer.writeVarInt(packet.version());
		buffer.writeByteArray(packet.cells());
	}, buffer -> new DomainShellSyncPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readByteArray()));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Registered play-to-client only, so this never runs on a server. */
	public static void handle(DomainShellSyncPacket packet, IPayloadContext context) {
		DomainShellClient.accept(packet.entityId(), packet.version(), packet.cells());
	}
}
