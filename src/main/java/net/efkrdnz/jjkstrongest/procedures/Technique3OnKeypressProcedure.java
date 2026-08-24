package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

public class Technique3OnKeypressProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		double x = 0;
		double z = 0;
		double yaw = 0;
		double wcs_pwer = 0;
		entity.getPersistentData().putDouble("TechniquePower", 1);
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("gojo")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_blue")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_blue >= 1) {
					entity.getPersistentData().putString("chanting", "blue");
					PlayArmAnimationProcedure.execute(entity, "blue_charge", true);
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_red")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red >= 1) {
					entity.getPersistentData().putString("chanting", "red");
					PlayArmAnimationProcedure.execute(entity, "red_charge", true);
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_purple")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_purple >= 3) {
					x = entity.getX();
					z = entity.getZ();
					yaw = entity.getYRot();
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.PURPLE_CHARGING, 50, 1, false, false));
					entity.getPersistentData().putString("chanting", "purple");
					PlayArmAnimationProcedure.execute(entity, "hollow_purple", true);
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_limitless")) {
				entity.getPersistentData().putString("chanting", "teleport");
				PlayArmAnimationProcedure.execute(entity, "gojo_tp", true);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("sukuna")) {
			if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_dismantle")) {
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).precision) {
					{
						double _setval = entity.getX() + 25 * entity.getLookAngle().x;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_x1 = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					{
						double _setval = entity.getY() + entity.getBbHeight() + 25 * entity.getLookAngle().y;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_y1 = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					{
						double _setval = entity.getZ() + 25 * entity.getLookAngle().z;
						{
							JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
							capability.wcs_z1 = _setval;
							capability.syncPlayerVariables(entity);
						}
					}
					PlayArmAnimationProcedure.execute(entity, "dismantle", true);
				} else {
					entity.getPersistentData().putString("chanting", "dis_net");
					PlayArmAnimationProcedure.execute(entity, "dismantle", true);
				}
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_fuga")) {
				entity.getPersistentData().putString("chanting", "flame_arrow");
				PlayArmAnimationProcedure.execute(entity, "fuga_hold", true);
			} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
				entity.getPersistentData().putString("chanting", "wcs3");
			}
		}
	}
}
