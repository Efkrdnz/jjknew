package net.efkrdnz.jjkstrongest.mixins;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;

@Mixin(LivingEntity.class)
public class DisableSwingMixin {
	// intercept swing method to cancel animation during custom animations
	@Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
	private void cancelSwingDuringAnimation(InteractionHand hand, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		// only apply to players
		if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
			return;
		}
		String animName = entity.getPersistentData().getString("current_arm_animation");
		if (!animName.isEmpty()) {
			// cancel the swing entirely
			ci.cancel();
		}
	}

	/**
	 * A swing that hit nothing may still have hit a domain barrier.
	 *
	 * <p>There is no vanilla event for swinging at an analytic surface — {@code
	 * LeftClickEmpty} never leaves the client and {@code AttackEntityEvent} needs an
	 * entity — but a miss does reach the server as a swing, which is what this is. The
	 * server does its own raycast from the player's eye, so the client never gets to say
	 * where the blow landed.
	 *
	 * <p>Injected at TAIL rather than HEAD so a swing the animation system cancelled above
	 * does not also punch the barrier.
	 */
	@Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("TAIL"))
	private void jjk_strongest$strikeDomainBarrier(InteractionHand hand, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!(entity instanceof net.minecraft.world.entity.player.Player player))
			return;
		if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel))
			return;
		if (net.efkrdnz.jjkstrongest.domain.DomainRegistry.activeCount == 0)
			return;
		net.efkrdnz.jjkstrongest.domain.DomainBarrierStrike.trySwing(serverLevel, player);
	}
}
