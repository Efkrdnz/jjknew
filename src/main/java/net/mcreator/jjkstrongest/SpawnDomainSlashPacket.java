package net.mcreator.jjkstrongest.network;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.network.NetworkEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.MalevolentShrineSlashManager;
import net.mcreator.jjkstrongest.network.SpawnDomainSlashPacket;

import java.util.function.Supplier;

public class SpawnDomainSlashPacket {
	private final double x, y, z;
	private final double dirX, dirY, dirZ;
	private final float length, width;
	private final int style;
	private final float roll, seed;
	private final float colorR, colorG, colorB;
	private final int lifetime;
	private final String domainUUID;

	public SpawnDomainSlashPacket(double x, double y, double z, double dirX, double dirY, double dirZ, float length, float width, int style, float roll, float seed, float r, float g, float b, int lifetime, String uuid) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dirX = dirX;
		this.dirY = dirY;
		this.dirZ = dirZ;
		this.length = length;
		this.width = width;
		this.style = style;
		this.roll = roll;
		this.seed = seed;
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		this.lifetime = lifetime;
		this.domainUUID = uuid;
	}

	public SpawnDomainSlashPacket(FriendlyByteBuf buffer) {
		this.x = buffer.readDouble();
		this.y = buffer.readDouble();
		this.z = buffer.readDouble();
		this.dirX = buffer.readDouble();
		this.dirY = buffer.readDouble();
		this.dirZ = buffer.readDouble();
		this.length = buffer.readFloat();
		this.width = buffer.readFloat();
		this.style = buffer.readInt();
		this.roll = buffer.readFloat();
		this.seed = buffer.readFloat();
		this.colorR = buffer.readFloat();
		this.colorG = buffer.readFloat();
		this.colorB = buffer.readFloat();
		this.lifetime = buffer.readInt();
		this.domainUUID = buffer.readUtf();
	}

	public static void encode(SpawnDomainSlashPacket packet, FriendlyByteBuf buffer) {
		buffer.writeDouble(packet.x);
		buffer.writeDouble(packet.y);
		buffer.writeDouble(packet.z);
		buffer.writeDouble(packet.dirX);
		buffer.writeDouble(packet.dirY);
		buffer.writeDouble(packet.dirZ);
		buffer.writeFloat(packet.length);
		buffer.writeFloat(packet.width);
		buffer.writeInt(packet.style);
		buffer.writeFloat(packet.roll);
		buffer.writeFloat(packet.seed);
		buffer.writeFloat(packet.colorR);
		buffer.writeFloat(packet.colorG);
		buffer.writeFloat(packet.colorB);
		buffer.writeInt(packet.lifetime);
		buffer.writeUtf(packet.domainUUID);
	}

	public static void handle(SpawnDomainSlashPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// client-side only
			if (ctx.get().getDirection().getReceptionSide().isClient()) {
				Minecraft.getInstance().execute(() -> {
					MalevolentShrineSlashManager.addSlash(new Vec3(packet.x, packet.y, packet.z), new Vec3(packet.dirX, packet.dirY, packet.dirZ), packet.length, packet.width, packet.style, packet.roll, packet.seed, packet.colorR, packet.colorG,
							packet.colorB, packet.lifetime, packet.domainUUID);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
