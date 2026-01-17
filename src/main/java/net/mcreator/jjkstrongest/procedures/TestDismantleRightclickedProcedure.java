package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.entity.DismantleProjectileEntity;

public class TestDismantleRightclickedProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || !(world instanceof Level _world))
			return;
		if (world.isClientSide())
			return;
		// Spawn slash 3 blocks in front of player
		Vec3 lookVec = entity.getViewVector(0.0f);
		Vec3 spawnPos = entity.getEyePosition(0.0f).add(lookVec.scale(0.0));
		// Fixed slash direction - use look vector (slash faces same direction as player)
		Vec3 slashDir = lookVec.normalize();
		// ---- STYLE 0/1/2 ----
		// 0 = clean black slash, faint aura
		// 1 = jagged/irregular
		// 2 = strong iridescent aura (rare)
		int styleRoll = _world.random.nextInt(100); // 0-99
		int style;
		if (styleRoll < 0) {
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
		// length in blocks (visual), width thin
		float slashLength = 6.0f + _world.random.nextFloat() * 10.0f; // 6..16
		float slashWidth = 0.25f + _world.random.nextFloat() * 0.40f; // 0.25..0.65
		// ---- ROLL (rotate within the slash plane) ----
		float roll = _world.random.nextFloat() * 6.2831853f;
		// ---- SEED for noise/shimmer ----
		float seed = _world.random.nextFloat() * 1000.0f;
		// ---- AURA COLOR ----
		float cr, cg, cb;
		if (style == 0) {
			// white aura (30% total - 20% + 10% from style distribution)
			cr = 0.9f;
			cg = 0.9f;
			cb = 0.9f;
		} else if (style == 1) {
			// red aura (70% most common)
			cr = 1.0f;
			cg = 0.15f + _world.random.nextFloat() * 0.20f;
			cb = 0.15f + _world.random.nextFloat() * 0.20f;
		} else {
			// style 2: full random RGB (10% rare)
			cr = _world.random.nextFloat();
			cg = _world.random.nextFloat();
			cb = _world.random.nextFloat();
		}
		// Create slash projectile
		DismantleProjectileEntity slash = new DismantleProjectileEntity(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), spawnPos.x, spawnPos.y, spawnPos.z, _world);
		// Initialize projectile settings
		initArrowProjectile(slash, entity, 0, true, false, false, AbstractArrow.Pickup.DISALLOWED);
		// Set all slash visual parameters
		slash.setSlashParams(slashLength, slashWidth, style, roll, seed, (float) slashDir.x, (float) slashDir.y, (float) slashDir.z, cr, cg, cb);
		slash.setOwner(entity);
		slash.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
		// Don't call shoot() - keeps it stationary at spawn location
		_world.addFreshEntity(slash);
	}

	private static AbstractArrow initArrowProjectile(AbstractArrow entityToSpawn, Entity shooter, float damage, boolean silent, boolean fire, boolean particles, AbstractArrow.Pickup pickup) {
		entityToSpawn.setOwner(shooter);
		entityToSpawn.setBaseDamage(damage);
		if (silent)
			entityToSpawn.setSilent(true);
		if (fire)
			entityToSpawn.setSecondsOnFire(100);
		if (particles)
			entityToSpawn.setCritArrow(true);
		entityToSpawn.pickup = pickup;
		return entityToSpawn;
	}
}
