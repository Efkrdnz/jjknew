package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;

@OnlyIn(Dist.CLIENT)
public class ClientLookPunchProcedure {
	// sends a normal attack packet to server for the entity under crosshair
	public static void execute(Entity entity) {
		if (!(entity instanceof LocalPlayer player))
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode == null)
			return;
		HitResult hr = mc.hitResult;
		if (hr instanceof EntityHitResult ehr) {
			player.swing(InteractionHand.MAIN_HAND);
			mc.gameMode.attack(player, ehr.getEntity());
		} else {
			player.swing(InteractionHand.MAIN_HAND);
		}
	}
}
