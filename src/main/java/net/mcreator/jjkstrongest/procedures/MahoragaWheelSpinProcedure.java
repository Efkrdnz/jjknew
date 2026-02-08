package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

public class MahoragaWheelSpinProcedure {
	// plays wheel spin anim + sound
	public static void execute(LevelAccessor world, Entity entity) {
		if (world == null || entity == null)
			return;
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		if (entity instanceof MahoragaEntity _m)
			_m.setAnimation("spin");
		entity.getPersistentData().putInt("maho_anim_clear", 2);
		if (world instanceof Level _level) {
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:wheelspin")), SoundSource.NEUTRAL, 1, 1);
			} else {
				_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:wheelspin")), SoundSource.NEUTRAL, 1, 1, false);
			}
		}
	}
}
