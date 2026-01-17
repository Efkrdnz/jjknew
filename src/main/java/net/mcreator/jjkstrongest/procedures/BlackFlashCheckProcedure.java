package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;
import net.mcreator.jjkstrongest.init.JjkStrongestModItems;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class BlackFlashCheckProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		if (event == null || event.isCanceled())
			return;
		Entity entity = event.getEntity();
		Entity sourceentity = event.getSource().getEntity();
		DamageSource damageSource = event.getSource();
		Entity immediatesourceentity = event.getSource().getDirectEntity();
		if (entity != null && sourceentity != null) {
			execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, sourceentity, damageSource, immediatesourceentity);
		}
	}

	public static void execute(Level world, double x, double y, double z, Entity entity, Entity sourceentity, DamageSource damageSource, Entity immediatesourceentity) {
		execute(null, world, x, y, z, entity, sourceentity, damageSource, immediatesourceentity);
	}

	private static void execute(@Nullable Event event, Level world, double x, double y, double z, Entity entity, Entity sourceentity, DamageSource damageSource, Entity immediatesourceentity) {
		if (entity == null || sourceentity == null || damageSource == null)
			return;
		// check if damage is from jujutsu technique - skip black flash if true
		if ((damageSource).is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu")))) {
			return;
		}
		if (sourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("technique")))) {
			return;
		}
		if (!(immediatesourceentity == null) && immediatesourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("technique")))) {
			return;
		}
		ItemStack helditem = ItemStack.EMPTY;
		double base_chance = 0;
		double zone_bonus = 0;
		double zone_level = 0;
		double total_chance = 0;
		double roll = 0;
		if (sourceentity instanceof Player) {
			helditem = (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
			// check if holding BF Test Item - instant 100% chance
			if (helditem.getItem() == JjkStrongestModItems.BF_TEST_ITEM.get()) {
				total_chance = 100;
			}
			// check if holding weapon - no Black Flash
			else if (helditem.getItem() instanceof AxeItem || helditem.getItem() instanceof SwordItem || helditem.getItem() instanceof TridentItem || helditem.getItem() instanceof ShovelItem) {
				return;
			}
			// normal Black Flash calculation
			else {
				// get base chance based on character
				if (((sourceentity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("gojo")) {
					base_chance = 3;
				} else if (((sourceentity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).sorcerer).equals("yuji")) {
					base_chance = 12;
				} else {
					base_chance = 6;
				}
				// get zone bonus
				if (sourceentity instanceof LivingEntity _livEnt && _livEnt.hasEffect(JjkStrongestModMobEffects.ZONE.get())) {
					zone_level = _livEnt.getEffect(JjkStrongestModMobEffects.ZONE.get()).getAmplifier();
				} else {
					zone_level = 0;
				}
				zone_bonus = 3 * zone_level;
				total_chance = base_chance + zone_bonus;
				// cap at 100
				if (total_chance > 100) {
					total_chance = 100;
				}
			}
			// roll for Black Flash
			roll = Mth.nextInt(RandomSource.create(), 0, 100);
			if (roll <= total_chance) {
				// execute Black Flash effects directly
				BlackFlashExecuteProcedure.execute(world, x, y, z, entity, sourceentity);
			}
		}
	}
}
