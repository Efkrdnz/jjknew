package net.mcreator.jjkstrongest.mixins;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public abstract class DisableHotbarKeymappingMixin {
	@Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
	public void inject1(CallbackInfoReturnable<Boolean> cir) {
		Entity entity = Minecraft.getInstance().player;
		String animName = entity.getPersistentData().getString("current_arm_animation");
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.1")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.2")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.3")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.4")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.5")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.6")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.7")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.8")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
		if (((KeyMapping) (Object) this).getName().contains("key.hotbar.9")) {
			if (!animName.isEmpty()) {
				cir.setReturnValue(false);
			}
		}
	}
}
