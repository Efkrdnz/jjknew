package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.client.MalevolentShrineSlashManager.DomainSlash;
import net.mcreator.jjkstrongest.client.MalevolentShrineSlashManager;
import net.mcreator.jjkstrongest.client.JjkShaderManager;
import net.mcreator.jjkstrongest.client.renderer.MalevolentShrineSlashRenderer;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MalevolentShrineSlashRenderer {
	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;
		if (JjkShaderManager.DISMANTLE_RENDER_TYPE == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
		float partialTick = event.getPartialTick();
		float timeSeconds = (mc.level.getGameTime() + partialTick) / 20.0f;
		// render all active slashes in batches
		for (DomainSlash slash : MalevolentShrineSlashManager.getActiveSlashes()) {
			renderSlash(slash, poseStack, bufferSource, cameraPos, timeSeconds, partialTick);
		}
		// flush buffer once for all slashes
		bufferSource.endBatch(JjkShaderManager.DISMANTLE_RENDER_TYPE);
	}

	// render single slash
	private static void renderSlash(DomainSlash slash, PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, float timeSeconds, float partialTick) {
		// prepare shader
		if (!JjkShaderManager.beginFrameCaptureDismantle(timeSeconds, slash.style, slash.seed, slash.length, slash.width, slash.colorR, slash.colorG, slash.colorB))
			return;
		poseStack.pushPose();
		// translate to slash position (relative to camera)
		Vec3 renderPos = slash.position.subtract(cameraPos);
		poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
		// get normalized direction
		Vec3 dir = slash.direction;
		float dx = (float) dir.x;
		float dy = (float) dir.y;
		float dz = (float) dir.z;
		// rotate quad to face slash direction
		float yaw = (float) Mth.atan2((double) dx, (double) dz);
		float horiz = Mth.sqrt(dx * dx + dz * dz);
		float pitch = (float) (-Mth.atan2((double) dy, (double) horiz));
		poseStack.mulPose(Axis.YP.rotation(yaw));
		poseStack.mulPose(Axis.XP.rotation(pitch));
		poseStack.mulPose(Axis.ZP.rotation(slash.roll));
		// get current size and alpha
		float currentLength = slash.getCurrentLength();
		float alpha = slash.getAlpha();
		// scale quad
		poseStack.scale(currentLength, slash.width, 1.0f);
		// render quad
		VertexConsumer vc = bufferSource.getBuffer(JjkShaderManager.DISMANTLE_RENDER_TYPE);
		Matrix4f matrix = poseStack.last().pose();
		int alphaInt = (int) (alpha * 255);
		int color = (alphaInt << 24) | 0xFFFFFF;
		vc.vertex(matrix, -0.5f, -0.5f, 0.0f).uv(0.0f, 1.0f).endVertex();
		vc.vertex(matrix, 0.5f, -0.5f, 0.0f).uv(1.0f, 1.0f).endVertex();
		vc.vertex(matrix, 0.5f, 0.5f, 0.0f).uv(1.0f, 0.0f).endVertex();
		vc.vertex(matrix, -0.5f, 0.5f, 0.0f).uv(0.0f, 0.0f).endVertex();
		poseStack.popPose();
	}
}
