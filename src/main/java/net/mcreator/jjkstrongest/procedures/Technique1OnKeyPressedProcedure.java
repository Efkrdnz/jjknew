package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;

public class Technique1OnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("sukuna")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_dismantle")) {
				RaycastDismantleAdvancedProcedure.execute(entity.level(), entity);
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_cleave")) {
				ReleaseArmAnimationProcedure.execute(entity);
				entity.getPersistentData().putString("chanting", "");
				CleaveHoldCancelProcedure.execute(entity);
			}
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("gojo")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_limitless")) {
				if (entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(JjkStrongestModMobEffects.INFINITY.get())) {
					if (entity instanceof LivingEntity _entity)
						_entity.removeEffect(JjkStrongestModMobEffects.INFINITY.get());
				} else {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.INFINITY.get(), 1200, 0, false, false));
				}
			}
		}
	}
}
