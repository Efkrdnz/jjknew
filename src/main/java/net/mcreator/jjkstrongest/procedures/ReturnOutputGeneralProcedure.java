package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;

public class ReturnOutputGeneralProcedure {
	public static double execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return 0;
		double result = 0;
		double recoil = 0;
		double BLACKFLASH = 0;
		double weather = 0;
		double overtime = 0;
		double emptyhand = 0;
		double overworld = 0;
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_overtime) {
			if (world.getLevelData().getGameTime() % 24000 > 15000 && world.getLevelData().getGameTime() % 24000 < 3000) {
				overtime = 0.2;
			} else {
				overtime = -0.2;
			}
		} else {
			overtime = 0;
		}
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_recoil) {
			recoil = 0.1;
		} else {
			recoil = 0;
		}
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_fairweatherfighter) {
			if (world.getLevelData().isRaining()) {
				weather = 0.1;
			} else {
				weather = -0.1;
			}
		} else {
			weather = 0;
		}
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_overworlddominance) {
			if ((entity.level().dimension()) == Level.OVERWORLD) {
				overworld = 0.1;
			} else {
				overworld = -0.1;
			}
		} else {
			overworld = 0;
		}
		if (entity instanceof LivingEntity _livEnt6 && _livEnt6.hasEffect(JjkStrongestModMobEffects.ZONE.get())) {
			BLACKFLASH = (1 + (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(JjkStrongestModMobEffects.ZONE.get()) ? _livEnt.getEffect(JjkStrongestModMobEffects.ZONE.get()).getAmplifier() : 0)) / 10;
		} else {
			BLACKFLASH = 0;
		}
		result = recoil + overtime + weather + overworld + BLACKFLASH;
		return result;
	}
}
