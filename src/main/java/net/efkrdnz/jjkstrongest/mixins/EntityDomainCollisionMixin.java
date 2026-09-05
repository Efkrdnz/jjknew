package net.efkrdnz.jjkstrongest.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.domain.DomainCollision;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes a domain shell solid.
 *
 * <p>{@code Entity#collide} is the seam vanilla already uses to answer "given this
 * desired movement, how much of it is allowed" — everything downstream in
 * {@code move()} (horizontal/vertical collision flags, {@code onGround}, step assist,
 * fall damage) is derived by comparing its input with its output. Clamping here
 * therefore gets all of that for free, including standing on the domain's invisible
 * floor plane.
 *
 * <p>It also runs on both logical sides, so the client predicts exactly what the
 * server will allow and the player is never snapped back.
 *
 * <p>This is a hot path shared with every other mod, so the very first thing it does
 * is read one volatile int; with no domain open anywhere — which is nearly always —
 * that is the entire cost.
 */
@Mixin(Entity.class)
public abstract class EntityDomainCollisionMixin {

	@Inject(method = "collide", at = @At("RETURN"), cancellable = true)
	private void jjk_strongest$clampToDomainShell(Vec3 desired, CallbackInfoReturnable<Vec3> cir) {
		if (DomainRegistry.activeCount == 0)
			return;
		Vec3 resolved = cir.getReturnValue();
		if (resolved == null)
			return;
		Vec3 clamped = DomainCollision.clamp((Entity) (Object) this, resolved);
		if (clamped != resolved)
			cir.setReturnValue(clamped);
	}
}
