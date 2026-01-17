package net.mcreator.jjkstrongest.client.renderer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.procedures.RenderRedFirstPersonProcedure;
import net.mcreator.jjkstrongest.procedures.RenderFlameArrowFirstPersonProcedure;
import net.mcreator.jjkstrongest.procedures.RenderBlueFirstPersonProcedure;
import net.mcreator.jjkstrongest.client.renderer.RedFirstPersonRenderHook;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class RedFirstPersonRenderHook {
	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		if (!mc.options.getCameraType().isFirstPerson())
			return;
		RenderRedFirstPersonProcedure.execute(mc, mc.player, event.getPoseStack(), event.getHand(), event.getPartialTick());
		RenderBlueFirstPersonProcedure.execute(mc, mc.player, event.getPoseStack(), event.getHand(), event.getPartialTick());
		RenderFlameArrowFirstPersonProcedure.execute(mc, mc.player, event.getPoseStack(), event.getHand(), event.getPartialTick());
	}
}
