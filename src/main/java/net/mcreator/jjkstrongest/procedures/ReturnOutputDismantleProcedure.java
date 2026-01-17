package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

public class ReturnOutputDismantleProcedure {
	public static double execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return 0;
		double base = 0;
		double imbue = 0;
		double result = 0;
		double recoil = 0;
		double BLACKFLASH = 0;
		double weather = 0;
		double overtime = 0;
		double emptyhand = 0;
		double overworld = 0;
		base = ReturnOutputGeneralProcedure.execute(world, entity);
		if ((entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).vow_dismantleimbue) {
			if (((entity instanceof LivingEntity _entity) ? _entity.getMainHandItem() : ItemStack.EMPTY).getItem() instanceof SwordItem
					|| (entity instanceof LivingEntity _entity ? _entity.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof SwordItem
					|| ((entity instanceof LivingEntity _entity) ? _entity.getMainHandItem() : ItemStack.EMPTY).getItem() instanceof TridentItem
					|| (entity instanceof LivingEntity _entity ? _entity.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof TridentItem) {
				imbue = 0.2;
			} else {
				imbue = -0.5;
			}
		} else {
			imbue = 0;
		}
		result = base + imbue;
		return result;
	}
}
