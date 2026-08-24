
package net.efkrdnz.jjkstrongest.network;

import net.minecraftforge.network.NetworkEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

import net.efkrdnz.jjkstrongest.procedures.Technique4OnKeyReleasedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique4OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Technique4Message {
	int type, pressedms;

	public Technique4Message(int type, int pressedms) {
		this.type = type;
		this.pressedms = pressedms;
	}

	public Technique4Message(FriendlyByteBuf buffer) {
		this.type = buffer.readInt();
		this.pressedms = buffer.readInt();
	}

	public static void buffer(Technique4Message message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}

	public static void handler(Technique4Message message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			pressAction(context.getSender(), message.type, message.pressedms);
		});
		context.setPacketHandled(true);
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

			Technique4OnKeyPressedProcedure.execute(entity);
		}
		if (type == 1) {

			Technique4OnKeyReleasedProcedure.execute(world, x, y, z, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		JjkStrongestMod.addNetworkMessage(Technique4Message.class, Technique4Message::buffer, Technique4Message::new, Technique4Message::handler);
	}
}
