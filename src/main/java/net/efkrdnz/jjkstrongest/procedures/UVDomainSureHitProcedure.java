package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects;

/**
 * The domain's sure-hit: everyone caught inside takes Information Overload.
 *
 * <p>This used to test a 58-block <em>cube</em> against a 30-block sphere, so it
 * reached about fifty blocks into the corners — well outside the barrier — and hit
 * people who were not in the domain at all. It tests the actual sphere now.
 */
public class UVDomainSureHitProcedure {

	private static final TagKey<EntityType<?>> TECHNIQUE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique"));

	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof DomainUVEntity domain) || !(world instanceof ServerLevel level))
			return;
		DomainSphere sphere = domain.sphere();
		if (!sphere.isUsable())
			return;
		String owner = domain.getPersistentData().getString("ownerUUID");

		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sphere.bounds(), e -> true)) {
			if (target.getStringUUID().equals(owner))
				continue;
			if (target.getType().is(TECHNIQUE))
				continue;
			if (target instanceof Player player && (player.isCreative() || player.isSpectator()))
				continue;
			if (!sphere.contains(target.getX(), target.getY(), target.getZ()))
				continue;
			target.addEffect(new MobEffectInstance(JjkStrongestModMobEffects.INFORMATION_OVERLOAD, 200, 1));
		}
	}
}
