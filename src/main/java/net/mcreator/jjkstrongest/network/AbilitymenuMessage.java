package net.mcreator.jjkstrongest.network;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;

import net.mcreator.jjkstrongest.JjkStrongestMod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AbilitymenuMessage {
	int type, pressedms;

	public AbilitymenuMessage(int type, int pressedms) {
		this.type = type;
		this.pressedms = pressedms;
	}

	public AbilitymenuMessage(FriendlyByteBuf buffer) {
		this.type = buffer.readInt();
		this.pressedms = buffer.readInt();
	}

	public static void buffer(AbilitymenuMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}

	public static void handler(AbilitymenuMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			if (context.getSender() != null) {
				Player player = context.getSender();
				String moveset = "";
				String displayName = "";
				switch (message.type) {
					case 0 :
						moveset = "gojo_blue";
						displayName = "Blue";
						break;
					case 1 :
						moveset = "gojo_limitless";
						displayName = "Limitless";
						break;
					case 2 :
						moveset = "gojo_red";
						displayName = "Red";
						break;
					case 3 :
						moveset = "gojo_purple";
						displayName = "Purple";
						break;
					case 4 :
						moveset = "gojo_melee";
						displayName = "Melee";
						break;
					// sukuna
					case 5 :
						moveset = "sukuna_cleave";
						displayName = "Cleave";
						break;
					case 6 :
						moveset = "sukuna_dismantle";
						displayName = "Dismantle";
						break;
					case 7 :
						moveset = "sukuna_fuga";
						displayName = "Fuga";
						break;
					case 8 :
						moveset = "sukuna_wcs";
						displayName = "World Slash";
						break;
					case 9 :
						moveset = "sukuna_shrine";
						displayName = "Shrine";
						break;
					case 10 :
						moveset = "sukuna_melee";
						displayName = "Melee";
						break;
					case 11 :
						moveset = "all_generic";
						displayName = "Generic";
						break;
					// yuji
					case 12 :
						moveset = "yuji_bloodmanipulation";
						displayName = "Blood Manipulation";
						break;
					case 13 :
						moveset = "yuji_shrine";
						displayName = "Shrine";
						break;
					case 14 :
						moveset = "yuji_divergentfist";
						displayName = "Divergent Fist";
						break;
					case 15 :
						moveset = "yuji_melee";
						displayName = "Melee";
						break;
					// inumaki
					case 16 :
						moveset = "inumaki_assault";
						displayName = "Assault";
						break;
					case 17 :
						moveset = "inumaki_control";
						displayName = "Control";
						break;
					case 18 :
						moveset = "inumaki_binding";
						displayName = "Binding";
						break;
					case 19 :
						moveset = "inumaki_utility";
						displayName = "Utility";
						break;
					case 20 :
						moveset = "inumaki_melee";
						displayName = "Melee";
						break;
					case 21 :
						moveset = "reverse_cursed_technique";
						displayName = "RCT";
						break;
					default :
						return;
				}
				String finalMoveset = moveset;
				String finalDisplayName = displayName;
				player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.current_moveset = finalMoveset;
					capability.syncPlayerVariables(player);
				});
				player.displayClientMessage(Component.literal("Selected: " + finalDisplayName), true);
			}
		});
		context.setPacketHandled(true);
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		JjkStrongestMod.addNetworkMessage(AbilitymenuMessage.class, AbilitymenuMessage::buffer, AbilitymenuMessage::new, AbilitymenuMessage::handler);
	}
}
