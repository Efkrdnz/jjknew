package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;

@EventBusSubscriber(Dist.CLIENT)
public class WorldSlashLineRenderProcedure {
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		execute(null);
	}

	// renders preview line while button is held
	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		Level world = player.level();
		// check if player is actively holding the button (indicated by a holding flag)
		if (!player.getPersistentData().getBoolean("WorldSlashHolding")) {
			return;
		}
		// calculate current aim point very close to camera (like crosshair indicator)
		Vec3 lookVec = player.getLookAngle();
		Vec3 eyePos = player.getEyePosition(1.0f);
		// spawn particle very close to camera (1.5 blocks away) as screen indicator
		Vec3 indicatorPos = eyePos.add(lookVec.scale(1.5));
		world.addParticle(ParticleTypes.END_ROD, indicatorPos.x, indicatorPos.y, indicatorPos.z, 0, 0, 0);
	}
}
