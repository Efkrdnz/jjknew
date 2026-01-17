package net.mcreator.jjkstrongest.mixins;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.FallingBlockEntity;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {
	@Shadow
	private int time;

	// removes falling block delay
	@Inject(method = "tick", at = @At("HEAD"))
	private void removeUpdateDelay(CallbackInfo ci) {
		this.time = 0;
	}

	// forces render and physics update
	@Inject(method = "tick", at = @At("TAIL"))
	private void forceRenderAndPhysicsUpdate(CallbackInfo ci) {
		FallingBlockEntity self = (FallingBlockEntity) (Object) this;
		Level world = self.level();
		if (!world.isClientSide) {
			self.setPos(self.getX(), self.getY(), self.getZ());
			self.setDeltaMovement(self.getDeltaMovement());
			self.hasImpulse = true;
		}
	}
}
