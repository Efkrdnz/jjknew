package net.mcreator.jjkstrongest.procedures;

import org.checkerframework.checker.units.qual.cd;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.mcreator.jjkstrongest.entity.MahoragaEntity;

@Mod.EventBusSubscriber(modid = "jjk_strongest")
public class MahoragaCombatEventsProcedure {
	// adapts and modifies damage when mahoraga is hurt
	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		if (event == null)
			return;
		LivingEntity victim = event.getEntity();
		if (!(victim instanceof MahoragaEntity))
			return;
		if (victim.level().isClientSide())
			return;
		DamageSource source = event.getSource();
		float amount = event.getAmount();
		// mark recently hurt for aura interrupt etc
		victim.getPersistentData().putInt("maho_hurt_ticks", 10);
		// only adapt to technique tag
		if (!source.is(MahoragaConstantsProcedure.JUJUTSU_TAG))
			return;
		ResourceLocation id = victim.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getKey(source.type());
		if (id == null)
			id = new ResourceLocation("minecraft", "generic");
		// spin lock if bf is high (optional, soft)
		double bf = victim.getPersistentData().getDouble("maho_bf");
		boolean lockSpins = bf >= 3.0;
		String key = "maho_adapt_dmg_" + id.toString();
		String cdKey = key + "_lastspin";
		int spins = victim.getPersistentData().getInt(key);
		// per-damage-type cooldown: 40 ticks = 2 seconds
		long now = victim.level().getGameTime();
		long lastSpin = victim.getPersistentData().getLong(cdKey);
		boolean canSpinThisType = (now - lastSpin) >= 40;
		// normal (non-forced) spin gain
		if (spins < MahoragaConstantsProcedure.FULL_SPINS && canSpinThisType) {
			double chance = getSpinChance(victim, amount);
			if (lockSpins)
				chance *= 0.35;
			RandomSource rand = victim.level().getRandom();
			if (rand.nextDouble() < chance) {
				spins++;
				victim.getPersistentData().putInt(key, spins);
				victim.getPersistentData().putLong(cdKey, now); // start cooldown only when spin happens
				MahoragaWheelSpinProcedure.execute(victim.level(), victim);
			}
		}
		// reduction scaling (based on current spins)
		double reduction;
		if (spins >= MahoragaConstantsProcedure.FULL_SPINS)
			reduction = 0.92;
		else
			reduction = Math.min(0.60, spins * 0.20);
		float newAmount = (float) (amount * (1.0 - reduction));
		if (newAmount < 0.05F)
			newAmount = 0.05F;
		// burst tracking (post reduction)
		double recent = victim.getPersistentData().getDouble("maho_recent_dmg");
		recent += newAmount;
		victim.getPersistentData().putDouble("maho_recent_dmg", recent);
		victim.getPersistentData().putInt("maho_recent_ticks", MahoragaConstantsProcedure.BURST_WINDOW_TICKS);
		// lethal gate
		float hp = victim.getHealth();
		boolean lethal = (hp - newAmount) <= 0.0F;
		if (lethal) {
			double need = victim.getMaxHealth() * MahoragaConstantsProcedure.BURST_KILL_RATIO;
			boolean burstEnough = recent >= need;
			// fully adapted -> always cancel death and recover
			if (spins >= MahoragaConstantsProcedure.FULL_SPINS) {
				// small heal burst when hit by adapted type
				int cd = victim.getPersistentData().getInt("maho_cd_adapt_heal");
				if (cd <= 0) {
					float heal = 2.0F + (float) Math.min(6.0, amount * 0.25F);
					if (!victim.isDeadOrDying() && victim.getHealth() > 0) {
						victim.heal(heal);
					}
					victim.getPersistentData().putInt("maho_cd_adapt_heal", 15);
				}
				event.setCanceled(true);
				float recover = (float) (victim.getMaxHealth() * Math.max(0.35, 1.0 - 0.12 * bf));
				victim.setHealth(recover);
				victim.invulnerableTime = 10;
				victim.getPersistentData().putDouble("maho_recent_dmg", 0);
				victim.getPersistentData().putInt("maho_recent_ticks", 0);
				// visual spin always, also refresh cooldown timestamp to prevent spam
				victim.getPersistentData().putLong(cdKey, now);
				MahoragaWheelSpinProcedure.execute(victim.level(), victim);
				return;
			}
			// not enough burst -> cancel lethal, leave at 1 hp and FORCE a spin toward this type
			if (!burstEnough) {
				event.setCanceled(true);
				victim.setHealth(1.0F);
				victim.invulnerableTime = 8;
				// forced spin bypasses cooldown but sets timestamp
				if (spins < MahoragaConstantsProcedure.FULL_SPINS) {
					spins++;
					victim.getPersistentData().putInt(key, spins);
				}
				victim.getPersistentData().putLong(cdKey, now);
				MahoragaWheelSpinProcedure.execute(victim.level(), victim);
				return;
			}
			// enough burst -> allow death normally
			event.setAmount(newAmount);
			return;
		}
		// not lethal -> apply reduced amount
		event.setAmount(newAmount);
	}

	private static double getSpinChance(LivingEntity victim, float takenDamage) {
		double max = Math.max(1.0, victim.getMaxHealth());
		double hp = victim.getHealth();
		double severity = takenDamage / max;
		double chance = 0.18 + Math.min(0.62, severity * 2.2);
		double afterRatio = (hp - takenDamage) / max;
		if (afterRatio < 0.25)
			chance += 0.10;
		if (afterRatio < 0.10)
			chance += 0.15;
		if (chance > 0.95)
			chance = 0.95;
		if (chance < 0.05)
			chance = 0.05;
		return chance;
	}
}
