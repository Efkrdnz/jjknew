/**
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside net.mcreator.jjkstrongest as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package net.mcreator.jjkstrongest;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(modid = "jjk_strongest", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScrollDisable {
	@SubscribeEvent
	public static void Scroll(InputEvent.MouseScrollingEvent event) {
		Entity entity = Minecraft.getInstance().player;
		if (entity == null)
			return;
		String animName = entity.getPersistentData().getString("current_arm_animation");
		if (!animName.isEmpty()) {
			event.setCanceled(true);
		}
	}
}
