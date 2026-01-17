package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;
import net.mcreator.jjkstrongest.Freezeframe;

import java.util.List;

public class WorldSlashExecuteProcedure {
	// executes world cutting slash using triangle plane between 3 points
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null || world == null)
			return;
		// === IMPACT FRAME FOR WORLD SLASH ===
		if (world.isClientSide()) {
			TriggerChargedImpactProcedure.execute((Level) world, entity, 3, 1.0f, 2.0f, 2.5f);
			// FREEZE FRAME on client after 2 ticks (100ms)
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
			new java.util.Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					// execute on main thread
					mc.execute(() -> {
						Freezeframe.execute(75);
					});
				}
			}, 100); // 100ms = ~2 ticks
			TriggerScreenShakeProcedure.execute((Level) world, entity, 10, 3.0f);
		}
		// MARK WORLD SLASH AS ACTIVE RIGHT AT THE START
		entity.getPersistentData().putLong("world_slash_tick", ((Level) world).getGameTime());
		// get saved coordinates from player variables
		double x1 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_x1;
		double y1 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_y1;
		double z1 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_z1;
		double x2 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_x2;
		double y2 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_y2;
		double z2 = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_z2;
		// point 3 is player eye position
		double x3 = entity.getX();
		double y3 = entity.getY() + entity.getEyeHeight();
		double z3 = entity.getZ();
		Vec3 point1 = new Vec3(x1, y1, z1);
		Vec3 point2 = new Vec3(x2, y2, z2);
		Vec3 point3 = new Vec3(x3, y3, z3);
		// calculate plane normal using cross product
		Vec3 v1 = point2.subtract(point1);
		Vec3 v2 = point3.subtract(point1);
		Vec3 normal = v1.cross(v2).normalize();
		// calculate bounding box containing all three points
		double minX = Math.min(Math.min(x1, x2), x3);
		double minY = Math.min(Math.min(y1, y2), y3);
		double minZ = Math.min(Math.min(z1, z2), z3);
		double maxX = Math.max(Math.max(x1, x2), x3);
		double maxY = Math.max(Math.max(y1, y2), y3);
		double maxZ = Math.max(Math.max(z1, z2), z3);
		// expand bounding box slightly for particles and entities
		minX -= 2;
		minY -= 2;
		minZ -= 2;
		maxX += 2;
		maxY += 2;
		maxZ += 2;
		// spawn minimal particles along triangle edges
		if (world instanceof ServerLevel serverLevel) {
			spawnEdgeParticles(serverLevel, point1, point2);
			spawnEdgeParticles(serverLevel, point2, point3);
			spawnEdgeParticles(serverLevel, point3, point1);
		}
		double planeThickness = 0.8;
		// wcs power
		double wcs_pwr = (entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).wcs_power;
		if (wcs_pwr == 100) {
			planeThickness = 1.5;
		}
		// break blocks within triangle
		for (int x = (int) Math.floor(minX); x <= (int) Math.ceil(maxX); x++) {
			for (int y = (int) Math.floor(minY); y <= (int) Math.ceil(maxY); y++) {
				for (int z = (int) Math.floor(minZ); z <= (int) Math.ceil(maxZ); z++) {
					BlockPos pos = new BlockPos(x, y, z);
					Vec3 blockCenter = new Vec3(x + 0.5, y + 0.5, z + 0.5);
					// check if point is inside triangle and near plane
					if (isPointInTriangle(blockCenter, point1, point2, point3, normal, planeThickness)) {
						BlockState state = world.getBlockState(pos);
						// check if block is breakable (hardness != -1)
						if (state.getDestroySpeed(world, pos) != -1 && !state.isAir()) {
							if (world instanceof ServerLevel _level)
								_level.getServer().getCommands().performPrefixedCommand(
										new CommandSourceStack(CommandSource.NULL, new Vec3(x + 0.5, y + 0.5, z + 0.5), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
										"setblock " + x + " " + y + " " + z + " air replace");
						}
					}
				}
			}
		}
		// damage entities within triangle
		AABB entityBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		List<LivingEntity> entities = ((Level) world).getEntitiesOfClass(LivingEntity.class, entityBox, e -> e != entity && e.isAlive());
		for (LivingEntity target : entities) {
			Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
			if (isPointInTriangle(targetPos, point1, point2, point3, normal, planeThickness * 2)) {
				// devastating damage
				target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), entity), 50.0F);
				// severe debuffs
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
				// knockback along plane normal
				if (world instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetPos.x, targetPos.y, targetPos.z, 5, 0.3, 0.3, 0.3, 0.1);
				}
			}
		}
		// play menacing sounds at all three points
		Vec3 centerSound = point1.add(point2).add(point3).scale(1.0 / 3.0);
		((Level) world).playSound(null, new BlockPos((int) centerSound.x, (int) centerSound.y, (int) centerSound.z), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 0.5F);
		((Level) world).playSound(null, new BlockPos((int) centerSound.x, (int) centerSound.y, (int) centerSound.z), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 2.0F, 0.7F);
	}

	// spawn minimal particles along edge between two points
	private static void spawnEdgeParticles(ServerLevel world, Vec3 start, Vec3 end) {
		int steps = 15;
		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			Vec3 pos = start.add(end.subtract(start).scale(t));
			world.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
		}
	}

	// check if point is inside triangle using barycentric coordinates and near plane
	private static boolean isPointInTriangle(Vec3 point, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 normal, double thickness) {
		// check distance from plane
		double distanceFromPlane = Math.abs(point.subtract(p1).dot(normal));
		if (distanceFromPlane > thickness) {
			return false;
		}
		// project point onto plane
		Vec3 projectedPoint = point.subtract(normal.scale(point.subtract(p1).dot(normal)));
		// calculate barycentric coordinates
		Vec3 v0 = p3.subtract(p1);
		Vec3 v1 = p2.subtract(p1);
		Vec3 v2 = projectedPoint.subtract(p1);
		double dot00 = v0.dot(v0);
		double dot01 = v0.dot(v1);
		double dot02 = v0.dot(v2);
		double dot11 = v1.dot(v1);
		double dot12 = v1.dot(v2);
		double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
		// check if point is inside triangle
		return (u >= 0) && (v >= 0) && (u + v <= 1);
	}
}
