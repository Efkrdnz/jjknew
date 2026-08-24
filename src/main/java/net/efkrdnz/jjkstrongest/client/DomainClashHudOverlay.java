package net.efkrdnz.jjkstrongest.client;


import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.client.DomainClashHudOverlay;

@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public class DomainClashHudOverlay {
	private static final int BAR_WIDTH = 160;
	private static final int BAR_HEIGHT = 10;
	private static final int BAR_PADDING = 3;
	// uv blue-white palette
	private static final int UV_BAR_COLOR = 0xFF8FC9FF;
	private static final int UV_BAR_BG = 0xFF1A2A3A;
	private static final int UV_BORDER_COLOR = 0xFF4A90D9;
	// shrine red palette
	private static final int SHRINE_BAR_COLOR = 0xFFFF3A3A;
	private static final int SHRINE_BAR_BG = 0xFF3A1A1A;
	private static final int SHRINE_BORDER_COLOR = 0xFFBB2222;
	// shared ui colors
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int PANEL_BG = 0xAA000000;
	private static final float MAX_CLASH_HP = 100f;

	@SubscribeEvent
	public static void onRenderHud(RenderGuiEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		// only render during clash
		Vec3 playerPos = mc.player.position();
		AABB searchBox = AABB.ofSize(playerPos, 300, 300, 300);
		// find nearby clashing uv
		DomainUVEntity uvDomain = null;
		for (DomainUVEntity uv : mc.level.getEntitiesOfClass(DomainUVEntity.class, searchBox, e -> e.isAlive())) {
			if (uv.getPersistentData().getBoolean("isClashing")) {
				uvDomain = uv;
				break;
			}
		}
		// find nearby clashing shrine
		MalevolentShrineEntity shrine = null;
		for (MalevolentShrineEntity s : mc.level.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> e.isAlive())) {
			if (s.getPersistentData().getBoolean("isClashing")) {
				shrine = s;
				break;
			}
		}
		// only draw if both are clashing
		if (uvDomain == null || shrine == null)
			return;
		float uvHP = uvDomain.getPersistentData().getFloat("uvClashHP");
		float shrineHP = shrine.getPersistentData().getFloat("shrineClashHP");
		float uvPct = Math.max(0f, Math.min(1f, uvHP / MAX_CLASH_HP));
		float shrinePct = Math.max(0f, Math.min(1f, shrineHP / MAX_CLASH_HP));
		GuiGraphics gui = event.getGuiGraphics();
		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();
		// center horizontally, position near top of screen
		int panelW = BAR_WIDTH + BAR_PADDING * 2 + 2;
		int panelH = (BAR_HEIGHT + BAR_PADDING) * 2 + BAR_PADDING + 20; // 20 = label row
		int panelX = (screenW - panelW * 2 - 16) / 2;
		int panelY = 12;
		// uv panel (left)
		renderDomainPanel(gui, panelX, panelY, panelW, panelH, uvPct, UV_BAR_COLOR, UV_BAR_BG, UV_BORDER_COLOR, "UNLIMITED VOID", (int) (uvPct * 100) + "%", false);
		// vs label in the middle
		int vsX = panelX + panelW + 4;
		int vsY = panelY + panelH / 2 - 4;
		gui.drawString(mc.font, "VS", vsX + 1, vsY, 0xAA888888, false);
		gui.drawString(mc.font, "VS", vsX, vsY, 0xFFFFFFFF, false);
		// shrine panel (right)
		int shrineX = vsX + 14;
		renderDomainPanel(gui, shrineX, panelY, panelW, panelH, shrinePct, SHRINE_BAR_COLOR, SHRINE_BAR_BG, SHRINE_BORDER_COLOR, "MALEVOLENT SHRINE", (int) (shrinePct * 100) + "%", true);
	}

	private static void renderDomainPanel(GuiGraphics gui, int x, int y, int panelW, int panelH, float pct, int barColor, int barBg, int borderColor, String label, String pctText, boolean flipBar) {
		Minecraft mc = Minecraft.getInstance();
		// panel background
		gui.fill(x, y, x + panelW, y + panelH, PANEL_BG);
		// panel border
		gui.hLine(x, x + panelW - 1, y, borderColor);
		gui.hLine(x, x + panelW - 1, y + panelH - 1, borderColor);
		gui.vLine(x, y, y + panelH - 1, borderColor);
		gui.vLine(x + panelW - 1, y, y + panelH - 1, borderColor);
		// label row
		int labelY = y + BAR_PADDING;
		// small colored dot
		gui.fill(x + BAR_PADDING, labelY + 2, x + BAR_PADDING + 5, labelY + 7, borderColor);
		gui.drawString(mc.font, label, x + BAR_PADDING + 8, labelY, TEXT_COLOR, false);
		// bar background
		int barX = x + BAR_PADDING;
		int barY = labelY + 12;
		gui.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, barBg);
		// filled bar — shrine bar fills right-to-left, uv fills left-to-right
		int fillW = (int) (BAR_WIDTH * pct);
		if (fillW > 0) {
			if (flipBar) {
				// right to left fill for shrine
				gui.fill(barX + BAR_WIDTH - fillW, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, barColor);
			} else {
				gui.fill(barX, barY, barX + fillW, barY + BAR_HEIGHT, barColor);
			}
		}
		// bar border
		gui.hLine(barX - 1, barX + BAR_WIDTH, barY - 1, borderColor);
		gui.hLine(barX - 1, barX + BAR_WIDTH, barY + BAR_HEIGHT, borderColor);
		gui.vLine(barX - 1, barY - 1, barY + BAR_HEIGHT, borderColor);
		gui.vLine(barX + BAR_WIDTH, barY - 1, barY + BAR_HEIGHT, borderColor);
		// pulsing low-hp tint when below 25%
		if (pct < 0.25f) {
			long time = System.currentTimeMillis();
			float pulse = (float) (Math.sin(time / 200.0) * 0.5 + 0.5);
			int alpha = (int) (pulse * 60);
			gui.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, (alpha << 24) | 0xFF3300);
		}
		// percentage text centered on bar
		int textW = mc.font.width(pctText);
		int textX = barX + (BAR_WIDTH - textW) / 2;
		int textY = barY + (BAR_HEIGHT - 8) / 2;
		// shadow
		gui.drawString(mc.font, pctText, textX + 1, textY + 1, 0x88000000, false);
		gui.drawString(mc.font, pctText, textX, textY, TEXT_COLOR, false);
	}
}
