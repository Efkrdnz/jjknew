package net.efkrdnz.jjkstrongest.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.efkrdnz.jjkstrongest.procedures.MarkExecuteOnKeyPressedProcedure;

import net.efkrdnz.jjkstrongest.JjkStrongestMod;

public record MarkExecuteMessage(int type, int pressedms) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MarkExecuteMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JjkStrongestMod.MODID, "mark_execute"));

	public static final StreamCodec<RegistryFriendlyByteBuf, MarkExecuteMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
		buffer.writeInt(message.type());
		buffer.writeInt(message.pressedms());
	}, buffer -> new MarkExecuteMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handler(MarkExecuteMessage message, IPayloadContext context) {
		// payload handlers already run on the main thread (HandlerThread.MAIN)
		pressAction(context.player(), message.type(), message.pressedms());
	}

	public static void pressAction(Player entity, int type, int pressedms) {
		Level world = entity.level();
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		// security measure to prevent arbitrary chunk generation
		if (!world.hasChunkAt(entity.blockPosition()))
			return;
		if (type == 0) {
			MarkExecuteOnKeyPressedProcedure.execute(entity);
		}
	}
}
