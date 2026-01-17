package net.mcreator.jjkstrongest.mixins;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.procedures.GetCurrentArmPoseProcedure;
import net.mcreator.jjkstrongest.procedures.ArmAnimationRegistryProcedure;
import net.mcreator.jjkstrongest.procedures.ArmAnimationDataProcedure;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonRendererMixin {
	@Shadow
	private void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, float equippedProgress, float swingProgress, HumanoidArm side) {
	}

	// intercept item rendering
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void onRenderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
			CallbackInfo ci) {
		if (player == null)
			return;
		// check if animation is active
		String animName = player.getPersistentData().getString("current_arm_animation");
		if (animName.isEmpty())
			return;
		// check if this animation forces visibility
		ArmAnimationDataProcedure anim = ArmAnimationRegistryProcedure.getAnimation(animName);
		if (anim == null || !anim.forceVisible)
			return;
		// determine which arm this is
		HumanoidArm side = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		// if animation uses BOTH arms, render both
		if (anim.useRightArm && anim.useLeftArm) {
			ci.cancel();
			// render RIGHT arm
			if (anim.useRightArm) {
				poseStack.pushPose();
				float[] poseRight = GetCurrentArmPoseProcedure.execute(player, HumanoidArm.RIGHT);
				if (poseRight != null) {
					poseStack.translate(poseRight[0], poseRight[1], poseRight[2]);
					poseStack.mulPose(Axis.XP.rotationDegrees(poseRight[3]));
					poseStack.mulPose(Axis.YP.rotationDegrees(poseRight[4]));
					poseStack.mulPose(Axis.ZP.rotationDegrees(poseRight[5]));
				}
				this.renderPlayerArm(poseStack, buffer, combinedLight, equippedProgress, swingProgress, HumanoidArm.RIGHT);
				poseStack.popPose();
			}
			// render LEFT arm
			if (anim.useLeftArm) {
				poseStack.pushPose();
				float[] poseLeft = GetCurrentArmPoseProcedure.execute(player, HumanoidArm.LEFT);
				if (poseLeft != null) {
					poseStack.translate(poseLeft[0], poseLeft[1], poseLeft[2]);
					poseStack.mulPose(Axis.XP.rotationDegrees(poseLeft[3]));
					poseStack.mulPose(Axis.YP.rotationDegrees(poseLeft[4]));
					poseStack.mulPose(Axis.ZP.rotationDegrees(poseLeft[5]));
				}
				this.renderPlayerArm(poseStack, buffer, combinedLight, equippedProgress, swingProgress, HumanoidArm.LEFT);
				poseStack.popPose();
			}
			return;
		}
		// single arm animation
		boolean shouldAnimate = (side == HumanoidArm.RIGHT && anim.useRightArm) || (side == HumanoidArm.LEFT && anim.useLeftArm);
		if (shouldAnimate) {
			ci.cancel();
			poseStack.pushPose();
			float[] pose = GetCurrentArmPoseProcedure.execute(player, side);
			if (pose != null) {
				poseStack.translate(pose[0], pose[1], pose[2]);
				poseStack.mulPose(Axis.XP.rotationDegrees(pose[3]));
				poseStack.mulPose(Axis.YP.rotationDegrees(pose[4]));
				poseStack.mulPose(Axis.ZP.rotationDegrees(pose[5]));
			}
			this.renderPlayerArm(poseStack, buffer, combinedLight, equippedProgress, swingProgress, side);
			poseStack.popPose();
		}
	}

	// keep existing inject for empty hand modification
	@Inject(method = "renderPlayerArm", at = @At("HEAD"))
	private void modifyEmptyHandPose(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float equipProgress, float swingProgress, HumanoidArm side, CallbackInfo ci) {
		LocalPlayer entity = Minecraft.getInstance().player;
		if (entity == null) {
			return;
		}
		// DEBUG - print left arm values every 60 frames
		if (side == HumanoidArm.LEFT) {
			double tx = entity.getPersistentData().getDouble("left_arm_translate_x");
			double ty = entity.getPersistentData().getDouble("left_arm_translate_y");
			if (tx != 0 || ty != 0) {
				System.out.println("LEFT ARM VALUES: tx=" + tx + " ty=" + ty);
			}
		}
		// check if animation is active
		String animName = entity.getPersistentData().getString("current_arm_animation");
		if (animName.isEmpty()) {
			// use editor values when no animation
			if (side == HumanoidArm.LEFT) {
				// left arm editor values
				double tx = entity.getPersistentData().getDouble("left_arm_translate_x");
				double ty = entity.getPersistentData().getDouble("left_arm_translate_y");
				double tz = entity.getPersistentData().getDouble("left_arm_translate_z");
				double rx = entity.getPersistentData().getDouble("left_arm_rotate_x");
				double ry = entity.getPersistentData().getDouble("left_arm_rotate_y");
				double rz = entity.getPersistentData().getDouble("left_arm_rotate_z");
				poseStack.translate(tx, ty, tz);
				poseStack.mulPose(Axis.XP.rotationDegrees((float) rx));
				poseStack.mulPose(Axis.YP.rotationDegrees((float) ry));
				poseStack.mulPose(Axis.ZP.rotationDegrees((float) rz));
			} else {
				// right arm editor values
				double tx = entity.getPersistentData().getDouble("arm_translate_x");
				double ty = entity.getPersistentData().getDouble("arm_translate_y");
				double tz = entity.getPersistentData().getDouble("arm_translate_z");
				double rx = entity.getPersistentData().getDouble("arm_rotate_x");
				double ry = entity.getPersistentData().getDouble("arm_rotate_y");
				double rz = entity.getPersistentData().getDouble("arm_rotate_z");
				poseStack.translate(tx, ty, tz);
				poseStack.mulPose(Axis.XP.rotationDegrees((float) rx));
				poseStack.mulPose(Axis.YP.rotationDegrees((float) ry));
				poseStack.mulPose(Axis.ZP.rotationDegrees((float) rz));
			}
			return;
		}
		// check if this animation forces visibility (already handled in renderArmWithItem)
		ArmAnimationDataProcedure anim = ArmAnimationRegistryProcedure.getAnimation(animName);
		if (anim != null && anim.forceVisible) {
			return; // handled by renderArmWithItem injection
		}
		// normal animation for empty hands
		float[] pose = GetCurrentArmPoseProcedure.execute(entity, side);
		if (pose != null) {
			poseStack.translate(pose[0], pose[1], pose[2]);
			poseStack.mulPose(Axis.XP.rotationDegrees(pose[3]));
			poseStack.mulPose(Axis.YP.rotationDegrees(pose[4]));
			poseStack.mulPose(Axis.ZP.rotationDegrees(pose[5]));
		} else {
			// default editor values (no animation active for this arm)
			if (side == HumanoidArm.LEFT) {
				// left arm editor values
				double tx = entity.getPersistentData().getDouble("left_arm_translate_x");
				double ty = entity.getPersistentData().getDouble("left_arm_translate_y");
				double tz = entity.getPersistentData().getDouble("left_arm_translate_z");
				double rx = entity.getPersistentData().getDouble("left_arm_rotate_x");
				double ry = entity.getPersistentData().getDouble("left_arm_rotate_y");
				double rz = entity.getPersistentData().getDouble("left_arm_rotate_z");
				poseStack.translate(tx, ty, tz);
				poseStack.mulPose(Axis.XP.rotationDegrees((float) rx));
				poseStack.mulPose(Axis.YP.rotationDegrees((float) ry));
				poseStack.mulPose(Axis.ZP.rotationDegrees((float) rz));
			} else {
				// right arm editor values
				double tx = entity.getPersistentData().getDouble("arm_translate_x");
				double ty = entity.getPersistentData().getDouble("arm_translate_y");
				double tz = entity.getPersistentData().getDouble("arm_translate_z");
				double rx = entity.getPersistentData().getDouble("arm_rotate_x");
				double ry = entity.getPersistentData().getDouble("arm_rotate_y");
				double rz = entity.getPersistentData().getDouble("arm_rotate_z");
				poseStack.translate(tx, ty, tz);
				poseStack.mulPose(Axis.XP.rotationDegrees((float) rx));
				poseStack.mulPose(Axis.YP.rotationDegrees((float) ry));
				poseStack.mulPose(Axis.ZP.rotationDegrees((float) rz));
			}
		}
	}
}
