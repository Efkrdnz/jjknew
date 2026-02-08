package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class WhenEntityIsAttackedProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getSource(), event.getEntity(), event.getSource().getDirectEntity(), event.getSource().getEntity());
		}
	}

	public static void execute(DamageSource damagesource, Entity entity, Entity immediatesourceentity, Entity sourceentity) {
		execute(null, damagesource, entity, immediatesourceentity, sourceentity);
	}

	private static void execute(@Nullable Event event, DamageSource damagesource, Entity entity, Entity immediatesourceentity, Entity sourceentity) {
		if (damagesource == null || entity == null || immediatesourceentity == null || sourceentity == null)
			return;
		Entity target = null;
		target = entity;
		target = entity;
		target = sourceentity;
		target = immediatesourceentity;
		if (!damagesource.is(TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("forge:jujutsu")))) {
			MarkEntityOnHitProcedure.execute(entity.level(), sourceentity, entity, immediatesourceentity);
		}
	}
}
