package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class ReturnChantTESTProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		if (!(entity.getPersistentData().getString("chanting")).equals("")) {
			return entity.getPersistentData().getString("chanting");
		}
		return "";
	}
}
