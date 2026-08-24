package net.efkrdnz.jjkstrongest.client.renderer;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.procedures.RenderRedFirstPersonProcedure;
import net.efkrdnz.jjkstrongest.procedures.RenderPurpleChargeFirstPersonProcedure;
import net.efkrdnz.jjkstrongest.procedures.RenderImaginaryPurpleFirstPersonProcedure;
import net.efkrdnz.jjkstrongest.procedures.RenderFlameArrowFirstPersonProcedure;
import net.efkrdnz.jjkstrongest.procedures.RenderBlueFirstPersonProcedure;
import net.efkrdnz.jjkstrongest.client.renderer.RedFirstPersonRenderHook;

@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
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
		RenderPurpleChargeFirstPersonProcedure.execute(mc, mc.player, event.getPoseStack(), event.getHand(), event.getPartialTick());
		RenderImaginaryPurpleFirstPersonProcedure.execute(mc, mc.player, event.getPoseStack(), event.getHand(), event.getPartialTick());
	}
}
