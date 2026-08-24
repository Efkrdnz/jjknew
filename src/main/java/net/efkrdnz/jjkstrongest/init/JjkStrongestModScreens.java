package net.efkrdnz.jjkstrongest.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.efkrdnz.jjkstrongest.client.gui.ArmAnimationEditorScreen;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

@EventBusSubscriber(modid = JjkStrongestMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class JjkStrongestModScreens {
	/**
	 * MenuScreens.register went private in 1.21; NeoForge exposes it through
	 * RegisterMenuScreensEvent, which also removes the need to enqueueWork.
	 */
	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(JjkStrongestModMenus.ARM_ANIMATION_EDITOR.get(), ArmAnimationEditorScreen::new);
	}
}
