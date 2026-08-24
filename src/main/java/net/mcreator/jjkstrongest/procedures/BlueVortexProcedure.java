package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.mcreator.jjkstrongest.entity.BlueVortexEntity;
import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BlueVortexProcedure {
	private static final double HAND_DISTANCE = 1.25;
	private static final double RADIUS = 11.0;
	private static final int MAX_HOLD_TICKS = 120;

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			tickPlayer(event.player.level(), event.player);
		}
	}

	public static boolean start(Entity entity) {
		if (entity == null || !isGojoBlue(entity))
			return false;
		entity.getPersistentData().putBoolean("blue_vortex_active", true);
		entity.getPersistentData().putInt("blue_vortex_ticks", 0);
		entity.getPersistentData().putString("chanting", "blue_vortex");
		PlayArmAnimationProcedure.execute(entity, "blue_vortex", true);
		if (!entity.level().isClientSide()) {
			ensureAnchor(entity.level(), entity);
			playSound(entity.level(), entity.blockPosition(), "block.beacon.activate", 0.7F, 1.6F);
		}
		return true;
	}

	public static boolean stop(Entity entity) {
		if (entity == null || !isGojoBlue(entity))
			return false;
		if (!entity.getPersistentData().getBoolean("blue_vortex_active") && !"blue_vortex".equals(entity.getPersistentData().getString("chanting")))
			return false;
		entity.getPersistentData().putBoolean("blue_vortex_active", false);
		entity.getPersistentData().putInt("blue_vortex_ticks", 0);
		if ("blue_vortex".equals(entity.getPersistentData().getString("chanting")))
			entity.getPersistentData().putString("chanting", "");
		ReleaseArmAnimationProcedure.execute(entity);
		if (!entity.level().isClientSide()) {
			removeAnchors(entity.level(), entity);
			playSound(entity.level(), entity.blockPosition(), "block.beacon.deactivate", 0.55F, 1.8F);
		}
		return true;
	}

	public static void tickAnchor(LevelAccessor world, Entity anchor) {
		if (!(world instanceof ServerLevel serverLevel) || anchor == null)
			return;
		Entity owner = findOwner(serverLevel, anchor);
		if (!(owner instanceof Player player) || !owner.isAlive() || !owner.getPersistentData().getBoolean("blue_vortex_active") || !isGojoBlue(owner)) {
			anchor.discard();
			return;
		}
		moveAnchor(anchor, player);
		applyVortex(serverLevel, player, anchor);
	}

	private static void tickPlayer(LevelAccessor world, Entity entity) {
		if (entity == null || !entity.getPersistentData().getBoolean("blue_vortex_active"))
			return;
		if (!isGojoBlue(entity)) {
			stop(entity);
			return;
		}
		int ticks = entity.getPersistentData().getInt("blue_vortex_ticks") + 1;
		entity.getPersistentData().putInt("blue_vortex_ticks", ticks);
		if (ticks > MAX_HOLD_TICKS) {
			stop(entity);
			return;
		}
		if (!world.isClientSide()) {
			Entity anchor = ensureAnchor(entity.level(), entity);
			if (anchor != null) {
				moveAnchor(anchor, entity);
				applyVortex((ServerLevel) entity.level(), entity, anchor);
			}
		}
	}

	private static boolean isGojoBlue(Entity entity) {
		return "gojo_blue".equals(entity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables()).current_moveset);
	}

	private static Entity ensureAnchor(Level world, Entity owner) {
		if (!(world instanceof ServerLevel serverLevel))
			return null;
		for (BlueVortexEntity existing : serverLevel.getEntitiesOfClass(BlueVortexEntity.class, AABB.ofSize(owner.position(), 80, 80, 80), e -> owner.getStringUUID().equals(e.getPersistentData().getString("ownerUUID")))) {
			return existing;
		}
		BlueVortexEntity anchor = new BlueVortexEntity(JjkStrongestModEntities.BLUE_VORTEX.get(), serverLevel);
		anchor.getPersistentData().putString("ownerUUID", owner.getStringUUID());
		moveAnchor(anchor, owner);
		serverLevel.addFreshEntity(anchor);
		return anchor;
	}

	private static void removeAnchors(Level world, Entity owner) {
		if (!(world instanceof ServerLevel serverLevel))
			return;
		for (BlueVortexEntity existing : serverLevel.getEntitiesOfClass(BlueVortexEntity.class, AABB.ofSize(owner.position(), 120, 120, 120), e -> owner.getStringUUID().equals(e.getPersistentData().getString("ownerUUID")))) {
			existing.discard();
		}
	}

	private static void moveAnchor(Entity anchor, Entity owner) {
		Vec3 look = owner.getLookAngle().normalize();
		Vec3 side = look.cross(new Vec3(0, 1, 0));
		if (side.lengthSqr() < 0.001)
			side = new Vec3(1, 0, 0);
		side = side.normalize();
		Vec3 pos = owner.getEyePosition().add(look.scale(HAND_DISTANCE)).add(side.scale(-0.28)).add(0, -0.18, 0);
		anchor.setPos(pos.x, pos.y, pos.z);
		anchor.setYRot(owner.getYRot());
		anchor.setXRot(owner.getXRot());
	}

	private static void applyVortex(ServerLevel world, Entity owner, Entity anchor) {
		Vec3 center = anchor.position();
		List<Entity> targets = world.getEntities(owner, new AABB(center, center).inflate(RADIUS), e -> shouldPull(e, owner, anchor));
		long time = world.getGameTime();
		for (Entity target : targets) {
			Vec3 toCenter = center.subtract(target.position().add(0, target.getBbHeight() * 0.45, 0));
			double distance = Math.max(0.65, toCenter.length());
			Vec3 pull = toCenter.normalize().scale(Math.min(0.85, 0.12 + (RADIUS - distance) / RADIUS * 0.55));
			if (target instanceof ItemEntity) {
				target.setDeltaMovement(target.getDeltaMovement().scale(0.72).add(pull.scale(1.25)));
			} else if (target instanceof Projectile) {
				target.setDeltaMovement(target.getDeltaMovement().scale(0.35).add(pull.scale(1.45)));
			} else {
				target.setDeltaMovement(target.getDeltaMovement().scale(0.78).add(pull.x, pull.y * 0.65, pull.z));
				if (target instanceof LivingEntity && time % 5 == 0 && distance < 3.2) {
					target.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC), owner), 1.5F);
				}
			}
			target.hurtMarked = true;
		}
	}

	private static boolean shouldPull(Entity target, Entity owner, Entity anchor) {
		if (target == null || target == owner || target == anchor || target.isSpectator())
			return false;
		if (target.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("technique"))))
			return target instanceof Projectile;
		return target instanceof LivingEntity || target instanceof ItemEntity || target instanceof Projectile;
	}

	private static Entity findOwner(ServerLevel world, Entity anchor) {
		try {
			return world.getEntity(UUID.fromString(anchor.getPersistentData().getString("ownerUUID")));
		} catch (Exception ignored) {
			return null;
		}
	}

	private static void playSound(Level world, BlockPos pos, String sound, float volume, float pitch) {
		world.playSound(null, pos, net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(sound)), SoundSource.PLAYERS, volume, pitch);
	}
}
