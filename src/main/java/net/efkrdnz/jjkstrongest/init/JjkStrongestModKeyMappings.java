
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.efkrdnz.jjkstrongest.init;

import org.lwjgl.glfw.GLFW;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import net.efkrdnz.jjkstrongest.network.Technique4Message;
import net.efkrdnz.jjkstrongest.network.Technique3Message;
import net.efkrdnz.jjkstrongest.network.Technique2Message;
import net.efkrdnz.jjkstrongest.network.Technique1Message;
import net.efkrdnz.jjkstrongest.network.MarkExecuteMessage;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class JjkStrongestModKeyMappings {
	public static final KeyMapping TECHNIQUE_1 = new KeyMapping("key.jjk_strongest.technique_1", GLFW.GLFW_KEY_Z, "key.categories.jjk") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				PacketDistributor.sendToServer(new Technique1Message(0, 0));
				Technique1Message.pressAction(Minecraft.getInstance().player, 0, 0);
				TECHNIQUE_1_LASTPRESS = System.currentTimeMillis();
			} else if (isDownOld != isDown && !isDown) {
				int dt = (int) (System.currentTimeMillis() - TECHNIQUE_1_LASTPRESS);
				PacketDistributor.sendToServer(new Technique1Message(1, dt));
				Technique1Message.pressAction(Minecraft.getInstance().player, 1, dt);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping TECHNIQUE_2 = new KeyMapping("key.jjk_strongest.technique_2", GLFW.GLFW_KEY_X, "key.categories.jjk") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				PacketDistributor.sendToServer(new Technique2Message(0, 0));
				Technique2Message.pressAction(Minecraft.getInstance().player, 0, 0);
				TECHNIQUE_2_LASTPRESS = System.currentTimeMillis();
			} else if (isDownOld != isDown && !isDown) {
				int dt = (int) (System.currentTimeMillis() - TECHNIQUE_2_LASTPRESS);
				PacketDistributor.sendToServer(new Technique2Message(1, dt));
				Technique2Message.pressAction(Minecraft.getInstance().player, 1, dt);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping TECHNIQUE_3 = new KeyMapping("key.jjk_strongest.technique_3", GLFW.GLFW_KEY_C, "key.categories.jjk") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				PacketDistributor.sendToServer(new Technique3Message(0, 0));
				Technique3Message.pressAction(Minecraft.getInstance().player, 0, 0);
				TECHNIQUE_3_LASTPRESS = System.currentTimeMillis();
			} else if (isDownOld != isDown && !isDown) {
				int dt = (int) (System.currentTimeMillis() - TECHNIQUE_3_LASTPRESS);
				PacketDistributor.sendToServer(new Technique3Message(1, dt));
				Technique3Message.pressAction(Minecraft.getInstance().player, 1, dt);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping TECHNIQUE_4 = new KeyMapping("key.jjk_strongest.technique_4", GLFW.GLFW_KEY_V, "key.categories.jjk") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				PacketDistributor.sendToServer(new Technique4Message(0, 0));
				Technique4Message.pressAction(Minecraft.getInstance().player, 0, 0);
				TECHNIQUE_4_LASTPRESS = System.currentTimeMillis();
			} else if (isDownOld != isDown && !isDown) {
				int dt = (int) (System.currentTimeMillis() - TECHNIQUE_4_LASTPRESS);
				PacketDistributor.sendToServer(new Technique4Message(1, dt));
				Technique4Message.pressAction(Minecraft.getInstance().player, 1, dt);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping ABILITYMENU = new KeyMapping("key.jjk_strongest.abilitymenu", GLFW.GLFW_KEY_R, "key.categories.jjk");
	public static final KeyMapping MARK_EXECUTE = new KeyMapping("key.jjk_strongest.mark_execute", GLFW.GLFW_KEY_UNKNOWN, "key.categories.jjk") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				PacketDistributor.sendToServer(new MarkExecuteMessage(0, 0));
				MarkExecuteMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	private static long TECHNIQUE_1_LASTPRESS = 0;
	private static long TECHNIQUE_2_LASTPRESS = 0;
	private static long TECHNIQUE_3_LASTPRESS = 0;
	private static long TECHNIQUE_4_LASTPRESS = 0;

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(TECHNIQUE_1);
		event.register(TECHNIQUE_2);
		event.register(TECHNIQUE_3);
		event.register(TECHNIQUE_4);
		event.register(ABILITYMENU);
		event.register(MARK_EXECUTE);
	}

	@EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().screen == null) {
				TECHNIQUE_1.consumeClick();
				TECHNIQUE_2.consumeClick();
				TECHNIQUE_3.consumeClick();
				TECHNIQUE_4.consumeClick();
				MARK_EXECUTE.consumeClick();
			}
		}
	}
}
