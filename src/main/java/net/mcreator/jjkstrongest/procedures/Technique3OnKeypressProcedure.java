package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;
import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;

public class Technique3OnKeypressProcedure {
	public static void execute(LevelAccessor world, double y, Entity entity) {
		if (entity == null)
			return;
		double x = 0;
		double z = 0;
		double yaw = 0;
		double wcs_pwer = 0;
		entity.getPersistentData().putDouble("TechniquePower", 1);
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("gojo")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_blue")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_blue >= 1) {
					entity.getPersistentData().putString("chanting", "blue");
					PlayArmAnimationProcedure.execute(entity, "blue_charge", true);
				}
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_red")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_red >= 1) {
					entity.getPersistentData().putString("chanting", "red");
					PlayArmAnimationProcedure.execute(entity, "red_charge", true);
				}
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("gojo_purple")) {
				if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).charge_purple >= 3) {
					x = entity.getX();
					z = entity.getZ();
					yaw = entity.getYRot();
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.PURPLE_CHARGING.get(), 50, 1, false, false));
					entity.getPersistentData().putString("chanting", "purple");
					PlayArmAnimationProcedure.execute(entity, "hollow_purple", true);
					if (world instanceof ServerLevel _serverLevel) {
						Entity entityinstance = JjkStrongestModEntities.HOLLOW_PURPLE_CHARGE.get().create(_serverLevel, null, null, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED, false, false);
						if (entityinstance != null) {
							entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
							{
								Entity _ent = entityinstance;
								_ent.setYRot((float) yaw);
								_ent.setXRot(0);
								_ent.setYBodyRot(_ent.getYRot());
								_ent.setYHeadRot(_ent.getYRot());
								_ent.yRotO = _ent.getYRot();
								_ent.xRotO = _ent.getXRot();
								if (_ent instanceof LivingEntity _entity) {
									_entity.yBodyRotO = _entity.getYRot();
									_entity.yHeadRotO = _entity.getYRot();
								}
							}
							if (entityinstance instanceof TamableAnimal _toTame && entity instanceof Player _owner)
								_toTame.tame(_owner);
							_serverLevel.addFreshEntity(entityinstance);
						}
					}
				}
			}
		} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("sukuna")) {
			if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_dismantle")) {
				{
					double _setval = entity.getX() + 25 * entity.getLookAngle().x;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_x1 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = entity.getY() + entity.getBbHeight() + 25 * entity.getLookAngle().y;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_y1 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = entity.getZ() + 25 * entity.getLookAngle().z;
					entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wcs_z1 = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				PlayArmAnimationProcedure.execute(entity, "dismantle", true);
			} else if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).equals("sukuna_fuga")) {
				entity.getPersistentData().putString("chanting", "flame_arrow");
				PlayArmAnimationProcedure.execute(entity, "fuga_hold", true);
			}
		}
		if (((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).current_moveset).contains("melee")) {
			MeleeBarrageProcedure.execute(entity.level(), entity);
		}
	}
}
