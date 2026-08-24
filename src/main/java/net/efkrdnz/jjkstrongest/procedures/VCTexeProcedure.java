package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class VCTexeProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, String key) {
		if (entity == null || key == null)
			return;
		String message = "";
		message = key;
		if (!(entity instanceof Player)) {
			return;
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("gojo")) {
			if (message.contains("domain_expansion")) {
				{
					double _setval = 1;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.domain_image_1 = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				{
					double _setval = 1;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.domain_image_2 = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (!world.isClientSide()) {
					DomainExpansionStartProcedure.execute(entity.level(), Math.round(x + 0), Math.round(y + 0), Math.round(z + 0), entity, 0);
				}
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("sukuna")) {
			if (message.contains("domain_expansion")) {
				{
					double _setval = 1;
					{
						JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
						capability.domain_image_2 = _setval;
						capability.syncPlayerVariables(entity);
					}
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:sukuna_domain_act")), SoundSource.PLAYERS, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:sukuna_domain_act")), SoundSource.PLAYERS, 1, 1, false);
					}
				}
				MalevolentShrineSummonProcedure.execute(entity.level(), entity);
				TriggerScreenShakeProcedure.execute((Level) world, entity, 5, 3.0f);
			} else if (message.contains("dismantle")) {
				entity.getPersistentData().putDouble("TechniquePower", 2);
				RaycastDismantleAdvancedProcedure.execute(entity.level(), entity);
			} else if (message.contains("fuga")) {
				FlameArrowShootExecuteProcedure.execute(entity);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("inumaki")) {
			if (message.contains("dont_move")) {
				SpeechExecuteStopProcedure.execute(entity);
			} else if (message.contains("die")) {
				SpeechExecuteDieProcedure.execute(entity);
			} else if (message.contains("blast") || message.contains("get_blown_away")) {
				SpeechExecuteBlastProcedure.execute(entity);
			} else if (message.contains("crush")) {
				SpeechExecuteCrushProcedure.execute(entity);
			} else if (message.contains("burst") || message.contains("explode")) {
				SpeechExecuteBurstProcedure.execute(entity);
			} else if (message.contains("sleep") || message.contains("fall_asleep")) {
				SpeechExecuteSleepProcedure.execute(entity);
			} else if (message.contains("flee") || message.contains("run_away")) {
				SpeechExecuteFleeProcedure.execute(entity);
			} else if (message.contains("rot") || message.contains("decay")) {
				SpeechExecuteRotProcedure.execute(entity);
			} else if (message.contains("twist")) {
				SpeechExecuteTwistProcedure.execute(entity);
			} else if (message.contains("burn") || message.contains("be_on_fire")) {
				SpeechExecuteBurnProcedure.execute(entity);
			} else if (message.contains("fall") || message.contains("soar")) {
				SpeechExecuteFallProcedure.execute(entity);
			} else if (message.contains("spit") || message.contains("vomit")) {
				SpeechExecuteSpitProcedure.execute(entity);
			} else if (message.contains("pull") || message.contains("come_here")) {
				SpeechExecutePullProcedure.execute(entity);
			} else if (message.contains("shrink") || message.contains("be_crushed")) {
				SpeechExecuteShrinkProcedure.execute(entity);
			} else if (message.contains("weep") || message.contains("cry")) {
				SpeechExecuteWeepProcedure.execute(entity);
			} else if (message.contains("kneel") || message.contains("bow")) {
				SpeechExecuteKneelProcedure.execute(entity);
			}
		}
		ReleaseArmAnimationProcedure.execute(entity);
	}
}
