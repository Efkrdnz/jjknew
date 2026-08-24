package net.efkrdnz.jjkvoice;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.efkrdnz.jjkvoice.network.VoiceNetwork;

/**
 * JJK Strongest: Voice Commands.
 *
 * <p>An optional companion to JJK Strongest that lets the player speak an
 * incantation instead of pressing a key. Recognition is local, deterministic, and
 * built from recordings the player makes themselves -- there is no model, no
 * account, and no network traffic anywhere in this mod.
 *
 * <p>This replaces the host mod's previous approach, where an external program
 * wrote a word into a text file under Documents and the mod polled that file on
 * the server thread. Audio now never leaves the client, and the client only ever
 * <em>requests</em> a technique: every gameplay decision still belongs to the
 * host mod on the logical server.
 */
@Mod(JjkVoiceMod.MOD_ID)
public final class JjkVoiceMod {
	public static final String MOD_ID = "jjkvoice";
	public static final Logger LOGGER = LogUtils.getLogger();

	public JjkVoiceMod(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(VoiceNetwork::register);
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
