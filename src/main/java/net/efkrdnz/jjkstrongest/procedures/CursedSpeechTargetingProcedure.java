package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class CursedSpeechTargetingProcedure {
	// call this from your keybind procedures with command name
	public static void execute(LevelAccessor world, Entity entity, String command) {
		if (entity == null || world == null)
			return;
		Vec3 eyePos = entity.getEyePosition(1.0F);
		Vec3 lookVec = entity.getLookAngle();
		double range = 30;
		double coneAngle = 35;
		AABB searchBox = new AABB(eyePos.x - range, eyePos.y - range, eyePos.z - range, eyePos.x + range, eyePos.y + range, eyePos.z + range);
		List<Entity> validTargets = new java.util.ArrayList<>();
		for (Entity foundEntity : world.getEntitiesOfClass(LivingEntity.class, searchBox)) {
			if (foundEntity == entity || !foundEntity.isAlive())
				continue;
			Vec3 toEntity = foundEntity.position().add(0, foundEntity.getBbHeight() / 2, 0).subtract(eyePos);
			double distance = toEntity.length();
			if (distance > range)
				continue;
			double dotProduct = lookVec.normalize().dot(toEntity.normalize());
			double angleToEntity = Math.toDegrees(Math.acos(dotProduct));
			if (angleToEntity <= coneAngle) {
				validTargets.add(foundEntity);
			}
		}
		List<Entity> finalTargets = new java.util.ArrayList<>();
		if (entity.isCrouching()) {
			finalTargets = validTargets;
		} else {
			Entity closest = null;
			double smallestAngle = Double.MAX_VALUE;
			for (Entity foundEntity : validTargets) {
				Vec3 toEntity = foundEntity.position().add(0, foundEntity.getBbHeight() / 2, 0).subtract(eyePos);
				double dotProduct = lookVec.normalize().dot(toEntity.normalize());
				double angle = Math.acos(dotProduct);
				if (angle < smallestAngle) {
					smallestAngle = angle;
					closest = foundEntity;
				}
			}
			if (closest != null) {
				finalTargets.add(closest);
			}
		}
		// execute command-specific procedure for each target
		for (Entity target : finalTargets) {
			executeCommandProcedure(world, entity, target, command);
		}
	}

	// routes to specific command procedures
	private static void executeCommandProcedure(LevelAccessor world, Entity source, Entity target, String command) {
		switch (command.toLowerCase()) {
			case "stop" :
				CursedSpeechStopEffectProcedure.execute(world, source, target);
				break;
			case "die" :
				CursedSpeechDieEffectProcedure.execute(world, source, target);
				break;
			case "blast" :
				CursedSpeechBlastEffectProcedure.execute(world, source, target);
				break;
			case "crush" :
				CursedSpeechCrushEffectProcedure.execute(world, source, target);
				break;
			case "burst" :
				CursedSpeechBurstEffectProcedure.execute(world, source, target);
				break;
			case "sleep" :
				CursedSpeechSleepEffectProcedure.execute(world, source, target);
				break;
			case "flee" :
				CursedSpeechFleeEffectProcedure.execute(world, source, target);
				break;
			case "rot" :
				CursedSpeechRotEffectProcedure.execute(world, source, target);
				break;
			case "twist" :
				CursedSpeechTwistEffectProcedure.execute(world, source, target);
				break;
			case "burn" :
				CursedSpeechBurnEffectProcedure.execute(world, source, target);
				break;
			case "fall" :
				CursedSpeechFallEffectProcedure.execute(world, source, target);
				break;
			case "spit" :
				CursedSpeechSpitEffectProcedure.execute(world, source, target);
				break;
			case "pull" :
				CursedSpeechPullEffectProcedure.execute(world, source, target);
				break;
			case "shrink" :
				CursedSpeechShrinkEffectProcedure.execute(world, source, target);
				break;
			case "weep" :
				CursedSpeechWeepEffectProcedure.execute(world, source, target);
				break;
			case "kneel" :
				CursedSpeechKneelEffectProcedure.execute(world, source, target);
				break;
		}
	}
}
