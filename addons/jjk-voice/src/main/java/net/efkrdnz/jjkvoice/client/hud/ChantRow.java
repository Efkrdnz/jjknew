package net.efkrdnz.jjkvoice.client.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.efkrdnz.jjkvoice.compat.JjkBridge;
import net.efkrdnz.jjkvoice.config.VoiceConfig;
import net.efkrdnz.jjkvoice.recognize.VoicePrintStore;

/**
 * One line of the overlay: what to say next for one ability.
 *
 * <p>Deciding what the rows are is kept away from drawing them, because the
 * decision is the part with rules in it -- whose technique this is, how far the
 * recital has got, whether the phrase was ever enrolled -- and it can be reasoned
 * about without reading render code.
 *
 * @param ability  the moveset this line charges
 * @param label    what to show as the ability's name
 * @param line     the phrase to say next
 * @param colour   packed ARGB, the ability's own
 * @param enrolled whether a voiceprint exists; if not, saying it cannot work
 * @param recited  lines of this incantation already taken
 * @param total    lines it has altogether
 */
@OnlyIn(Dist.CLIENT)
public record ChantRow(String ability, String label, String line, int colour, boolean enrolled,
		int recited, int total) {

	/** Ability colours. Dismantle is deliberately not a second red. */
	private static int colourOf(String ability) {
		return switch (ability) {
			case "gojo_blue" -> 0xFF85B7EB;
			case "gojo_red" -> 0xFFF09595;
			case "gojo_purple" -> 0xFFAFA9EC;
			case "sukuna_dismantle" -> 0xFFF0997B;
			default -> 0xFFD3D1C7;
		};
	}

	private static String labelOf(String ability) {
		return switch (ability) {
			case "gojo_blue" -> "Blue";
			case "gojo_red" -> "Red";
			case "gojo_purple" -> "Purple";
			case "sukuna_dismantle" -> "Dismantle";
			default -> ability;
		};
	}

	/**
	 * What the overlay should show right now.
	 *
	 * <p>With a recital running, only the abilities it could still be for and only
	 * the line that would come next. With none, every chantable ability the
	 * player's technique includes, showing its opening.
	 */
	public static List<ChantRow> current() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null)
			return List.of();

		VoiceConfig config = VoiceConfig.get();
		List<String> abilities = ChantHudState.running()
				? ChantHudState.candidates()
				: new ArrayList<>(JjkBridge.chantableMovesets());
		int recited = ChantHudState.running() ? ChantHudState.recited() : 0;

		List<ChantRow> rows = new ArrayList<>();
		for (String ability : abilities) {
			// Narrowed here as well as on the server: this is what stops another
			// sorcerer's techniques being advertised to someone who cannot use them.
			if (!JjkBridge.owns(minecraft.player, ability))
				continue;
			List<String> lines = config.incantationsFor(ability);
			// A candidate the config has no lines for would draw a blank row. That
			// happens when the server knows an ability this config was edited to drop.
			if (lines.isEmpty())
				continue;
			// Recited to the end: there is no next line, so show the word that throws
			// it instead. The pips being full is what says it is charged.
			String next = recited < lines.size() ? lines.get(recited) : firingWord(config, ability);
			if (next.isEmpty())
				continue;
			rows.add(new ChantRow(ability, labelOf(ability), next, colourOf(ability),
					VoicePrintStore.isEnrolled(next), recited, lines.size()));
		}
		return rows;
	}

	/** What the player says to throw a charged ability, for the finished row. */
	private static String firingWord(VoiceConfig config, String ability) {
		List<String> phrases = config.phrasesFor(ability);
		if (!phrases.isEmpty())
			return phrases.get(0);
		// Dismantle is thrown by naming a shape rather than the ability.
		List<String> dismantle = config.phrasesFor("dismantle");
		return "sukuna_dismantle".equals(ability) && !dismantle.isEmpty() ? dismantle.get(0) : "";
	}

	/** A stand-in mid-recital row, so the editor positions something real-looking. */
	public static List<ChantRow> preview() {
		return List.of(
				new ChantRow("gojo_blue", "Blue", "twilight", colourOf("gojo_blue"), true, 1, 3),
				new ChantRow("gojo_red", "Red", "paramita", colourOf("gojo_red"), true, 1, 3));
	}
}
