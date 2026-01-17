package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class ReturnHealthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "\u00A74" + Math.round(entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) + " / " + Math.round(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1);
	}
}
