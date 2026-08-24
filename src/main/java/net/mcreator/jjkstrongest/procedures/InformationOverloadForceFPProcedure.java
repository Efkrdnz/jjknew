package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;

import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InformationOverloadForceFPProcedure {
	private static boolean forced = false;
	private static CameraType prev = null;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void updateWorldTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || minecraft.player == null)
			return;
		execute(event, minecraft.player);
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable net.minecraftforge.eventbus.api.Event event, Entity entity) {
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
