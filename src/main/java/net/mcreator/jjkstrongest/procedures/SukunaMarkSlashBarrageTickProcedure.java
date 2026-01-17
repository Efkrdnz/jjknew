package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.DismantleProjectileEntity;

import java.util.List;

public class SukunaMarkSlashBarrageTickProcedure {
	public static void execute(Level world, Entity entity) {
		if (entity == null || world.isClientSide())
			return;
		CompoundTag data = entity.getPersistentData();
		if (!data.getBoolean("mark_barrage_active"))
			return;
		// increment timer
		int timer = data.getInt("mark_barrage_timer");
		timer++;
		data.putInt("mark_barrage_timer", timer);
		int interval = data.getInt("mark_barrage_interval");
		// check if it's time for next slash
		if (timer % interval != 0)
			return;
		int count = data.getInt("mark_barrage_count");
		int maxCount = data.getInt("mark_barrage_max");
		if (count >= maxCount) {
			// barrage complete
			data.putBoolean("mark_barrage_active", false);
			return;
		}
		// find attacker
		String attackerUUID = data.getString("mark_barrage_attacker");
		if (attackerUUID.isEmpty()) {
			data.putBoolean("mark_barrage_active", false);
			return;
		}
		Player attacker = null;
		List<? extends Player> players = world.players();
		for (Player p : players) {
			if (p.getStringUUID().equals(attackerUUID)) {
				attacker = p;
				break;
			}
		}
		if (attacker == null || !entity.isAlive()) {
			data.putBoolean("mark_barrage_active", false);
			return;
		}
		double output_multiplier = data.getDouble("mark_barrage_output");
		float damagePerSlash = (float) (2.0 * output_multiplier);
		// apply damage with correct damage source - FIXED
		DamageSource damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), attacker);
		entity.hurt(damageSource, damagePerSlash);
		// CONTINUOUS KNOCKBACK
		Vec3 playerPos = attacker.position();
		Vec3 currentTargetPos = entity.position();
		Vec3 direction = currentTargetPos.subtract(playerPos).normalize();
		double knockbackPerSlash = 0.25;
		double upwardBoost = 0.05;
		Vec3 currentVel = entity.getDeltaMovement();
		entity.setDeltaMovement(currentVel.x + direction.x * knockbackPerSlash, currentVel.y + upwardBoost, currentVel.z + direction.z * knockbackPerSlash);
		// random slash position around target
		double angleH = world.getRandom().nextDouble() * Math.PI * 2;
		double angleV = (world.getRandom().nextDouble() - 0.5) * Math.PI;
		double radius = 0.5 + world.getRandom().nextDouble() * 1.5;
		double offsetX = Math.cos(angleH) * Math.cos(angleV) * radius;
		double offsetY = Math.sin(angleV) * radius;
		double offsetZ = Math.sin(angleH) * Math.cos(angleV) * radius;
		Vec3 slashPos = currentTargetPos.add(offsetX, entity.getBbHeight() / 2 + offsetY, offsetZ);
		// slash direction varies
		Vec3 slashDir;
		if (world.getRandom().nextBoolean()) {
			slashDir = playerPos.subtract(slashPos).normalize();
		} else {
			double randAngleH = world.getRandom().nextDouble() * Math.PI * 2;
			double randAngleV = (world.getRandom().nextDouble() - 0.5) * Math.PI * 0.5;
			slashDir = new Vec3(Math.cos(randAngleH) * Math.cos(randAngleV), Math.sin(randAngleV), Math.sin(randAngleH) * Math.cos(randAngleV)).normalize();
		}
		// SPAWN SLASH ENTITY
		spawnSlashEntity(world, attacker, slashPos, slashDir, output_multiplier);
		// increment count
		count++;
		data.putInt("mark_barrage_count", count);
		// FINAL SLASH: bigger knockback
		if (count >= maxCount) {
			double finalKnockbackStrength = 1.5;
			entity.setDeltaMovement(direction.x * finalKnockbackStrength, 0.6, direction.z * finalKnockbackStrength);
			data.putBoolean("mark_barrage_active", false);
		}
		// prevent invulnerability frames - KEEP THIS
		entity.invulnerableTime = 0;
	}

	// spawn slash entity directly
	private static void spawnSlashEntity(Level world, Player player, Vec3 position, Vec3 slashDir, double output_multiplier) {
		// Weighted random style: 70% red, 20% white, 10% RGB
		int styleRoll = world.random.nextInt(100);
		int style;
		if (styleRoll < 10) {
			style = 2; // RGB
		} else if (styleRoll < 30) {
			style = 0; // white
		} else {
			style = 1; // red
		}
		// random size
		float slashLength = 6.0f + world.random.nextFloat() * 10.0f;
		float slashWidth = 0.25f + world.random.nextFloat() * 0.40f;
		// random roll and seed
		float roll = world.random.nextFloat() * 6.2831853f;
		float seed = world.random.nextFloat() * 1000.0f;
		// color based on style
		float cr, cg, cb;
		if (style == 0) {
			cr = 0.9f;
			cg = 0.9f;
			cb = 0.9f;
		} else if (style == 1) {
			cr = 1.0f;
			cg = 0.15f + world.random.nextFloat() * 0.20f;
			cb = 0.15f + world.random.nextFloat() * 0.20f;
		} else {
			cr = world.random.nextFloat();
			cg = world.random.nextFloat();
			cb = world.random.nextFloat();
		}
		// create projectile entity
		DismantleProjectileEntity proj = new DismantleProjectileEntity(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), position.x, position.y, position.z, world);
		// set parameters
		proj.setSlashParams(slashLength, slashWidth, style, roll, seed, (float) slashDir.x, (float) slashDir.y, (float) slashDir.z, cr, cg, cb);
		proj.setOwner(player);
		proj.setPos(position.x, position.y, position.z);
		// add to world
		world.addFreshEntity(proj);
	}
}
