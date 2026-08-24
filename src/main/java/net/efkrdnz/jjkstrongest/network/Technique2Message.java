
package net.efkrdnz.jjkstrongest.network;

import net.minecraftforge.network.NetworkEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

import net.efkrdnz.jjkstrongest.procedures.Technique2OnKeyPressedProcedure;
import net.efkrdnz.jjkstrongest.procedures.Technique2OnKeyPressProcedure;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Technique2Message {
	int type, pressedms;

	public Technique2Message(int type, int pressedms) {
		this.type = type;
		this.pressedms = pressedms;
	}

	public Technique2Message(FriendlyByteBuf buffer) {
		this.type = buffer.readInt();
		this.pressedms = buffer.readInt();
	}

	public static void buffer(Technique2Message message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}

	public static void handler(Technique2Message message, Supplier<NetworkEvent.Context> contextSupplier) {
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

			Technique2OnKeyPressProcedure.execute(entity);
		}
		if (type == 1) {

			Technique2OnKeyPressedProcedure.execute(entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		JjkStrongestMod.addNetworkMessage(Technique2Message.class, Technique2Message::buffer, Technique2Message::new, Technique2Message::handler);
	}
}
