package net.efkrdnz.jjkvoice.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import net.efkrdnz.jjkvoice.JjkVoiceMod;

/**
 * The push-to-talk binding.
 *
 * <p>Defaults to apostrophe rather than the usual V, because Simple Voice Chat
 * already claims V for proximity chat and colliding with it would make the two
 * unusable together.
 */
@EventBusSubscriber(modid = JjkVoiceMod.MOD_ID, value = Dist.CLIENT)
public final class VoiceKeyMappings {
	public static final String CATEGORY = "key.categories.jjkvoice";

	public static final KeyMapping VOICE_COMMAND = new KeyMapping(
			"key.jjkvoice.voice_command",
			KeyConflictContext.IN_GAME,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_APOSTROPHE,
			CATEGORY);

	private VoiceKeyMappings() {
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(VOICE_COMMAND);
	}
}
