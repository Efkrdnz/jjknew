package net.mcreator.jjkstrongest.mixins;

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
}
