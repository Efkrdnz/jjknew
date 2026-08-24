package net.efkrdnz.jjkstrongest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.LogicalSide;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.network.AbilitymenuMessage;
import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.network.MarkExecuteMessage;
import net.efkrdnz.jjkstrongest.network.SpawnDomainSlashPacket;
import net.efkrdnz.jjkstrongest.network.Technique1Message;
import net.efkrdnz.jjkstrongest.network.Technique2Message;
import net.efkrdnz.jjkstrongest.network.Technique3Message;
import net.efkrdnz.jjkstrongest.network.Technique4Message;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModTabs;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModSounds;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModParticleTypes;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMenus;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModItems;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModBlocks;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModBlockEntities;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

import java.lang.reflect.Field;

import java.io.StringWriter;
import java.io.PrintWriter;

@Mod(JjkStrongestMod.MODID)
public class JjkStrongestMod {
	public static final Logger LOGGER = LogManager.getLogger(JjkStrongestMod.class);
	public static final String MODID = "jjk_strongest";

	public JjkStrongestMod(IEventBus bus) {
		NeoForge.EVENT_BUS.register(this);
		JjkStrongestModSounds.REGISTRY.register(bus);
		JjkStrongestModBlocks.REGISTRY.register(bus);
		JjkStrongestModBlockEntities.REGISTRY.register(bus);
		JjkStrongestModItems.REGISTRY.register(bus);
		JjkStrongestModEntities.REGISTRY.register(bus);

		JjkStrongestModTabs.REGISTRY.register(bus);

		JjkStrongestModMobEffects.REGISTRY.register(bus);

		JjkStrongestModParticleTypes.REGISTRY.register(bus);

		JjkStrongestModMenus.REGISTRY.register(bus);

		JjkStrongestModVariables.ATTACHMENT_TYPES.register(bus);
	}

	/**
	 * Server -> client. Pushes a value into a named {@link EditBox} of whatever
	 * screen the receiving client currently has open.
	 */
	public record TextboxSetMessage(String textboxid, String data) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<TextboxSetMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "textbox_set"));

		public static final StreamCodec<RegistryFriendlyByteBuf, TextboxSetMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
			// these were round-tripped through Component on 1.20.1 for no reason;
			// they are plain strings, and 1.21 component codecs need registry access
			buffer.writeUtf(message.textboxid());
			buffer.writeUtf(message.data());
		}, buffer -> new TextboxSetMessage(buffer.readUtf(), buffer.readUtf()));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}

		public static void handler(TextboxSetMessage message, IPayloadContext context) {
			if (message.data() == null)
				return;
			Screen currentScreen = Minecraft.getInstance().screen;
			Map<String, EditBox> textFieldsMap = new HashMap<>();
			if (currentScreen != null) {
				Field[] fields = currentScreen.getClass().getDeclaredFields();
				for (Field field : fields) {
					if (EditBox.class.isAssignableFrom(field.getType())) {
						try {
							field.setAccessible(true);
							EditBox textField = (EditBox) field.get(currentScreen);
							if (textField != null) {
								textFieldsMap.put(field.getName(), textField);
							}
						} catch (IllegalAccessException ex) {
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							ex.printStackTrace(pw);
							String exceptionAsString = sw.toString();
							JjkStrongestMod.LOGGER.error(exceptionAsString);
						}
					}
				}
			}
			if (textFieldsMap.get(message.textboxid()) != null) {
				textFieldsMap.get(message.textboxid()).setValue(message.data());
			}
		}
	}

	/**
	 * NeoForge replaced Forge's SimpleChannel with typed payloads registered in
	 * one place, so every packet the mod sends is declared here rather than each
	 * message class self-registering during common setup.
	 */
	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
	public static class NetworkRegistration {
		private static final String PROTOCOL_VERSION = "1";

		@SubscribeEvent
		public static void registerPayloads(RegisterPayloadHandlersEvent event) {
			PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

			registrar.playToServer(Technique1Message.TYPE, Technique1Message.STREAM_CODEC, Technique1Message::handler);
			registrar.playToServer(Technique2Message.TYPE, Technique2Message.STREAM_CODEC, Technique2Message::handler);
			registrar.playToServer(Technique3Message.TYPE, Technique3Message.STREAM_CODEC, Technique3Message::handler);
			registrar.playToServer(Technique4Message.TYPE, Technique4Message.STREAM_CODEC, Technique4Message::handler);
			registrar.playToServer(MarkExecuteMessage.TYPE, MarkExecuteMessage.STREAM_CODEC, MarkExecuteMessage::handler);
			registrar.playToServer(AbilitymenuMessage.TYPE, AbilitymenuMessage.STREAM_CODEC, AbilitymenuMessage::handler);

			registrar.playToClient(JjkStrongestModVariables.PlayerVariablesSyncMessage.TYPE, JjkStrongestModVariables.PlayerVariablesSyncMessage.STREAM_CODEC, JjkStrongestModVariables.PlayerVariablesSyncMessage::handler);
			registrar.playToClient(SpawnDomainSlashPacket.TYPE, SpawnDomainSlashPacket.STREAM_CODEC, SpawnDomainSlashPacket::handle);
			registrar.playToClient(TextboxSetMessage.TYPE, TextboxSetMessage.STREAM_CODEC, TextboxSetMessage::handler);
		}
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		// NeoForge dropped Forge's SidedThreadGroups; EffectiveSide reports the
		// logical side of the calling thread, which is what this guard wanted.
		if (EffectiveSide.get() == LogicalSide.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(ServerTickEvent.Post event) {
		List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
		workQueue.forEach(work -> {
			work.setValue(work.getValue() - 1);
			if (work.getValue() == 0)
				actions.add(work);
		});
		actions.forEach(e -> e.getKey().run());
		workQueue.removeAll(actions);
	}
}
