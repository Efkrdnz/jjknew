
package net.efkrdnz.jjkstrongest.entity;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

public class BFEntityEntity extends PathfinderMob {

	public BFEntityEntity(EntityType<BFEntityEntity> type, Level world) {
		super(type, world);
		this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6f);
		xpReward = 0;
		setNoAi(false);
		setPersistenceRequired();
		this.moveControl = new FlyingMoveControl(this, 10, true);
	}


	@Override
	protected PathNavigation createNavigation(Level world) {
		return new FlyingPathNavigation(this, world);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(5, new FloatGoal(this));
	}


	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
	}

	@Override
	public boolean causeFallDamage(float l, float d, DamageSource source) {
		return false;
	}

	@Override
	public boolean hurt(DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.getDirectEntity() instanceof AbstractArrow)
			return false;
		if (damagesource.getDirectEntity() instanceof Player)
			return false;
		if (damagesource.getDirectEntity() instanceof ThrownPotion || damagesource.getDirectEntity() instanceof AreaEffectCloud)
			return false;
		if (damagesource.is(DamageTypes.FALL))
			return false;
		if (damagesource.is(DamageTypes.CACTUS))
			return false;
		if (damagesource.is(DamageTypes.DROWN))
			return false;
		if (damagesource.is(DamageTypes.LIGHTNING_BOLT))
			return false;
		if (damagesource.is(DamageTypes.EXPLOSION) || damagesource.is(DamageTypes.PLAYER_EXPLOSION))
			return false;
		if (damagesource.is(DamageTypes.TRIDENT))
			return false;
		if (damagesource.is(DamageTypes.FALLING_ANVIL))
			return false;
		if (damagesource.is(DamageTypes.DRAGON_BREATH))
			return false;
		if (damagesource.is(DamageTypes.WITHER) || damagesource.is(DamageTypes.WITHER_SKULL))
			return false;
		return super.hurt(damagesource, amount);
	}

	@Override
	public boolean ignoreExplosion(net.minecraft.world.level.Explosion explosion) {
		return true;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entityIn) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
	}

	@Override
	public void setNoGravity(boolean ignored) {
		super.setNoGravity(true);
	}

	public void aiStep() {
		super.aiStep();
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();
		// auto-remove after 36 ticks (1.8 seconds)
		if (this.tickCount >= 36) {
			this.discard();
		}
		// SERVER-SIDE ONLY: track and follow target
		if (!this.level().isClientSide()) {
			// try to find target on first 3 ticks (gives time for entity to spawn properly)
			if (this.tickCount <= 3 && this.getPersistentData().getString("attached_entity_uuid").isEmpty()) {
				net.minecraft.world.phys.AABB searchBox = this.getBoundingBox().inflate(3.0);
				java.util.List<net.minecraft.world.entity.LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, searchBox);
				// find closest entity that isn't this entity
				net.minecraft.world.entity.LivingEntity closest = null;
				double closestDist = Double.MAX_VALUE;
				for (net.minecraft.world.entity.LivingEntity entity : nearbyEntities) {
					if (entity != this && entity.isAlive()) {
						double dist = this.distanceToSqr(entity);
						if (dist < closestDist) {
							closestDist = dist;
							closest = entity;
						}
					}
				}
				if (closest != null) {
					this.getPersistentData().putString("attached_entity_uuid", closest.getStringUUID());
				}
			}
			// follow attached entity every tick
			String attachedUUID = this.getPersistentData().getString("attached_entity_uuid");
			if (!attachedUUID.isEmpty()) {
				// search in larger area (target might have been knocked far)
				net.minecraft.world.phys.AABB searchBox = this.getBoundingBox().inflate(64.0);
				java.util.List<net.minecraft.world.entity.LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, searchBox);
				for (net.minecraft.world.entity.LivingEntity entity : nearbyEntities) {
					if (entity.getStringUUID().equals(attachedUUID)) {
						if (entity.isAlive()) {
							// teleport to entity's center position
							net.minecraft.world.phys.Vec3 targetPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
							this.setPos(targetPos.x, targetPos.y, targetPos.z);
						} else {
							// target died, stop following
							this.getPersistentData().remove("attached_entity_uuid");
						}
						break;
					}
				}
			}
		}
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 10);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.FLYING_SPEED, 0.3);
		return builder;
	}
}
