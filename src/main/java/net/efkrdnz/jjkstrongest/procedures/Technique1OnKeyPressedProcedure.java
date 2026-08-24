package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

public class Technique1OnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("sukuna")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_dismantle")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).precision) {
					RaycastDismantleAdvancedProcedure.execute(entity.level(), entity);
				} else {
					ShootDismantleTravelProcedure.execute(entity.level(), entity, ReturnOutputDismantleProcedure.execute(entity.level(), entity), entity.getPersistentData().getDouble("TechniquePower"), true);
				}
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_cleave")) {
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
				CleaveHoldCancelProcedure.execute(entity);
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_wcs")) {
				entity.getPersistentData().putString("chanting", "");
			}
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("gojo")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_limitless")) {
				if (entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(JjkStrongestModMobEffects.INFINITY.get())) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(JjkStrongestModMobEffects.INFINITY.get());
				} else {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.INFINITY.get(), 1200, 0, false, false));
				}
			}
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).contains("melee")) {
			MeleePunchProcedure.execute(entity.level(), entity);
			ReleaseArmAnimationProcedure.execute(entity);
			{
				boolean _setval = !(entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).left;
				entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.left = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
	}
}
