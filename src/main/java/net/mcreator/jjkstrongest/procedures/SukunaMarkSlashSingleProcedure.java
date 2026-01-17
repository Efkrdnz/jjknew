package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

public class SukunaMarkSlashSingleProcedure {
	public static void execute(LevelAccessor world, Player player, Entity target) {
		if (player == null || target == null || !(world instanceof Level _world))
			return;
		// get output multiplier
		double output_multiplier = ReturnOutputDismantleProcedure.execute(world, player);
		// damage calculation
		float baseDamage = 10.0f;
		float finalDamage = (float) (baseDamage * 1 + output_multiplier);
		// apply damage with correct damage source
		DamageSource damageSource = new DamageSource(_world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), player);
		target.hurt(damageSource, finalDamage);
		// pull target toward player
		Vec3 playerPos = player.position();
		Vec3 targetPos = target.position();
		Vec3 direction = playerPos.subtract(targetPos).normalize();
		double pullStrength = 1.5;
		target.setDeltaMovement(direction.x * pullStrength, direction.y * pullStrength + 0.3, direction.z * pullStrength);
		// spawn NEW shader-based slash visual
		Vec3 slashDir = playerPos.subtract(targetPos).normalize();
		double techniquePower = output_multiplier;
		SpawnDismantleSlashProcedure.execute(_world, player, target, slashDir, techniquePower);
	}
}
