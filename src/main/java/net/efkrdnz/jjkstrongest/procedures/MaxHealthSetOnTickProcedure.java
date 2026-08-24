package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;

import javax.annotation.Nullable;

@EventBusSubscriber
public class MaxHealthSetOnTickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity().level(), event.getEntity());
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (world.getLevelData().getGameTime() % 20 == 0) {
			if (!((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).sorcerer).equals("")) {
				if (!(entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(MobEffects.HEALTH_BOOST))) {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 99999999, 15, false, false));
					if (entity instanceof LivingEntity _entity)
						_entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1);
				}
				if ((entity.getData(JjkStrongestModVariables.PLAYER_VARIABLES)).CE_FLOW) {
					if (!(entity instanceof LivingEntity _livEnt5 && _livEnt5.hasEffect(MobEffects.MOVEMENT_SPEED))) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999999, 5, false, false));
					}
					if (!(entity instanceof LivingEntity _livEnt7 && _livEnt7.hasEffect(MobEffects.JUMP))) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 99999999, 2, false, false));
					}
					if (!(entity instanceof LivingEntity _livEnt9 && _livEnt9.hasEffect(MobEffects.DAMAGE_BOOST))) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999999, 1, false, false));
					}
					if (!(entity instanceof LivingEntity _livEnt11 && _livEnt11.hasEffect(MobEffects.DAMAGE_RESISTANCE))) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999999, 1, false, false));
					}
				}
			}
		}
	}
}
