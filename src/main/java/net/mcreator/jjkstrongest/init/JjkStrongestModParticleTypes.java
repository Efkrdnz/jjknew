
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.jjkstrongest.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

import net.mcreator.jjkstrongest.JjkStrongestMod;

public class JjkStrongestModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, JjkStrongestMod.MODID);
	public static final RegistryObject<SimpleParticleType> INFINITY_PARTICLE = REGISTRY.register("infinity_particle", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> RED_01 = REGISTRY.register("red_01", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> RED_02 = REGISTRY.register("red_02", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> RED_03 = REGISTRY.register("red_03", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PUNCH_IMPACT = REGISTRY.register("punch_impact", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> EXPLOSION_CUSTOM = REGISTRY.register("explosion_custom", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PURPLE_01 = REGISTRY.register("purple_01", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PURPLE_02 = REGISTRY.register("purple_02", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BLUEPARTICLE_1 = REGISTRY.register("blueparticle_1", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BLUEPARTICLE_2 = REGISTRY.register("blueparticle_2", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BLUEPARTICLE_3 = REGISTRY.register("blueparticle_3", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BLUE_AURA = REGISTRY.register("blue_aura", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BLUE_DUST = REGISTRY.register("blue_dust", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PARTICLE_BLACK_FLASH = REGISTRY.register("particle_black_flash", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PARTICLE_HP_CHARGE = REGISTRY.register("particle_hp_charge", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PARTICLE_HP_CHARGE_2 = REGISTRY.register("particle_hp_charge_2", () -> new SimpleParticleType(true));
}
