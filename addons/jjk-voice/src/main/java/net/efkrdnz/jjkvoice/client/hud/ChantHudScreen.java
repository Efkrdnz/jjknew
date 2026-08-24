package net.efkrdnz.jjkvoice.client.hud;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.efkrdnz.jjkvoice.config.VoiceConfig;

/**
 * Drag the overlay where you want it.
 *
 * <p>Shows a mid-recital block rather than an empty one, so what gets positioned
 * is the shape that will actually be on screen. Saves on close; nothing is sent
 * anywhere, the position being the client's own business.
 */
@OnlyIn(Dist.CLIENT)
public final class ChantHudScreen extends Screen {
	/** How near an edge or centre line counts as meaning it. */
	private static final int SNAP = 6;

	private static final int GUIDE = 0x66FFFFFF;

	private final List<ChantRow> rows = ChantRow.preview();

	private int x;
	private int y;
	private boolean dragging;
	private int grabX;
	private int grabY;

	public ChantHudScreen() {
		super(Component.translatable("screen.jjkvoice.hud"));
	}

	@Override
	protected void init() {
		VoiceConfig config = VoiceConfig.get();
		x = (int) Math.round(config.hudX * width);
		y = (int) Math.round(config.hudY * height);
		clamp();
	}

	private int blockWidth() {
		return ChantHud.widthOf(font, rows);
	}

	private int blockHeight() {
		return ChantHud.heightOf(rows);
	}

	private void clamp() {
		x = Math.max(0, Math.min(x, width - blockWidth()));
		y = Math.max(0, Math.min(y, height - blockHeight()));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		if (dragging) {
			x = mouseX - grabX;
			y = mouseY - grabY;
			snap();
			clamp();
		}

		drawGuides(graphics);
		ChantHud.draw(graphics, font, rows, x, y);
		graphics.drawCenteredString(font, Component.translatable("screen.jjkvoice.hud.hint")
				.withStyle(ChatFormatting.GRAY), width / 2, height - 24, 0xFFFFFFFF);
	}

	/** Pulls the block onto an edge or a centre line when it is nearly on one. */
	private void snap() {
		int right = width - blockWidth();
		int bottom = height - blockHeight();
		int centreX = right / 2;
		int centreY = bottom / 2;
		if (Math.abs(x) <= SNAP)
			x = 0;
		else if (Math.abs(x - right) <= SNAP)
			x = right;
		else if (Math.abs(x - centreX) <= SNAP)
			x = centreX;
		if (Math.abs(y) <= SNAP)
			y = 0;
		else if (Math.abs(y - bottom) <= SNAP)
			y = bottom;
		else if (Math.abs(y - centreY) <= SNAP)
			y = centreY;
	}

	private void drawGuides(GuiGraphics graphics) {
		if (!dragging)
			return;
		graphics.fill(width / 2, 0, width / 2 + 1, height, GUIDE);
		graphics.fill(0, height / 2, width, height / 2 + 1, GUIDE);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && mouseX >= x && mouseX <= x + blockWidth()
				&& mouseY >= y && mouseY <= y + blockHeight()) {
			dragging = true;
			grabX = (int) mouseX - x;
			grabY = (int) mouseY - y;
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void onClose() {
		VoiceConfig config = VoiceConfig.get();
		// Stored as fractions, so the position survives a resolution or GUI-scale
		// change instead of being measured against a screen that no longer exists.
		config.hudX = width <= 0 ? 0.0D : (double) x / width;
		config.hudY = height <= 0 ? 0.0D : (double) y / height;
		config.save();
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
