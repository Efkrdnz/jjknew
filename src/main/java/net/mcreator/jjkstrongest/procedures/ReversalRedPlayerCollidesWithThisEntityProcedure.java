package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.entity.Entity;

public class ReversalRedPlayerCollidesWithThisEntityProcedure {
	public static void execute(Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (!(entity.getPersistentData().getString("caster")).equals(sourceentity.getDisplayName().getString())) {
			sourceentity.getPersistentData().putBoolean("RedDrag", true);
		}
	}
}
