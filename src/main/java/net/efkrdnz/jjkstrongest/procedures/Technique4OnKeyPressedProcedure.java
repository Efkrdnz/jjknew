package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

public class Technique4OnKeyPressedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		double x = 0;
		double z = 0;
		double yaw = 0;
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).contains("melee")) {
			{
				boolean _setval = true;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.block = _setval;
					// Only sync from server
					if (!entity.level().isClientSide()) {
						capability.syncPlayerVariables(entity);
					}
				}
			}
		}
		if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_wcs")) {
			{
				double _setval = entity.getX() + 125 * entity.getLookAngle().x;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_x1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = entity.getY() + entity.getBbHeight() + 125 * entity.getLookAngle().y;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_y1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			{
				double _setval = entity.getZ() + 125 * entity.getLookAngle().z;
				{
					JjkStrongestModVariables.PlayerVariables capability = entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
					capability.wcs_z1 = _setval;
					capability.syncPlayerVariables(entity);
				}
			}
			PlayArmAnimationProcedure.execute(entity, "domain_sukuna", true);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_red")) {
			if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).charge_red > 0) {
				entity.getPersistentData().putString("chanting", "red");
				PlayArmAnimationProcedure.execute(entity, "red_charge", true);
			}
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_limitless")) {
			PlayArmAnimationProcedure.execute(entity, "domain_gojo", true);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_blue")) {
			BlueVortexProcedure.start(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("gojo_purple")) {
			entity.getPersistentData().putString("chanting", "imaginary_purple");
			PlayArmAnimationProcedure.execute(entity, "imaginary_purple", true);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("sukuna_shrine")) {
			PlayArmAnimationProcedure.execute(entity, "domain_sukuna", true);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_control")) {
			SpeechExecuteCrushProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_binding")) {
			SpeechExecuteWeepProcedure.execute(entity);
		} else if (((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).current_moveset).equals("inumaki_utility")) {
			SpeechExecuteKneelProcedure.execute(entity);
		}
	}
}
