
package net.efkrdnz.jjkstrongest.potion;

import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.GuiGraphics;

import net.efkrdnz.jjkstrongest.procedures.InfinityOnEffectExpireProcedure;
import net.efkrdnz.jjkstrongest.procedures.InfinityOnEffectActiveTickProcedure;

public class InfinityMobEffect extends MobEffect {
	public InfinityMobEffect() {
		super(MobEffectCategory.NEUTRAL, -6684673);
	}

	@Override
	public String getDescriptionId() {
		return "effect.jjk_strongest.infinity";
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		InfinityOnEffectActiveTickProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void initializeClient(java.util.function.Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new IClientMobEffectExtensions() {
			@Override
			public boolean isVisibleInInventory(MobEffectInstance effect) {
				return false;
			}

			@Override
			public boolean renderInventoryText(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
				return false;
			}
		});
	}
}
