package net.efkrdnz.jjkvoice.client.hud;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import net.efkrdnz.jjkvoice.JjkVoiceMod;
import net.efkrdnz.jjkvoice.audio.VoicechatBridge;
import net.efkrdnz.jjkvoice.config.VoiceConfig;

/**
 * Draws the incantation lines the player can say next.
 *
 * <p>Follows the host mod's own overlay pattern rather than inventing one:
 * client-scoped subscriber on {@code RenderGuiEvent.Pre}, drawing through the
 * event's {@code GuiGraphics}.
 */
@EventBusSubscriber(modid = JjkVoiceMod.MOD_ID, value = Dist.CLIENT)
public final class ChantHud {
	private static final int LINE_HEIGHT = 11;
	private static final int LABEL_GAP = 10;
	private static final int PAD = 4;

	private static final int DIM = 0xFF5F5E5A;
	private static final int BACKDROP = 0x70000000;

	private ChantHud() {
	}

	@SubscribeEvent
	public static void onRenderGui(RenderGuiEvent.Pre event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui)
			return;
		VoiceConfig config = VoiceConfig.get();
		if (!config.hudEnabled)
			return;
		// Nothing on it could be acted on without the microphone path, so it goes
		// away rather than sitting there as decoration.
		if (!VoicechatBridge.isClientReady())
			return;

		List<ChantRow> rows = ChantRow.current();
		if (rows.isEmpty())
			return;

		GuiGraphics graphics = event.getGuiGraphics();
		int[] origin = anchor(graphics, rows, config);
		draw(graphics, minecraft.font, rows, origin[0], origin[1]);
	}

	/**
	 * Where the block sits, from fractions of the screen.
	 *
	 * <p>Fractions rather than pixels so a resolution or GUI-scale change leaves it
	 * where it was put, and clamped so a stored position from a wider screen cannot
	 * strand it off the edge of a narrower one.
	 */
	static int[] anchor(GuiGraphics graphics, List<ChantRow> rows, VoiceConfig config) {
		int width = widthOf(Minecraft.getInstance().font, rows);
		int height = heightOf(rows);
		int x = (int) Math.round(config.hudX * graphics.guiWidth());
		int y = (int) Math.round(config.hudY * graphics.guiHeight());
		x = Math.max(0, Math.min(x, graphics.guiWidth() - width));
		y = Math.max(0, Math.min(y, graphics.guiHeight() - height));
		return new int[] {x, y};
	}

	static int widthOf(Font font, List<ChantRow> rows) {
		int widest = 0;
		for (ChantRow row : rows)
			widest = Math.max(widest, font.width(row.line()) + LABEL_GAP + font.width(row.label()));
		return widest + PAD * 2;
	}

	static int heightOf(List<ChantRow> rows) {
		return rows.size() * LINE_HEIGHT + (committed(rows) ? LINE_HEIGHT : 0) + PAD * 2;
	}

	/**
	 * Whether one ability has won, which is when pips are worth drawing.
	 *
	 * <p>Not simply one row: a Dismantle recited through shows three, one per shape
	 * it can be thrown in, and they are all the same ability.
	 */
	private static boolean committed(List<ChantRow> rows) {
		if (rows.isEmpty())
			return false;
		String first = rows.get(0).ability();
		for (ChantRow row : rows)
			if (!row.ability().equals(first))
				return false;
		return true;
	}

	static void draw(GuiGraphics graphics, Font font, List<ChantRow> rows, int x, int y) {
		int width = widthOf(font, rows);
		graphics.fill(x, y, x + width, y + heightOf(rows), BACKDROP);

		int textX = x + PAD;
		int textY = y + PAD;
		for (ChantRow row : rows) {
			// An un-enrolled line cannot match however clearly it is said, so it is
			// shown as unavailable rather than advertised.
			int colour = row.enrolled() ? row.colour() : DIM;
			graphics.drawString(font, row.line(), textX, textY, colour, false);
			if (!row.enrolled())
				graphics.fill(textX, textY + 4, textX + font.width(row.line()), textY + 5, DIM);

			int labelX = x + width - PAD - font.width(row.label());
			graphics.drawString(font, row.label(), labelX, textY, DIM, false);
			textY += LINE_HEIGHT;
		}

		if (committed(rows))
			drawPips(graphics, rows.get(0), textX, textY);
	}

	/**
	 * One pip per line of the incantation, filled for the ones already said.
	 *
	 * <p>Drawn rather than written. The obvious characters for this are outside
	 * Minecraft's default font and would come out as missing-glyph boxes.
	 */
	private static void drawPips(GuiGraphics graphics, ChantRow row, int x, int y) {
		int size = 3;
		int gap = 2;
		int top = y + 2;
		for (int i = 0; i < row.total(); i++) {
			int left = x + i * (size + gap);
			graphics.fill(left, top, left + size, top + size, i < row.recited() ? row.colour() : DIM);
		}
	}
}
