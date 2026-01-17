package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;

public class BlackFlashExecuteProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		double zone_level = 0;
		double max_zone_level = 5;
		// check if attacker is yuji for higher zone cap
		if (((sourceentity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("yuji")) {
			max_zone_level = 8;
		}
		// add zone effect to attacker
		if (sourceentity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
			if (_entity.hasEffect(JjkStrongestModMobEffects.ZONE.get())) {
				zone_level = _entity.getEffect(JjkStrongestModMobEffects.ZONE.get()).getAmplifier() + 1;
				if (zone_level < max_zone_level) {
					_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.ZONE.get(), -1, (int) zone_level, false, false));
				}
			} else {
				_entity.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.ZONE.get(), -1, 0, false, false));
			}
		}
		// apply debuffs to victim
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
			_entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
			_entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
		}
		// apply knockback
		if (entity instanceof LivingEntity _entity) {
			Vec3 knockbackDir = entity.position().subtract(sourceentity.position()).normalize();
			_entity.setDeltaMovement(_entity.getDeltaMovement().add(knockbackDir.x * 2.5, 0.8, knockbackDir.z * 2.5));
		}
		// SPAWN BLACK FLASH LIGHTNING ENTITY (replaces particles)
		SpawnBlackFlashLightningProcedure.execute((Level) world, entity);
		// play sound
		if (world instanceof Level _level) {
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:blackflash")), SoundSource.NEUTRAL, (float) 0.3,
						(float) Mth.nextDouble(RandomSource.create(), 0.75, 1.25));
			} else {
				_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:blackflash")), SoundSource.NEUTRAL, (float) 0.3, (float) Mth.nextDouble(RandomSource.create(), 0.75, 1.25), false);
			}
		}
		// trigger shader effect
		if (world instanceof Level _level) {
			TriggerBlackFlashShaderProcedure.execute(_level, sourceentity, 15, 1.0f);
		}
	}
}
