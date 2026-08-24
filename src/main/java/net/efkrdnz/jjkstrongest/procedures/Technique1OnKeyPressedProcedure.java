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
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("sukuna")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_dismantle")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).precision) {
					RaycastDismantleAdvancedProcedure.execute(entity.level(), entity);
				} else {
					ShootDismantleTravelProcedure.execute(entity.level(), entity, ReturnOutputDismantleProcedure.execute(entity.level(), entity), entity.getPersistentData().getDouble("TechniquePower"), true);
				}
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_cleave")) {
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
				CleaveHoldCancelProcedure.execute(entity);
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
				entity.getPersistentData().putString("chanting", "");
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("gojo")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_limitless")) {
				if (entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(JjkStrongestModMobEffects.INFINITY)) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(JjkStrongestModMobEffects.INFINITY);
				} else {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.INFINITY, 1200, 0, false, false));
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).contains("melee")) {
			MeleePunchProcedure.execute(entity.level(), entity);
			ReleaseArmAnimationProcedure.execute(entity);
			{
				boolean _setval = !(entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).left;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.left = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
		}
	}
}
