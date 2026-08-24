package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

public class LapseBlueMoveProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		Entity owner = null;
		double tp = 0;
		if (!((entity instanceof TamableAnimal _tamEnt ? (Entity) _tamEnt.getOwner() : null) == (null))) {
			owner = entity instanceof TamableAnimal _tamEnt ? (Entity) _tamEnt.getOwner() : null;
		} else {
			return;
		}
		tp = entity.getPersistentData().getDouble("TechniquePower");
		{
			Entity _ent = entity;
			_ent.teleportTo((owner.getX() + (6 + 3 * tp) * owner.getLookAngle().x), (owner.getY() + 1.6 + (5 + 3 * tp) * owner.getLookAngle().y), (owner.getZ() + (6 + 3 * tp) * owner.getLookAngle().z));
			if (_ent instanceof ServerPlayer _serverPlayer)
				_serverPlayer.connection.teleport((owner.getX() + (6 + 3 * tp) * owner.getLookAngle().x), (owner.getY() + 1.6 + (5 + 3 * tp) * owner.getLookAngle().y), (owner.getZ() + (6 + 3 * tp) * owner.getLookAngle().z), _ent.getYRot(),
						_ent.getXRot());
		}
		if (!owner.isShiftKeyDown()) {
			entity.getPersistentData().putBoolean("stay", true);
		}
	}
}
