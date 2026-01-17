package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.DismantleProjectileEntity;

public class SpawnDismantleSlashProcedure {
	// spawn visual slash effect at target position
	public static void execute(Level world, Entity shooter, Entity target, Vec3 slashDir, double techniquePower) {
		if (world == null || shooter == null || target == null || world.isClientSide())
			return;
		// normalize direction (avoid NaN)
		double len = Math.sqrt(slashDir.x * slashDir.x + slashDir.y * slashDir.y + slashDir.z * slashDir.z);
		if (len < 1.0e-6) {
			slashDir = new Vec3(0, 0, 1);
			len = 1;
		}
		double dirX = slashDir.x / len;
		double dirY = slashDir.y / len;
		double dirZ = slashDir.z / len;
		// ---- STYLE 0/1/2 ----
		// Weighted random: 70% red, 20% white, 10% RGB
		int styleRoll = world.random.nextInt(100);
		int style;
		if (styleRoll < 1) {
			// 10% chance - Style 2 (iridescent RGB)
			style = 2;
		} else if (styleRoll < 30) {
			// 20% chance - Style 0 (white)
			style = 0;
		} else {
			// 70% chance - Style 1 (red)
			style = 1;
		}
		// ---- SIZE ----
		// Fixed size (same as test item) - no technique power scaling
		float slashLength = 6.0f + world.random.nextFloat() * 10.0f; // 6..16 blocks
		float slashWidth = 0.45f + world.random.nextFloat() * 0.40f; // 0.25..0.65 blocks
		// ---- ROLL (rotate within the slash plane) ----
		float roll = world.random.nextFloat() * 6.2831853f;
		// ---- SEED for noise/shimmer ----
		float seed = world.random.nextFloat() * 1000.0f;
		// ---- AURA COLOR ----
		float cr, cg, cb;
		if (style == 0) {
			// white aura
			cr = 0.9f;
			cg = 0.9f;
			cb = 0.9f;
		} else if (style == 1) {
			// red aura (most common)
			cr = 1.0f;
			cg = 0.15f + world.random.nextFloat() * 0.20f;
			cb = 0.15f + world.random.nextFloat() * 0.20f;
		} else {
			// style 2: full random RGB (rare)
			cr = 0.9f;
			cg = 0.9f;
			cb = 0.9f;
		}
		// spawn projectile at target position
		Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
		// create projectile entity
		DismantleProjectileEntity proj = new DismantleProjectileEntity(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), targetPos.x, targetPos.y, targetPos.z, world);
		// initialize projectile settings
		initArrowProjectile(proj, shooter, 0, true, false, false, AbstractArrow.Pickup.DISALLOWED);
		// set all slash visual parameters (synced to client)
		proj.setSlashParams(slashLength, slashWidth, style, roll, seed, (float) dirX, (float) dirY, (float) dirZ, cr, cg, cb);
		// position at target and don't move (stationary)
		proj.setPos(targetPos.x, targetPos.y, targetPos.z);
		// Don't call shoot() - keeps it stationary at spawn location
		// add to world
		world.addFreshEntity(proj);
	}

	// initialize arrow projectile settings
	private static AbstractArrow initArrowProjectile(AbstractArrow entityToSpawn, Entity shooter, float damage, boolean silent, boolean fire, boolean particles, AbstractArrow.Pickup pickup) {
		entityToSpawn.setOwner(shooter);
		entityToSpawn.setBaseDamage(damage);
		if (silent)
			entityToSpawn.setSilent(true);
		if (fire)
			entityToSpawn.setSecondsOnFire(100);
		if (particles)
			entityToSpawn.setCritArrow(false);
		entityToSpawn.pickup = pickup;
		return entityToSpawn;
	}
}
