package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = "jjk_strongest", bus = EventBusSubscriber.Bus.GAME)
public class BeforeInfinityProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingIncomingDamageEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getSource(), event.getEntity());
		}
	}

	public static void execute(LevelAccessor world, double x, double y, double z, DamageSource damagesource, Entity entity) {
		execute(null, world, x, y, z, damagesource, entity);
	}

	// infinity gate with mahoraga bypass and domain amplification counter
	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, DamageSource damagesource, Entity entity) {
		if (damagesource == null || entity == null)
			return;
		if (damagesource.is(DamageTypes.GENERIC_KILL))
			return;
		if (!(entity instanceof LivingEntity victim))
			return;
		if (!(victim.hasEffect(JjkStrongestModMobEffects.INFINITY)))
			return;
		// find attacker
		Entity attacker = damagesource.getEntity();
		if (attacker == null)
			attacker = damagesource.getDirectEntity();
		// domain amplification neutralizes infinity and removes it
		if (attacker instanceof LivingEntity livingAttacker && livingAttacker.hasEffect(JjkStrongestModMobEffects.DOMAIN_AMPLIFICATION)) {
			victim.removeEffect(JjkStrongestModMobEffects.INFINITY);
			// let the attack through without cancelling
			return;
		}
		// mahoraga adapts to infinity and eventually bypasses
		if (attacker instanceof MahoragaEntity maho) {
			int full = MahoragaConstantsProcedure.FULL_SPINS;
			int spins = maho.getPersistentData().getInt("maho_adapt_infinity");
			// already adapted -> do not cancel
			if (spins >= full)
				return;
			long now = maho.level().getGameTime();
			long lastTry = maho.getPersistentData().getLong("maho_adapt_infinity_lasttry");
			// 2s per try
			if (now - lastTry >= 40) {
				maho.getPersistentData().putLong("maho_adapt_infinity_lasttry", now);
				double chance = 0.50;
				double bf = maho.getPersistentData().getDouble("maho_bf");
				if (bf >= 3.0)
					chance *= 0.35;
				RandomSource rand = maho.level().getRandom();
				if (rand.nextDouble() < chance) {
					spins++;
					maho.getPersistentData().putInt("maho_adapt_infinity", spins);
					MahoragaWheelSpinProcedure.execute(maho.level(), maho);
					// if this completed adaptation, let this hit through
					if (spins >= full)
						return;
				}
			}
			// not adapted yet -> infinity blocks
			cancel(event);
			playInfinitySound(world, x, y, z);
			return;
		}
		// everyone else gets blocked
		cancel(event);
		playInfinitySound(world, x, y, z);
	}

	private static void cancel(@Nullable Event event) {
		if (event == null)
			return;
		if (event instanceof ICancellableEvent cancellable)
			cancellable.setCanceled(true);
	}

	private static void playInfinitySound(LevelAccessor world, double x, double y, double z) {
		if (world instanceof Level _level) {
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.amethyst_block.step")), SoundSource.NEUTRAL, 1, 1);
			} else {
				_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.amethyst_block.step")), SoundSource.NEUTRAL, 1, 1, false);
			}
		}
	}
}
