package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.efkrdnz.jjkstrongest.client.MalevolentShrineSlashManager;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public record SpawnDomainSlashPacket(double x, double y, double z, double dirX, double dirY, double dirZ, float length, float width, int style, float roll, float seed, float colorR, float colorG, float colorB, int lifetime,
		String domainUUID) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SpawnDomainSlashPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JjkStrongestMod.MODID, "spawn_domain_slash"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SpawnDomainSlashPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> {
		buffer.writeDouble(packet.x());
		buffer.writeDouble(packet.y());
		buffer.writeDouble(packet.z());
		buffer.writeDouble(packet.dirX());
		buffer.writeDouble(packet.dirY());
		buffer.writeDouble(packet.dirZ());
		buffer.writeFloat(packet.length());
		buffer.writeFloat(packet.width());
		buffer.writeInt(packet.style());
		buffer.writeFloat(packet.roll());
		buffer.writeFloat(packet.seed());
		buffer.writeFloat(packet.colorR());
		buffer.writeFloat(packet.colorG());
		buffer.writeFloat(packet.colorB());
		buffer.writeInt(packet.lifetime());
		buffer.writeUtf(packet.domainUUID());
	}, buffer -> new SpawnDomainSlashPacket(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readFloat(), buffer.readInt(),
			buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readUtf()));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/**
	 * Registered play-to-client only, so this never runs on a server. Payload
	 * handlers already execute on the main thread, which for the client is the
	 * render thread the slash manager expects.
	 */
	public static void handle(SpawnDomainSlashPacket packet, IPayloadContext context) {
		MalevolentShrineSlashManager.addSlash(new Vec3(packet.x(), packet.y(), packet.z()), new Vec3(packet.dirX(), packet.dirY(), packet.dirZ()), packet.length(), packet.width(), packet.style(), packet.roll(), packet.seed(),
				packet.colorR(), packet.colorG(), packet.colorB(), packet.lifetime(), packet.domainUUID());
	}
}
