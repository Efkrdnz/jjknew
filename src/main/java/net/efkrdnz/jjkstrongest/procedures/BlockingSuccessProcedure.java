package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class BlockingSuccessProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity());
		}
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		// Instead of checking capability, check if blocking animation is active!
		String currentAnim = entity.getPersistentData().getString("current_arm_animation");
		boolean isHoldingBlock = currentAnim.equals("blocking") && entity.getPersistentData().getBoolean("arm_anim_holding");
		if (isHoldingBlock) {
			// Play impact animation
			StopArmAnimationProcedure.execute(entity);
			PlayArmAnimationProcedure.execute(entity, "blocking", false);
			// Cancel damage on server
			if (!entity.level().isClientSide()) {
				if (event instanceof ICancellableEvent cancellable)
					cancellable.setCanceled(true);
			}
		}
	}
}
