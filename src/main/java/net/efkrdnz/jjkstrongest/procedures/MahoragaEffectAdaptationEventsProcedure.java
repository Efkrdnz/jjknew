package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;
import net.efkrdnz.jjkstrongest.network.DomainShellSyncPacket;

import java.util.UUID;

@EventBusSubscriber(modid = "jjk_strongest", bus = EventBusSubscriber.Bus.GAME)
public class MahoragaEffectAdaptationEventsProcedure {

	/** Ten seconds of being overwhelmed before the wheel finishes turning. */
	public static final int UV_ADAPT_TICKS = 200;

	/** Ticks of real exposure banked so far. Never reset, so broken-up exposure still counts. */
	private static final String UV_ADAPT_KEY = "maho_uv_adapt_ticks";

	/**
	 * What the shell keeps when Mahoraga breaks it: cracked right through, but not holed.
	 *
	 * <p>Below about an eighth the collapse pass reads a cell as an opening and draws nothing
	 * where it was, so a lower number here would delete the barrier rather than break it.
	 */
	private static final float SHATTER_REMAINING = 0.18f;

	private static final ResourceLocation OVERLOAD = ResourceLocation.parse("jjk_strongest:information_overload");

	/** The same key {@link #onEffectApplicable} counts spins in, built the same way. */
	private static final String OVERLOAD_SPINS = "maho_eff_" + sanitize(OVERLOAD);

	/**
	 * Mahoraga adapting his way out of an Unlimited Void, on a clock rather than on dice.
	 *
	 * <p>Every other effect still rolls in {@link #onEffectApplicable}: one attempt every two
	 * seconds, four successes needed. For Information Overload that came out at fourteen
	 * seconds <em>expected</em> and anywhere from four to thirty in practice, which makes the
	 * one domain in the mod that is supposed to be a death sentence a coin flip. This counts
	 * instead, and only while the effect is actually on him, so what it measures is time spent
	 * inside the domain rather than time spent anywhere.
	 *
	 * <p>The counter is never cleared. Leave the Void at five seconds, come back, and he
	 * finishes adapting five seconds later — letting him out once should cost you.
	 *
	 * <p>Called from {@code MahoragaEntity.baseTick()}, which vanilla runs whether or not the
	 * mob has AI — and by the time this matters, he does not.
	 */
	public static void tickVoidAdaptation(MahoragaEntity mahoraga) {
		if (mahoraga.level().isClientSide())
			return;
		if (!mahoraga.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD))
			return;
		CompoundTag data = mahoraga.getPersistentData();
		if (data.getInt(OVERLOAD_SPINS) >= MahoragaConstantsProcedure.FULL_SPINS)
			return;
		int ticks = data.getInt(UV_ADAPT_KEY) + 1;
		data.putInt(UV_ADAPT_KEY, ticks);
		if (ticks < UV_ADAPT_TICKS)
			return;
		// Filling in the existing spin counter rather than inventing a second flag: from here
		// the already-adapted branch below denies the effect outright, for good, and denies it
		// with the same collapse it always did. No second immunity to keep in step with the
		// first one.
		data.putInt(OVERLOAD_SPINS, MahoragaConstantsProcedure.FULL_SPINS);
		MahoragaWheelSpinProcedure.execute(mahoraga.level(), mahoraga);
		// Un-freezes him on this same tick, through DomainSuppression's restore branch.
		mahoraga.removeEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD);
		collapseDomainMahoragaIsInside(mahoraga);
	}

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
			if (isOverload(id)) {
				collapseDomainMahoragaIsInside(victim);
			}
			return;
		}
		// Information Overload is on a clock now, not on dice — see tickVoidAdaptation. Rolling
		// for it here as well would put the two in competition: a lucky pair of rolls would let
		// him walk out of a Void in four seconds, long before the ten the clock promises.
		if (isOverload(id))
			return;
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
				if (isOverload(id)) {
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

	private static boolean isOverload(ResourceLocation id) {
		return OVERLOAD.equals(id);
	}

	private static String sanitize(ResourceLocation id) {
		return id.toString().replace(':', '_').replace('/', '_').replace('.', '_');
	}

	// Collapses the domain Mahoraga is actually standing in.
	//
	// This used to take the nearest domain within 220 blocks and collapse that,
	// whether or not Mahoraga was inside it — adapting to Information Overload in
	// one domain could shut down somebody else's across the map. The registry gives
	// the containing domain directly, so it now collapses the right one or nothing.
	private static void collapseDomainMahoragaIsInside(Entity mahoraga) {
		if (!(mahoraga.level() instanceof ServerLevel level))
			return;
		DomainUVEntity inside = null;
		for (DomainUVEntity domain : DomainRegistry.voidsIn(level)) {
			// Already coming down. The sure-hit keeps trying to land on an adapted Mahoraga
			// every second it can still see him, and without this each of those attempts would
			// re-break a barrier that is mid-collapse and put another shell snapshot on the wire.
			if (domain.getPhase() == DomainPhase.COLLAPSING)
				continue;
			if (domain.isAlive() && domain.sphere().contains(mahoraga.getX(), mahoraga.getY(), mahoraga.getZ())) {
				inside = domain;
				break;
			}
		}
		if (inside == null)
			return;
		shatterShell(inside, mahoraga);
		String ownerUUID = inside.getPersistentData().getString("ownerUUID");
		Player owner = null;
		if (ownerUUID != null && !ownerUUID.isEmpty()) {
			try {
				owner = level.getServer().getPlayerList().getPlayer(UUID.fromString(ownerUUID));
			} catch (IllegalArgumentException malformedUUID) {
				owner = null;
			}
		}
		if (owner != null) {
			DomainCollapseManualProcedure.collapsePlayerDomain(level, owner);
		} else {
			inside.getPersistentData().putInt("duration", 0);
			DomainUVEntityTickProcedure.beginCollapse(inside);
		}
	}

	/**
	 * Breaks the barrier before it comes apart, so the collapse reads as broken rather than
	 * switched off.
	 *
	 * <p>A domain that simply runs out of time fades from a pristine shell: the collapse pass
	 * draws every shard's own fracture face, but the white shatter across them is scaled by how
	 * damaged the barrier was, and an untouched one gets none of it. Mahoraga did not wait this
	 * one out. Cracking the shell through first is what puts the breaks on the pieces, and
	 * pointing the fracture at him is what makes them lean away from where he is standing.
	 *
	 * <p>The snapshot has to go out by hand. The per-tick sync lives in {@code tickShell},
	 * which only runs while the domain is sealed — and the caller puts it into its collapse on
	 * the very next line, so nothing would ever send this.
	 */
	private static void shatterShell(DomainUVEntity domain, Entity breaker) {
		DomainShell shell = domain.shell();
		if (shell == null)
			return;
		Vec3 from = breaker.position().subtract(domain.position());
		shell.fracture(SHATTER_REMAINING, from);
		domain.setShellIntegrity(shell.totalIntegrity());
		PacketDistributor.sendToPlayersTrackingEntity(domain, new DomainShellSyncPacket(domain.getId(), shell.version(), shell.snapshot()));
		shell.markSynced();
	}
}
