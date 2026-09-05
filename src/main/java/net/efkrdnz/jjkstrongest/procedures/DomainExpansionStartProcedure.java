package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainDefinition;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

public class DomainExpansionStartProcedure {

	private static final DomainDefinition DEFINITION = DomainDefinition.UNLIMITED_VOID;
	private static final float RADIUS = DEFINITION.radius();
	private static final double CAPTURE_RADIUS = 35.0;
	private static final int DURATION_TICKS = DEFINITION.durationTicks();

	public static void execute(LevelAccessor world, double x, double y, double z, Entity caster, int domainType) {
		if (!(world instanceof ServerLevel serverLevel) || caster == null)
			return;

		DomainUVEntity domain = new DomainUVEntity(net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities.DOMAIN_UV.get(), serverLevel);
		domain.setPos(x, y, z);
		domain.setPersistenceRequired();
		domain.setInvulnerable(true);

		// Shape: synced, so the client renders and collides against the same sphere.
		domain.setShellRadius(0.0f);
		domain.setTargetRadius(RADIUS);
		domain.setFloorOffset(DEFINITION.floorOffset());
		domain.setPhase(DomainPhase.EXPANDING);
		domain.setPhaseProgress(0.0f);
		domain.setShellSeed(serverLevel.random.nextInt(100000));

		// Server-side bookkeeping the client has no business knowing.
		CompoundTag data = domain.getPersistentData();
		data.putInt("domainType", domainType);
		data.putString("ownerUUID", caster.getStringUUID());
		data.putInt("duration", DURATION_TICKS);
		data.putInt("expansionTick", 0);

		if (!serverLevel.addFreshEntity(domain))
			return;

		captureEntities(serverLevel, x, y, z);
		caster.teleportTo(caster.getX(), y, caster.getZ());
	}

	/**
	 * Drags everything nearby onto the domain floor as it opens.
	 *
	 * <p>The capture radius is deliberately wider than the shell, so someone standing
	 * just outside gets pulled in rather than sealed out at the boundary.
	 */
	private static void captureEntities(Level level, double centerX, double centerY, double centerZ) {
		double minY = centerY - 15.0;
		double maxY = centerY + CAPTURE_RADIUS;
		AABB searchBox = new AABB(centerX - CAPTURE_RADIUS, centerY - 64.0, centerZ - CAPTURE_RADIUS, centerX + CAPTURE_RADIUS, centerY + 64.0, centerZ + CAPTURE_RADIUS);
		double radiusSq = CAPTURE_RADIUS * CAPTURE_RADIUS;

		for (Entity entity : level.getEntitiesOfClass(Entity.class, searchBox, DomainExpansionStartProcedure::shouldCapture)) {
			double ey = entity.getY();
			if (ey < minY || ey > maxY)
				continue;
			double dx = entity.getX() - centerX;
			double dz = entity.getZ() - centerZ;
			if ((dx * dx + dz * dz) > radiusSq)
				continue;
			entity.teleportTo(entity.getX(), centerY, entity.getZ());
			entity.resetFallDistance();
			if (entity instanceof LivingEntity living)
				living.setDeltaMovement(living.getDeltaMovement().multiply(0.5, 0.0, 0.5));
		}
	}

	private static boolean shouldCapture(Entity entity) {
		if (entity instanceof Player player)
			return !player.isCreative() && !player.isSpectator();
		if (entity instanceof TamableAnimal tamed && tamed.isTame())
			return false;
		return entity instanceof Mob || entity instanceof LivingEntity;
	}
}
