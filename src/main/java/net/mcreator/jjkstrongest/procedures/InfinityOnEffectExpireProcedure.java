package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class InfinityOnEffectExpireProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		CompoundTag nbt = entity.getPersistentData();
		if (nbt.getBoolean("infinityFrozen")) {
			// restore original velocity
			entity.setDeltaMovement(new Vec3(nbt.getDouble("infinityOriginalVelX"), nbt.getDouble("infinityOriginalVelY"), nbt.getDouble("infinityOriginalVelZ")));
			// clean up
			nbt.remove("infinityFrozen");
			nbt.remove("infinityOriginalVelX");
			nbt.remove("infinityOriginalVelY");
			nbt.remove("infinityOriginalVelZ");
			nbt.remove("infinityCrushing");
			nbt.remove("infinityCrushTimer");
		}
	}
}
