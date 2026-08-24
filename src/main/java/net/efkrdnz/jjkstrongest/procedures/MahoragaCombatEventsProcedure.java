package net.efkrdnz.jjkstrongest.procedures;


import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

@EventBusSubscriber(modid = "jjk_strongest")
public class MahoragaCombatEventsProcedure {
	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		if (event == null)
			return;
		LivingEntity victim = event.getEntity();
		if (!(victim instanceof MahoragaEntity maho))
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
			id = ResourceLocation.fromNamespaceAndPath("minecraft", "generic");
		// store last hit type (useful generally)
		victim.getPersistentData().putString("maho_last_dmg_id", id.toString());
		// spin lock if bf is high (optional, soft)
		double bf = victim.getPersistentData().getDouble("maho_bf");
		boolean lockSpins = bf >= 3.0;
		String key = "maho_adapt_dmg_" + id.toString();
		String cdKey = key + "_lastspin";
		int spins = victim.getPersistentData().getInt(key);
		int full = MahoragaConstantsProcedure.FULL_SPINS;
		// per-damage-type cooldown: 40 ticks = 2 seconds
		long now = victim.level().getGameTime();
		long lastSpin = victim.getPersistentData().getLong(cdKey);
		boolean canSpinThisType = (now - lastSpin) >= 40;
		// choose who to notify (prefer Mahoraga's target, fallback to attacker)
		Player notifyPlayer = findNotifyPlayer(maho, source);
		// normal (non-forced) spin gain
		if (spins < full && canSpinThisType) {
			double chance = getSpinChance(victim, amount);
			if (lockSpins)
				chance *= 0.35;
			RandomSource rand = victim.level().getRandom();
			if (rand.nextDouble() < chance) {
				int before = spins;
				spins++;
				victim.getPersistentData().putInt(key, spins);
				victim.getPersistentData().putLong(cdKey, now);
				boolean justCompleted = (before < full) && (spins >= full);
				// spin heal + visuals + notify
				doSpin(victim, notifyPlayer, id.toString(), spins, full, justCompleted);
			}
		}
		// reduction scaling (based on current spins)
		double reduction;
		if (spins >= full)
			reduction = 0.92;
		else
			reduction = Math.min(0.60, spins * 0.20);
		float newAmount = (float) (amount * (1.0 - reduction));
		if (newAmount < 0.05F)
			newAmount = 0.05F;
		// buff healing when hit by something fully adapted to
		if (spins >= full) {
			int cd = victim.getPersistentData().getInt("maho_cd_adapt_heal");
			if (cd <= 0) {
				float heal = 3.0F + (float) Math.min(20.0, amount * 0.30F);
				if (!victim.isDeadOrDying() && victim.getHealth() > 0)
					victim.heal(heal);
				victim.getPersistentData().putInt("maho_cd_adapt_heal", 10);
			}
		}
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
			// fully adapted -> always cancel death and revive at FULL hp
			if (spins >= full) {
				event.setCanceled(true);
				victim.setHealth(victim.getMaxHealth());
				victim.invulnerableTime = 10;
				victim.getPersistentData().putDouble("maho_recent_dmg", 0);
				victim.getPersistentData().putInt("maho_recent_ticks", 0);
				victim.getPersistentData().putLong(cdKey, now);
				// wheel spin event still happens -> notify with ADAPTED
				doSpin(victim, notifyPlayer, id.toString(), spins, full, false);
				return;
			}
			// not enough burst -> cancel lethal, leave at 1 hp and FORCE a spin toward this type
			if (!burstEnough) {
				event.setCanceled(true);
				victim.setHealth(1.0F);
				victim.invulnerableTime = 8;
				int before = spins;
				if (spins < full) {
					spins++;
					victim.getPersistentData().putInt(key, spins);
				}
				victim.getPersistentData().putLong(cdKey, now);
				boolean justCompleted = (before < full) && (spins >= full);
				doSpin(victim, notifyPlayer, id.toString(), spins, full, justCompleted);
				return;
			}
			// enough burst -> allow death normally
			event.setAmount(newAmount);
			return;
		}
		// not lethal -> apply reduced amount
		event.setAmount(newAmount);
	}

	private static void doSpin(LivingEntity victim, Player notifyPlayer, String damageId, int spins, int full, boolean justCompleted) {
		if (victim == null)
			return;
		// heal on every spin
		if (!victim.isDeadOrDying() && victim.getHealth() > 0)
			victim.heal(10.0F);
		// visuals
		MahoragaWheelSpinProcedure.execute(victim.level(), victim);
		// notify
		sendAdaptMessage(notifyPlayer, damageId, spins, full, justCompleted);
	}

	private static void sendAdaptMessage(Player player, String damageId, int spins, int full, boolean justCompleted) {
		if (player == null)
			return;
		if (full <= 0)
			full = 1;
		int pct = (int) Math.round((spins * 100.0) / full);
		if (pct > 100)
			pct = 100;
		String status = (spins >= full) ? "ADAPTED" : "ADAPTING";
		// actionbar=true so it doesn't spam chat too hard
		player.displayClientMessage(Component.literal("§8[§bMahoraga§8] §7Adaptation to §f" + damageId + " §7= §e" + pct + "% §7(" + spins + "/" + full + ") §f" + status), true);
		if (justCompleted) {
			player.displayClientMessage(Component.literal("§8[§bMahoraga§8] §aFULLY ADAPTED to §f" + damageId + "§a!"), false);
		}
	}

	private static Player findNotifyPlayer(MahoragaEntity maho, DamageSource source) {
		// 1) prefer Mahoraga's current target
		LivingEntity tgt = maho.getTarget();
		if (tgt instanceof Player p)
			return p;
		// 2) fallback to damage source entity
		Entity attacker = source.getEntity();
		if (attacker instanceof Player p)
			return p;
		// 3) fallback to direct entity (projectile, etc)
		Entity direct = source.getDirectEntity();
		if (direct instanceof Player p)
			return p;
		return null;
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
