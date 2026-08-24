package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

import javax.annotation.Nullable;

@EventBusSubscriber(value = Dist.CLIENT)
public class InformationOverloadForceFPProcedure {
	private static boolean forced = false;
	private static CameraType prev = null;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void updateWorldTick(ClientTickEvent.Pre event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || minecraft.player == null)
			return;
		execute(event, minecraft.player);
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable net.neoforged.bus.api.Event event, Entity entity) {
		if (entity == null)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null)
			return;
		boolean has = (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD.get()));
		if (has) {
			if (!forced) {
				prev = minecraft.options.getCameraType();
				forced = true;
			}
			if (!minecraft.options.getCameraType().isFirstPerson()) {
				minecraft.options.setCameraType(CameraType.FIRST_PERSON);
			}
		} else {
			if (forced) {
				forced = false;
				if (prev != null && minecraft.options.getCameraType() != prev) {
					minecraft.options.setCameraType(prev);
				}
				prev = null;
			}
		}
	}
}
