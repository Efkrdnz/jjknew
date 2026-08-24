package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

public class MahoragaConstantsProcedure {
	// shared constants
	public static final int FULL_SPINS = 4;
	public static final int BURST_WINDOW_TICKS = 40; // 2s
	public static final double BURST_KILL_RATIO = 0.65; // maxhp * ratio
	public static final int BF_CAP = 5;
	public static final double BF_DECAY = 0.02; // per tick
	public static final TagKey<DamageType> JUJUTSU_TAG = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "jujutsu"));
}
