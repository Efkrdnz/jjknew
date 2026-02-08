package net.mcreator.jjkstrongest.client.renderer;

import org.joml.Matrix4f;

import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;
import net.mcreator.jjkstrongest.client.renderer.InformationOverloadDebugLinesClientRenderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

@Mod.EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class InformationOverloadDebugLinesClientRenderer {
	private static final RenderType OVERLAY_LINES = RenderType.create("information_overload_lines", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader)).setTransparencyState(new RenderStateShard.TransparencyStateShard("info_overload_add", () -> {
				com.mojang.blaze3d.systems.RenderSystem.enableBlend();
				com.mojang.blaze3d.systems.RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
			}, () -> {
				com.mojang.blaze3d.systems.RenderSystem.disableBlend();
				com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
			})).setCullState(new RenderStateShard.CullStateShard(false)).setDepthTestState(new RenderStateShard.DepthTestStateShard("always", org.lwjgl.opengl.GL11.GL_ALWAYS)).setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
					.createCompositeState(false));

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.player == null || mc.level == null)
			return;
		if (mc.screen != null)
			return;
		if (!mc.options.getCameraType().isFirstPerson())
			return;
		Player player = mc.player;
		if (!player.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD.get()))
			return;
		if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND)
			return;
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		renderOverlay(player, poseStack, bufferSource, event.getPartialTick());
		bufferSource.endBatch(OVERLAY_LINES);
	}

	private static void renderOverlay(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
		float t = (player.tickCount + partialTick) / 20.0f;
		float strength = getStrength(player);
		poseStack.pushPose();
		poseStack.translate(0.0, 0.0, -0.55);
		float scale = 1.02f;
		poseStack.scale(scale, scale, scale);
		VertexConsumer vc = bufferSource.getBuffer(OVERLAY_LINES);
		Matrix4f m = poseStack.last().pose();
		int rings = 3 + (int) (strength * 3.0f);
		int sym = 6 + (int) (strength * 6.0f);
		float baseThickness = 0.0028f - 0.0014f * strength;
		if (baseThickness < 0.0012f)
			baseThickness = 0.0012f;
		float alpha = 0.26f + 0.44f * strength;
		float block = t * 0.5f;
		float id0 = (float) Math.floor(block);
		float id1 = id0 + 1.0f;
		float u = fract(block);
		float blend = smoothstep(0.00f, 0.10f, u);
		Params p0 = paramsFor(id0);
		Params p1 = paramsFor(id1);
		Params p = Params.mix(p0, p1, blend);
		float spin = t * (0.42f + 0.95f * strength);
		for (int r = 0; r < rings; r++) {
			float rf = r / (float) Math.max(1, rings - 1);
			float ringRot = spin * (0.22f + 0.52f * rf);
			float ringScale = 0.68f + 0.18f * rf;
			float cr = 0.10f;
			float cg = 0.70f;
			float cb = 1.00f;
			float hot = 0.30f + 0.70f * rf;
			cr = lerp(cr, 0.90f, hot);
			cg = lerp(cg, 0.22f, hot);
			cb = lerp(cb, 0.98f, hot);
			float flick = 0.90f + 0.10f * (float) Math.sin(t * 7.5f + rf * 6.0f);
			float a = alpha * flick * (0.78f + 0.22f * rf);
			float thick = baseThickness * (0.92f - 0.18f * rf);
			drawSymmetricSpiro(vc, m, p, sym, ringScale, ringRot, thick, cr, cg, cb, a);
		}
		float flash = smoothstep(0.992f, 1.0f, u) * (0.20f + 0.40f * strength);
		if (flash > 0.001f) {
			drawCenterBurst(vc, m, flash * 0.30f, 0.55f + 0.35f * strength);
		}
		poseStack.popPose();
	}

	private static void drawSymmetricSpiro(VertexConsumer vc, Matrix4f m, Params p, int sym, float scale, float rot, float thickness, float r, float g, float b, float a) {
		int points = 140;
		float lastX = 0, lastY = 0;
		boolean hasLast = false;
		for (int i = 0; i <= points; i++) {
			float th = (i / (float) points) * (float) (Math.PI * 2.0);
			float wob = (float) Math.sin(th * p.twist + p.timePhase) * (0.015f + 0.020f * p.wob);
			float x = (p.R - p.r) * (float) Math.cos(th) + p.d * (float) Math.cos(((p.R - p.r) / p.r) * th);
			float y = (p.R - p.r) * (float) Math.sin(th) - p.d * (float) Math.sin(((p.R - p.r) / p.r) * th);
			float px = (x + wob) * scale;
			float py = (y - wob) * scale;
			float sx = (float) (px * Math.cos(rot) - py * Math.sin(rot));
			float sy = (float) (px * Math.sin(rot) + py * Math.cos(rot));
			if (hasLast) {
				for (int k = 0; k < sym; k++) {
					float ang = (k / (float) sym) * (float) (Math.PI * 2.0);
					float c = (float) Math.cos(ang);
					float s = (float) Math.sin(ang);
					float ax = lastX * c - lastY * s;
					float ay = lastX * s + lastY * c;
					float bx = sx * c - sy * s;
					float by = sx * s + sy * c;
					addLineQuad(vc, m, ax, ay, bx, by, thickness, r, g, b, a);
				}
			}
			lastX = sx;
			lastY = sy;
			hasLast = true;
		}
	}

	private static void addLineQuad(VertexConsumer vc, Matrix4f m, float x1, float y1, float x2, float y2, float w, float r, float g, float b, float a) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len < 1e-6f)
			return;
		float nx = -dy / len;
		float ny = dx / len;
		float ox = nx * w;
		float oy = ny * w;
		int light = LightTexture.FULL_BRIGHT;
		vc.vertex(m, x1 - ox, y1 - oy, 0).color(r, g, b, a).uv2(light).overlayCoords(OverlayTexture.NO_OVERLAY).endVertex();
		vc.vertex(m, x1 + ox, y1 + oy, 0).color(r, g, b, a).uv2(light).overlayCoords(OverlayTexture.NO_OVERLAY).endVertex();
		vc.vertex(m, x2 + ox, y2 + oy, 0).color(r, g, b, a).uv2(light).overlayCoords(OverlayTexture.NO_OVERLAY).endVertex();
		vc.vertex(m, x2 - ox, y2 - oy, 0).color(r, g, b, a).uv2(light).overlayCoords(OverlayTexture.NO_OVERLAY).endVertex();
	}

	private static void drawCenterBurst(VertexConsumer vc, Matrix4f m, float a, float size) {
		float w = 0.0045f;
		float r = 0.90f;
		float g = 0.97f;
		float b = 1.00f;
		addLineQuad(vc, m, -size, 0, size, 0, w, r, g, b, a);
		addLineQuad(vc, m, 0, -size, 0, size, w, r, g, b, a);
		addLineQuad(vc, m, -size * 0.7f, -size * 0.7f, size * 0.7f, size * 0.7f, w, r, g, b, a * 0.6f);
		addLineQuad(vc, m, -size * 0.7f, size * 0.7f, size * 0.7f, -size * 0.7f, w, r, g, b, a * 0.6f);
	}

	private static float getStrength(Player player) {
		var inst = player.getEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD.get());
		if (inst == null)
			return 0.0f;
		int amp = inst.getAmplifier();
		float base = 0.85f + amp * 0.15f;
		if (base > 1.0f)
			base = 1.0f;
		float pulse = 0.88f + 0.12f * (float) Math.sin(player.tickCount * 0.45f);
		return clamp(base * pulse, 0.0f, 1.0f);
	}

	private static float clamp(float v, float a, float b) {
		return v < a ? a : (v > b ? b : v);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float fract(float x) {
		return x - (float) Math.floor(x);
	}

	private static float smoothstep(float a, float b, float x) {
		float t = clamp((x - a) / (b - a), 0.0f, 1.0f);
		return t * t * (3.0f - 2.0f * t);
	}

	private static Params paramsFor(float id) {
		float h1 = hash11(id * 7.1f);
		float h2 = hash11(id * 11.3f);
		float h3 = hash11(id * 19.7f);
		float ri = (float) Math.floor(4.0f + h1 * 7.0f);
		float rj = (float) Math.floor(3.0f + h2 * 6.0f);
		float R = 0.70f;
		float r = R * (rj / ri);
		float d = 0.35f + 0.25f * h3;
		float twist = 6.0f + (float) Math.floor(hash11(id * 3.9f) * 8.0f);
		float wob = 0.35f + 0.35f * hash11(id * 13.7f);
		float timePhase = id * 1.7f;
		return new Params(R, r, d, twist, wob, timePhase);
	}

	private static float hash11(float p) {
		p = fract(p * 0.1031f);
		p = p * (p + 33.33f);
		p = p * (p + p);
		return fract(p);
	}

	private static class Params {
		public final float R;
		public final float r;
		public final float d;
		public final float twist;
		public final float wob;
		public final float timePhase;

		public Params(float R, float r, float d, float twist, float wob, float timePhase) {
			this.R = R;
			this.r = r;
			this.d = d;
			this.twist = twist;
			this.wob = wob;
			this.timePhase = timePhase;
		}

		public static Params mix(Params a, Params b, float t) {
			return new Params(lerp(a.R, b.R, t), lerp(a.r, b.r, t), lerp(a.d, b.d, t), lerp(a.twist, b.twist, t), lerp(a.wob, b.wob, t), lerp(a.timePhase, b.timePhase, t));
		}
	}
}
