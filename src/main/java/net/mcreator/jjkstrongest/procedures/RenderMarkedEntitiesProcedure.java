package net.mcreator.jjkstrongest.procedures;

import org.joml.Matrix4f;
import org.joml.Matrix3f;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import java.util.UUID;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderMarkedEntitiesProcedure {
	// textures for marks
	private static final ResourceLocation SUKUNA_MARK = new ResourceLocation("jjk_strongest", "textures/mark_sukuna.png");
	private static final ResourceLocation GOJO_MARK = new ResourceLocation("jjk_strongest", "textures/mark_gojo.png");
	private static final ResourceLocation SUKUNA_MARK_TARGETED = new ResourceLocation("jjk_strongest", "textures/mark_sukuna_targeted.png");
	private static final ResourceLocation GOJO_MARK_TARGETED = new ResourceLocation("jjk_strongest", "textures/mark_gojo_targeted.png");

	@SubscribeEvent
	public static void onRenderWorld(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.level == null)
			return;
		String currentTechnique = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer;
		String markedJson = (player.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).marked_entities;
		if (markedJson == null || markedJson.isEmpty() || currentTechnique == null || currentTechnique.isEmpty())
			return;
		JsonArray markedArray;
		try {
			markedArray = JsonParser.parseString(markedJson).getAsJsonArray();
		} catch (Exception e) {
			return;
		}
		if (markedArray.size() == 0)
			return;
		// get player look direction for targeting check
		Vec3 lookVec = player.getLookAngle();
		Vec3 playerPos = player.getEyePosition();
		double minDotThreshold = Math.cos(Math.toRadians(20));
		// determine which entity is being targeted
		String targetedUUID = null;
		double bestDot = -1.0;
		for (int i = 0; i < markedArray.size(); i++) {
			String uuidString = markedArray.get(i).getAsString();
			try {
				UUID uuid = UUID.fromString(uuidString);
				// find entity in client world
				for (Entity worldEntity : mc.level.entitiesForRendering()) {
					if (worldEntity.getUUID().equals(uuid) && worldEntity.isAlive()) {
						Vec3 targetPos = worldEntity.position().add(0, worldEntity.getBbHeight() / 2, 0);
						Vec3 toTarget = targetPos.subtract(playerPos).normalize();
						double dot = lookVec.dot(toTarget);
						if (dot > minDotThreshold && dot > bestDot) {
							bestDot = dot;
							targetedUUID = uuidString;
						}
						break;
					}
				}
			} catch (Exception e) {
				// skip
			}
		}
		// render marks
		ResourceLocation normalTexture = currentTechnique.equals("sukuna") ? SUKUNA_MARK : GOJO_MARK;
		ResourceLocation targetedTexture = currentTechnique.equals("sukuna") ? SUKUNA_MARK_TARGETED : GOJO_MARK_TARGETED;
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		for (int i = 0; i < markedArray.size(); i++) {
			String uuidString = markedArray.get(i).getAsString();
			try {
				UUID uuid = UUID.fromString(uuidString);
				// find entity in client world
				for (Entity worldEntity : mc.level.entitiesForRendering()) {
					if (worldEntity.getUUID().equals(uuid) && worldEntity.isAlive()) {
						boolean isTargeted = uuidString.equals(targetedUUID);
						ResourceLocation texture = isTargeted ? targetedTexture : normalTexture;
						renderMarkOnEntity(poseStack, bufferSource, worldEntity, player, texture, event.getPartialTick(), isTargeted);
						break;
					}
				}
			} catch (Exception e) {
				// skip
			}
		}
		bufferSource.endBatch();
	}

	// render single mark on entity
	private static void renderMarkOnEntity(PoseStack poseStack, MultiBufferSource buffer, Entity entity, Player player, ResourceLocation texture, float partialTicks, boolean isTargeted) {
		// interpolate entity position
		double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
		double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks;
		double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;
		// camera position
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		poseStack.pushPose();
		// translate to entity position (above head)
		poseStack.translate(x - cameraPos.x, y - cameraPos.y + entity.getBbHeight() + 0.5, z - cameraPos.z);
		// billboard (face camera)
		poseStack.mulPose(Axis.YP.rotationDegrees(-player.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(player.getXRot()));
		// scale
		float scale = isTargeted ? 0.7f : 0.5f;
		poseStack.scale(scale, scale, scale);
		// render through walls - use eyes render type for see-through effect
		RenderType renderType = RenderType.eyes(texture);
		VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();
		// quad vertices
		float size = 1.0f;
		int alpha = isTargeted ? 255 : 200;
		// vertex order for quad
		addVertex(vertexConsumer, matrix4f, matrix3f, -size, size, 0, 0, 0, alpha); // top left
		addVertex(vertexConsumer, matrix4f, matrix3f, size, size, 0, 1, 0, alpha); // top right
		addVertex(vertexConsumer, matrix4f, matrix3f, size, -size, 0, 1, 1, alpha); // bottom right
		addVertex(vertexConsumer, matrix4f, matrix3f, -size, -size, 0, 0, 1, alpha); // bottom left
		poseStack.popPose();
	}

	// add vertex to buffer
	private static void addVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v, int alpha) {
		consumer.vertex(pose, x, y, z).color(255, 255, 255, alpha).uv(u, v).overlayCoords(0, 10).uv2(240, 240).normal(normal, 0, 1, 0).endVertex();
	}
}
