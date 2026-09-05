package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

/**
 * The beat between the shell closing and the domain turning hostile: the rays fire
 * client-side and everyone inside is briefly blinded.
 */
public class DomainUVPostLinesPhaseProcedure {

	/** Tick within the phase at which the blinding flash lands. */
	private static final int FLASH_TICK = 30;
	private static final double BLIND_RADIUS = 35.0;

	public static void execute(ServerLevel level, DomainUVEntity domain, int settleTicks) {
		CompoundTag data = domain.getPersistentData();
		int postTick = data.getInt("postTick");

		if (postTick == FLASH_TICK)
			applyBlindness(level, domain);

		postTick++;
		data.putInt("postTick", postTick);
		domain.setPhaseProgress(Math.min(1.0f, (float) postTick / settleTicks));

		if (postTick >= settleTicks) {
			domain.setPhase(DomainPhase.ACTIVE);
			domain.setPhaseProgress(0.0f);
		}
	}

	private static void applyBlindness(ServerLevel level, DomainUVEntity domain) {
		String owner = domain.getPersistentData().getString("ownerUUID");
		AABB box = new AABB(domain.getX() - BLIND_RADIUS, domain.getY() - BLIND_RADIUS, domain.getZ() - BLIND_RADIUS, domain.getX() + BLIND_RADIUS, domain.getY() + BLIND_RADIUS,
				domain.getZ() + BLIND_RADIUS);
		double radiusSq = BLIND_RADIUS * BLIND_RADIUS;
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> true)) {
			if (target.getStringUUID().equals(owner))
				continue;
			if (target instanceof Player player && (player.isCreative() || player.isSpectator()))
				continue;
			if (target.position().distanceToSqr(domain.position()) > radiusSq)
				continue;
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 15, 0, true, false, false));
		}
	}
}
