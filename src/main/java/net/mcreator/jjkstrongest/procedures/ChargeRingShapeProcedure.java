package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.eventbus.api.Event;

import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;

public class ChargeRingShapeProcedure {
	private static BufferBuilder bufferBuilder = null;
	private static VertexBuffer vertexBuffer = null;
	private static VertexFormat.Mode mode = null;
	private static VertexFormat format = null;

	private static void add(double x, double y, double z, int color) {
		add(x, y, z, 0.0F, 0.0F, color);
	}

	private static void add(double x, double y, double z, float u, float v, int color) {
		if (bufferBuilder == null || !bufferBuilder.building())
			return;
		if (format == DefaultVertexFormat.POSITION_COLOR) {
			bufferBuilder.vertex(x, y, z).color(color).endVertex();
		} else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
			bufferBuilder.vertex(x, y, z).uv(u, v).color(color).endVertex();
		}
	}

	private static boolean begin(VertexFormat.Mode mode, VertexFormat format, boolean update) {
		if (ChargeRingShapeProcedure.bufferBuilder == null || !ChargeRingShapeProcedure.bufferBuilder.building()) {
			if (update)
				clear();
			if (ChargeRingShapeProcedure.vertexBuffer == null) {
				if (format == DefaultVertexFormat.POSITION_COLOR) {
					ChargeRingShapeProcedure.mode = mode;
					ChargeRingShapeProcedure.format = format;
					ChargeRingShapeProcedure.bufferBuilder = Tesselator.getInstance().getBuilder();
					ChargeRingShapeProcedure.bufferBuilder.begin(mode, DefaultVertexFormat.POSITION_COLOR);
					return true;
				} else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
					ChargeRingShapeProcedure.mode = mode;
					ChargeRingShapeProcedure.format = format;
					ChargeRingShapeProcedure.bufferBuilder = Tesselator.getInstance().getBuilder();
					ChargeRingShapeProcedure.bufferBuilder.begin(mode, DefaultVertexFormat.POSITION_TEX_COLOR);
					return true;
				}
			}
		}
		return false;
	}

	private static void clear() {
		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}
	}

	private static void end() {
		if (bufferBuilder == null || !bufferBuilder.building())
			return;
		if (vertexBuffer != null)
			vertexBuffer.close();
		vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
		vertexBuffer.bind();
		vertexBuffer.upload(bufferBuilder.end());
		VertexBuffer.unbind();
		bufferBuilder = null;
	}

	private static VertexBuffer shape() {
		return vertexBuffer;
	}

	public static VertexBuffer execute() {
		return execute(null);
	}

	private static VertexBuffer execute(@Nullable Event event) {
		// get charge data from player
		double chantCounter = 0;
		double maxChant = 130;
		String chantingType = "";
		if (Minecraft.getInstance().player != null) {
			chantCounter = Minecraft.getInstance().player.getPersistentData().getDouble("ChantCounter");
			chantingType = Minecraft.getInstance().player.getPersistentData().getString("chanting");
			// adjust max based on technique type
			if (chantingType.equals("blue") || chantingType.equals("dismantle")) {
				maxChant = 30;
			} else if (chantingType.equals("red")) {
				maxChant = 40;
			} else if (chantingType.equals("purple")) {
				maxChant = 130;
			}
		}
		// calculate fill percentage
		double fillPercentage = Math.min(chantCounter / maxChant, 1.0);
		if (begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR, (!Minecraft.getInstance().isPaused()))) {
			// ring parameters
			float outerRadius = 0.5f;
			float innerRadius = 0.4f;
			int segments = 64;
			// calculate segments to fill
			int fillSegments = (int) (segments * fillPercentage);
			// get color based on charge level
			int color = getChargeColor(chantCounter, chantingType);
			// create ring geometry
			for (int i = 0; i < fillSegments; i++) {
				float angle1 = (float) (i * 2.0 * Math.PI / segments - Math.PI / 2);
				float angle2 = (float) ((i + 1) * 2.0 * Math.PI / segments - Math.PI / 2);
				// outer vertices
				double x1_outer = Math.cos(angle1) * outerRadius;
				double y1_outer = Math.sin(angle1) * outerRadius;
				double x2_outer = Math.cos(angle2) * outerRadius;
				double y2_outer = Math.sin(angle2) * outerRadius;
				// inner vertices
				double x1_inner = Math.cos(angle1) * innerRadius;
				double y1_inner = Math.sin(angle1) * innerRadius;
				double x2_inner = Math.cos(angle2) * innerRadius;
				double y2_inner = Math.sin(angle2) * innerRadius;
				// first triangle
				add(x1_outer, y1_outer, 0, color);
				add(x1_inner, y1_inner, 0, color);
				add(x2_outer, y2_outer, 0, color);
				// second triangle
				add(x1_inner, y1_inner, 0, color);
				add(x2_inner, y2_inner, 0, color);
				add(x2_outer, y2_outer, 0, color);
			}
			end();
		}
		return shape();
	}

	// color based on charge level and technique
	private static int getChargeColor(double chantCounter, String chantingType) {
		if (chantingType.equals("blue")) {
			if (chantCounter >= 30) {
				return 255 << 24 | 0 << 16 | 100 << 8 | 255;
			} else if (chantCounter >= 20) {
				return 255 << 24 | 100 << 16 | 200 << 8 | 255;
			} else if (chantCounter >= 10) {
				return 255 << 24 | 150 << 16 | 150 << 8 | 150;
			}
		} else if (chantingType.equals("red")) {
			if (chantCounter >= 40) {
				return 255 << 24 | 255 << 16 | 0 << 8 | 0;
			} else if (chantCounter >= 20) {
				return 255 << 24 | 255 << 16 | 100 << 8 | 0;
			} else if (chantCounter >= 10) {
				return 255 << 24 | 150 << 16 | 150 << 8 | 150;
			}
		} else if (chantingType.equals("purple")) {
			if (chantCounter >= 130) {
				return 255 << 24 | 200 << 16 | 0 << 8 | 255;
			} else if (chantCounter >= 110) {
				return 255 << 24 | 180 << 16 | 50 << 8 | 255;
			} else if (chantCounter >= 90) {
				return 255 << 24 | 150 << 16 | 100 << 8 | 255;
			} else if (chantCounter >= 70) {
				return 255 << 24 | 120 << 16 | 120 << 8 | 200;
			}
		} else if (chantingType.equals("dismantle")) {
			if (chantCounter >= 30) {
				return 255 << 24 | 200 << 16 | 0 << 8 | 0;
			} else if (chantCounter >= 20) {
				return 255 << 24 | 255 << 16 | 100 << 8 | 0;
			} else if (chantCounter >= 10) {
				return 255 << 24 | 255 << 16 | 180 << 8 | 100;
			}
		}
		return 255 << 24 | 200 << 16 | 200 << 8 | 200;
	}
}
