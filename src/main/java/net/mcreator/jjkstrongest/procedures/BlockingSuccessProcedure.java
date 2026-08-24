package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class BlockingSuccessProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
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
				if (event != null && event.isCancelable()) {
					event.setCanceled(true);
				} else if (event != null && event.hasResult()) {
					event.setResult(Event.Result.DENY);
				}
			}
		}
	}
}
