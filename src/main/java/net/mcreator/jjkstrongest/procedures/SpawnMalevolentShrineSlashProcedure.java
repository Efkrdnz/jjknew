package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.DismantleProjectileEntity;

public class SpawnMalevolentShrineSlashProcedure {
	// spawn massive domain slash effect (no RGB, bigger size)
	public static void execute(Level world, Entity shooter, double x, double y, double z, Vec3 slashDir) {
		if (world == null || shooter == null || world.isClientSide())
			return;
		// normalize direction
		double len = Math.sqrt(slashDir.x * slashDir.x + slashDir.y * slashDir.y + slashDir.z * slashDir.z);
		if (len < 1.0e-6) {
			slashDir = new Vec3(0, 0, 1);
			len = 1;
		}
		double dirX = slashDir.x / len;
		double dirY = slashDir.y / len;
		double dirZ = slashDir.z / len;
		// weighted random: 70% red, 30% white (no RGB for domain)
		int styleRoll = world.random.nextInt(100);
		int style;
		if (styleRoll < 30) {
			style = 0; // white
		} else {
			style = 1; // red
		}
		// much bigger size for domain slashes
		float slashLength = 25.0f + world.random.nextFloat() * 10.0f; // 25..35 blocks (huge)
		float slashWidth = 1.5f + world.random.nextFloat() * 1.5f; // 1.5-3 blocks (thick)
		// random roll
		float roll = world.random.nextFloat() * 6.2831853f;
		// seed for noise
		float seed = world.random.nextFloat() * 1000.0f;
		// aura color
		float cr, cg, cb;
		if (style == 0) {
			// white aura
			cr = 1.0f;
			cg = 1.0f;
			cb = 1.0f;
		} else {
			// red aura (deep crimson for malevolent shrine)
			cr = 1.0f;
			cg = 0.1f + world.random.nextFloat() * 0.15f;
			cb = 0.1f + world.random.nextFloat() * 0.15f;
		}
		// create projectile entity
		DismantleProjectileEntity proj = new DismantleProjectileEntity(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), x, y, z, world);
		// initialize projectile
		initArrowProjectile(proj, shooter, 0, true, false, false, AbstractArrow.Pickup.DISALLOWED);
		// set slash parameters
		proj.setSlashParams(slashLength, slashWidth, style, roll, seed, (float) dirX, (float) dirY, (float) dirZ, cr, cg, cb);
		// position at spawn location
		proj.setPos(x, y, z);
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
