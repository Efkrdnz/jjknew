package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.CommandSourceStack;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;

public class BindingVowRecognizeProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		String message = "";
		String sorcerer = "";
		message = ((new Object() {
			public String getMessage() {
				try {
					return MessageArgument.getMessage(arguments, "vow").getString();
				} catch (CommandSyntaxException ignored) {
					return "";
				}
			}
		}).getMessage()).toUpperCase();
		sorcerer = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer;
		// check if message starts with proper declaration
		if (!(message.contains("I NEED") || message.contains("I WANT") || message.contains("I SEEK") || message.contains("I VOW"))) {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("§cDeclare your vow! (Use: I need/want/seek...)"), false);
			return;
		}
		// overtime vow
		if (message.contains("OVERTIME") || (message.contains("WORK") && message.contains("NIGHT"))) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_overtime) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_overtime = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§6§l⚖ OVERTIME §r§7- Weakened by day, stronger at night (+50%)"), false);
				}
			}
		}
		// recoil vow
		else if ((message.contains("MORE") || message.contains("BETTER") || message.contains("STRONG") || message.contains("GREATER"))
				&& (message.contains("OUTPUT") || message.contains("IMPACT") || message.contains("POWER") || message.contains("DAMAGE"))) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_recoil) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_recoil = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ RECOIL §r§7- Take recoil damage, +30% output"), false);
				}
			}
		}
		// heal brain/burnout vow
		else if ((message.contains("HEAL") || message.contains("RESTORE") || message.contains("RECOVER") || message.contains("FIX")) && (message.contains("BRAIN") || message.contains("BURNOUT") || message.contains("MIND"))) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_RTChealbrain) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_RTChealbrain = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§b§l⚖ HEAL BURNOUT §r§7- RCT -30%, can heal burnout"), false);
				}
			}
		}
		// sukuna - executioner vow
		if ((message.contains("EXECUTE") || message.contains("FINISH") || message.contains("KILL")) && (message.contains("WEAK") || message.contains("WOUNDED") || message.contains("LOW"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_executioner) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_executioner = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ EXECUTIONER §r§7- Weaker vs healthy, stronger vs wounded (<50% HP)"), false);
				}
			}
		}
		// sukuna - true form vow (PERMANENT)
		else if ((message.contains("TRUE") || message.contains("ORIGINAL") || message.contains("HEIAN")) && (message.contains("FORM") || message.contains("BODY") || message.contains("POWER"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_trueform) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_trueform = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§4§l⚖ TRUE FORM §r§7[PERMANENT] - Unlocks transformation (needs BF + RCT)"), false);
				}
			}
		}
		// sukuna - adaptive cleave vow
		else if ((message.contains("ADAPT") || message.contains("LEARN") || message.contains("REMEMBER")) && (message.contains("CLEAVE") || message.contains("TARGET") || message.contains("ENEMY"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_adaptivecleave) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_adaptivecleave = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ ADAPTIVE CLEAVE §r§7- Weaker first hit, stronger on repeated targets"), false);
				}
			}
		}
		// sukuna - shredding barrage vow
		else if ((message.contains("MORE") || message.contains("MANY") || message.contains("MULTIPLE")) && (message.contains("SLASH") || message.contains("CUT") || message.contains("BARRAGE"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_shreddingbarrage) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_shreddingbarrage = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ SHREDDING BARRAGE §r§7- Less damage/slash, more slashes + stun"), false);
				}
			}
		}
		// sukuna - blade resonance vow
		else if ((message.contains("BLADE") || message.contains("SWORD") || message.contains("WEAPON")) && (message.contains("RESONATE") || message.contains("CHANNEL") || message.contains("IMBUE"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_bladeresonance) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_dismantleimbue = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ BLADE RESONANCE §r§7- Lower output w/o sword, higher with sword"), false);
				}
			}
		}
		// sukuna - world cutting slash vow (PERMANENT)
		else if ((message.contains("TARGET") || message.contains("CUT") || message.contains("SLASH") || message.contains("REACH")) && (message.contains("WORLD") || message.contains("SPACE") || message.contains("REALITY"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_wcsact) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_wcsact = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§4§l⚖ WORLD SLASH §r§7[PERMANENT] - Unlocks WCS with chanting (longer cast)"), false);
				}
			}
		}
		// sukuna - smoldering buildup vow
		else if ((message.contains("BUILD") || message.contains("CHARGE") || message.contains("ACCUMULATE")) && (message.contains("FIRE") || message.contains("FLAME") || message.contains("FURNACE"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_smolderingbuildup) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_smolderingbuildup = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ SMOLDERING BUILDUP §r§7- No cooldown, charges over time (20-100%+)"), false);
				}
			}
		}
		// sukuna - concentrated inferno vow
		else if ((message.contains("CONCENTRATE") || message.contains("FOCUS") || message.contains("DIRECT")) && (message.contains("FIRE") || message.contains("FLAME") || message.contains("BURN"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_concentratedinferno) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_concentratedinferno = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ CONCENTRATED INFERNO §r§7- No explosion, higher direct damage"), false);
				}
			}
		}
		// sukuna - desperate flames vow
		else if ((message.contains("DESPERATE") || message.contains("OUTNUMBER") || message.contains("CORNERED")) && (message.contains("FIRE") || message.contains("FLAME") || message.contains("FURNACE"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_desperateflames) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_desperateflames = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ DESPERATE FLAMES §r§7- Shorter chant when outnumbered, much less CE cost"), false);
				}
			}
		}
		// sukuna - pyroclasm vow
		else if ((message.contains("BURN") || message.contains("FIRE") || message.contains("FLAME")) && (message.contains("FUEL") || message.contains("EMPOWER") || message.contains("STRENGTHEN"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_pyroclasm) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_pyroclasm = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§c§l⚖ PYROCLASM §r§7- -15% when not burning, +10% while burning"), false);
				}
			}
		}
		// sukuna - meditative opening vow
		else if ((message.contains("MEDITATE") || message.contains("CALM") || message.contains("PREPARE")) && (message.contains("DOMAIN") || message.contains("SHRINE") || message.contains("EXPANSION"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_meditativeopening) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_meditativeopening = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§5§l⚖ MEDITATIVE OPENING §r§7- Faster domain if idle 10s, slower if just used ability"), false);
				}
			}
		}
		// sukuna - empty hand shrine vow
		else if ((message.contains("EMPTY") || message.contains("BARE") || message.contains("UNARMED")) && (message.contains("HAND") || message.contains("DOMAIN") || message.contains("SHRINE"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_emptyhandshrine) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_emptyhandshrine = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§5§l⚖ EMPTY HAND SHRINE §r§7- Weaker domain with items, stronger with empty hands"), false);
				}
			}
		}
		// sukuna - overwhelming malevolence vow
		else if ((message.contains("OVERWHELM") || message.contains("BURST") || message.contains("INITIAL")) && (message.contains("DOMAIN") || message.contains("SHRINE") || message.contains("MALEVOLENT"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_overwhelmingmalevolence) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_overwhelmingmalevolence = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§5§l⚖ OVERWHELMING MALEVOLENCE §r§7- HUGE damage first 5s, decreases after"), false);
				}
			}
		}
		// sukuna - shrine supremacy vow
		else if ((message.contains("SHRINE") || message.contains("DOMAIN")) && (message.contains("SUPREMACY") || message.contains("DOMINANCE") || message.contains("CONTROL"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_shrinesupremacy) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_shrinesupremacy = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§5§l⚖ SHRINE SUPREMACY §r§7- Take more damage in domains, deal more in your domain"), false);
				}
			}
		}
		// sukuna - overextended malice vow
		else if ((message.contains("EXTEND") || message.contains("PROLONG") || message.contains("LONGER")) && (message.contains("DOMAIN") || message.contains("SHRINE") || message.contains("MALEVOLENT"))) {
			if (!sorcerer.equals("sukuna")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Sukuna only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_overextendedmalice) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_overextendedmalice = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§5§l⚖ OVEREXTENDED MALICE §r§7- Take DoT/burnout, domain lasts much longer"), false);
				}
			}
		}
		// gojo - distance amplification vow
		else if ((message.contains("DISTANCE") || message.contains("FAR") || message.contains("RANGE")) && (message.contains("PURPLE") || message.contains("IMAGINARY") || message.contains("VOID"))) {
			if (!sorcerer.equals("gojo")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Gojo only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_distanceamplification) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_distanceamplification = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§5§l⚖ DISTANCE AMPLIFICATION §r§7- Purple damage scales with distance"), false);
				}
			}
		}
		// gojo - stationary perfection vow
		else if ((message.contains("STILL") || message.contains("STATIONARY") || message.contains("MOTIONLESS")) && (message.contains("INFINITY") || message.contains("BARRIER") || message.contains("LIMITLESS"))) {
			if (!sorcerer.equals("gojo")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Gojo only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_stationaryperfection) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_stationaryperfection = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§b§l⚖ STATIONARY PERFECTION §r§7- Stronger Infinity when still, weaker when moving"), false);
				}
			}
		}
		// gojo - calculated warp vow
		else if ((message.contains("CALCULATE") || message.contains("PRECISE") || message.contains("EXTENDED")) && (message.contains("TELEPORT") || message.contains("WARP") || message.contains("BLUE"))) {
			if (!sorcerer.equals("gojo")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Gojo only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_calculatedwarp) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_calculatedwarp = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§b§l⚖ CALCULATED WARP §r§7- 1.5s charge, teleport 2x distance"), false);
				}
			}
		}
		// universal - overworld dominance vow
		else if ((message.contains("OVERWORLD") || message.contains("SURFACE") || message.contains("WORLD")) && (message.contains("DOMINANCE") || message.contains("POWER") || message.contains("STRONGER"))) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_overworlddominance) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_overworlddominance = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§a§l⚖ OVERWORLD DOMINANCE §r§7- Stronger in Overworld, weaker in Nether/End"), false);
				}
			}
		}
		// universal - unencumbered focus vow
		else if ((message.contains("UNENCUMBERED") || message.contains("FREE") || message.contains("EMPTY")) && (message.contains("HAND") || message.contains("OFFHAND") || message.contains("FOCUS"))) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_unencumberedfocus) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_unencumberedfocus = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§a§l⚖ UNENCUMBERED FOCUS §r§7- Higher output with empty offhand"), false);
				}
			}
		}
		// universal - pressured excellence vow (gojo only)
		else if ((message.contains("PRESSURE") || message.contains("OUTNUMBER") || message.contains("MULTIPLE")) && (message.contains("ENEMY") || message.contains("ENEMIES") || message.contains("FOE"))) {
			if (!sorcerer.equals("gojo")) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§c[Gojo only]"), false);
				return;
			}
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_pressuredexcellence) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_pressuredexcellence = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§b§l⚖ PRESSURED EXCELLENCE §r§7- Stronger vs multiple enemies, weaker solo"), false);
				}
			}
		}
		// universal - fair weather fighter vow
		else if ((message.contains("WEATHER") || message.contains("CLEAR") || message.contains("SUN")) && (message.contains("FIGHT") || message.contains("BATTLE") || message.contains("POWER"))) {
			if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_fairweatherfighter) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("§eYou already have this vow."), false);
			} else {
				{
					boolean _setval = true;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.vow_fairweatherfighter = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.displayClientMessage(Component.literal("§e§l⚖ FAIR WEATHER FIGHTER §r§7- Stronger in clear weather, weaker in rain"), false);
				}
			}
		}
		// no matching vow found
		else {
			if (entity instanceof Player _player && !_player.level().isClientSide()) {
				_player.displayClientMessage(Component.literal("§cNo vow matches your declaration."), false);
			}
		}
	}
}
