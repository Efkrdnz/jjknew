package net.efkrdnz.jjkstrongest.procedures;


import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaWheelSpinProcedure {
	// plays spin and gives heal
	public static void execute(LevelAccessor world, Entity entity) {
		if (world == null || entity == null)
			return;
		if (entity instanceof LivingEntity le) {
			if (!le.isDeadOrDying() && le.getHealth() > 0) {
				le.heal(15.0F);
			}
		}
		if (entity instanceof MahoragaEntity m) {
			m.setAnimation("spin");
			entity.getPersistentData().putInt("maho_anim_clear", 12);
		}
		if (world instanceof Level _level) {
			double x = entity.getX();
			double y = entity.getY();
			double z = entity.getZ();
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:wheelspin")), SoundSource.NEUTRAL, 1, 1);
			} else {
				_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:wheelspin")), SoundSource.NEUTRAL, 1, 1, false);
			}
		}
	}
}
