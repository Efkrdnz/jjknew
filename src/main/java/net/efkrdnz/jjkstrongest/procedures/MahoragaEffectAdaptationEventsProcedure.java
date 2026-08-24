package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.UUID;
import java.util.List;
import java.util.Comparator;

@EventBusSubscriber(modid = "jjk_strongest", bus = EventBusSubscriber.Bus.GAME)
public class MahoragaEffectAdaptationEventsProcedure {
	@SubscribeEvent
	public static void onEffectApplicable(MobEffectEvent.Applicable event) {
		if (event == null)
			return;
		LivingEntity victim = event.getEntity();
		if (!(victim instanceof MahoragaEntity))
			return;
		if (victim.level().isClientSide())
			return;
		MobEffectInstance inst = event.getEffectInstance();
		if (inst == null || inst.getEffect() == null)
			return;
		if (inst.getEffect().value().getCategory() != MobEffectCategory.HARMFUL)
			return;
		ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value());
		if (id == null)
			return;
		String key = "maho_eff_" + sanitize(id);
		int spins = victim.getPersistentData().getInt(key);
		int full = MahoragaConstantsProcedure.FULL_SPINS;
		// already fully adapted -> deny and collapse if info overload
		if (spins >= full) {
			event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
			if ("jjk_strongest:information_overload".equals(id.toString())) {
				collapseDomainMahoragaIsInside(victim);
			}
			return;
		}
		// per-effect cooldown (2s) on attempt
		long now = victim.level().getGameTime();
		String cdKey = key + "_lasttry";
		long lastTry = victim.getPersistentData().getLong(cdKey);
		if (now - lastTry < 40)
			return;
		victim.getPersistentData().putLong(cdKey, now);
		double chance = getSpinChanceFromEffect(inst);
		RandomSource rand = victim.level().getRandom();
		if (rand.nextDouble() < chance) {
			spins++;
			victim.getPersistentData().putInt(key, spins);
			MahoragaWheelSpinProcedure.execute(victim.level(), victim);
			// if this spin completed adaptation, remove effect and deny this application
			if (spins >= full) {
				if (victim.hasEffect(inst.getEffect()))
					victim.removeEffect(inst.getEffect());
				event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
				if ("jjk_strongest:information_overload".equals(id.toString())) {
					collapseDomainMahoragaIsInside(victim);
				}
			}
		}
	}

	private static double getSpinChanceFromEffect(MobEffectInstance inst) {
		int amp = Math.max(0, inst.getAmplifier());
		int dur = Math.max(1, inst.getDuration());
		double durN = Math.min(1.0, dur / 200.0);
		double ampN = Math.min(1.0, (amp + 1) / 3.0);
		double sev = 0.55 * durN + 0.45 * ampN;
		double chance = 0.10 + sev * 0.55;
		if (chance > 0.75)
			chance = 0.75;
		return chance;
	}

	private static String sanitize(ResourceLocation id) {
		return id.toString().replace(':', '_').replace('/', '_').replace('.', '_');
	}

	// finds the DomainUV Mahoraga is inside and collapses the OWNER's domain using your procedure
	private static void collapseDomainMahoragaIsInside(Entity mahoraga) {
		if (!(mahoraga.level() instanceof ServerLevel level))
			return;
		AABB box = AABB.ofSize(mahoraga.position(), 220, 220, 220);
		List<DomainUVEntity> list = level.getEntitiesOfClass(DomainUVEntity.class, box, e -> true);
		if (list.isEmpty())
			return;
		DomainUVEntity closest = list.stream().min(Comparator.comparingDouble(d -> d.distanceToSqr(mahoraga))).orElse(null);
		if (closest == null)
			return;
		String ownerUUID = closest.getPersistentData().getString("ownerUUID");
		if (ownerUUID == null || ownerUUID.isEmpty()) {
			closest.getPersistentData().putInt("duration", 0);
			return;
		}
		try {
			UUID uuid = UUID.fromString(ownerUUID);
			Player owner = level.getServer().getPlayerList().getPlayer(uuid);
			if (owner != null) {
				DomainCollapseManualProcedure.collapsePlayerDomain(level, owner);
			} else {
				closest.getPersistentData().putInt("duration", 0);
			}
		} catch (Exception e) {
			closest.getPersistentData().putInt("duration", 0);
		}
	}
}
