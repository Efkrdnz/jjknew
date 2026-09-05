package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.JjkStrongestMod;
import net.efkrdnz.jjkstrongest.domain.DomainNoclip;

import java.util.UUID;

/**
 * Tells one client that its own domain exemption changed.
 *
 * <p>Needed because collision is evaluated on both sides: the server letting you through a
 * barrier the client still clamps you against is just rubber-banding.
 */
public record DomainNoclipPacket(UUID player, boolean exempt) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DomainNoclipPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JjkStrongestMod.MODID, "domain_noclip"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DomainNoclipPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> {
		buffer.writeUUID(packet.player());
		buffer.writeBoolean(packet.exempt());
	}, buffer -> new DomainNoclipPacket(buffer.readUUID(), buffer.readBoolean()));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Registered play-to-client only, so this never runs on a server. */
	public static void handle(DomainNoclipPacket packet, IPayloadContext context) {
		DomainNoclip.set(packet.player(), packet.exempt());
	}
}
