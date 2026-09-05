
package net.efkrdnz.jjkstrongest.entity;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.efkrdnz.jjkstrongest.domain.DomainBarrierKind;
import net.efkrdnz.jjkstrongest.domain.DomainSource;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

public class MalevolentShrineEntity extends PathfinderMob implements DomainSource {

	// Clash state has to reach the client for the versus HUD to draw; persistent
	// data never leaves the server.
	private static final EntityDataAccessor<Float> CLASH_HP = SynchedEntityData.defineId(MalevolentShrineEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> CLASHING = SynchedEntityData.defineId(MalevolentShrineEntity.class, EntityDataSerializers.BOOLEAN);

	public MalevolentShrineEntity(EntityType<MalevolentShrineEntity> type, Level world) {
		super(type, world);
		this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6f);
		xpReward = 0;
		setNoAi(true);
		setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CLASH_HP, 100.0f);
		builder.define(CLASHING, false);
	}

	/** How far the shrine's slashes and damage reach. Matches its tick procedure's radius. */
	public static final double FIELD_RADIUS = 100.0;

	@Override
	public DomainSphere volume() {
		return DomainSphere.openField(this.position(), FIELD_RADIUS);
	}

	@Override
	public DomainBarrierKind barrierKind() {
		// No shell: the shrine covers ground, it does not enclose it.
		return DomainBarrierKind.OPEN;
	}

	public float getClashHP() {
		return this.entityData.get(CLASH_HP);
	}

	public void setClashHP(float hp) {
		this.entityData.set(CLASH_HP, hp);
	}

	public boolean isClashing() {
		return this.entityData.get(CLASHING);
	}

	public void setClashing(boolean clashing) {
		if (this.entityData.get(CLASHING) != clashing)
			this.entityData.set(CLASHING, clashing);
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
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (this.getPersistentData().contains("ownerUUID")) {
			compound.putString("ownerUUID", this.getPersistentData().getString("ownerUUID"));
		}
		if (this.getPersistentData().contains("domainCastY")) {
			compound.putDouble("domainCastY", this.getPersistentData().getDouble("domainCastY"));
		}
		if (this.getPersistentData().contains("domainLifetimeTicks")) {
			compound.putInt("domainLifetimeTicks", this.getPersistentData().getInt("domainLifetimeTicks"));
		}
		if (this.getPersistentData().contains("destructionProgress")) {
			compound.putInt("destructionProgress", this.getPersistentData().getInt("destructionProgress"));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("ownerUUID")) {
			this.getPersistentData().putString("ownerUUID", compound.getString("ownerUUID"));
		}
		if (compound.contains("domainCastY")) {
			this.getPersistentData().putDouble("domainCastY", compound.getDouble("domainCastY"));
		}
		if (compound.contains("domainLifetimeTicks")) {
			this.getPersistentData().putInt("domainLifetimeTicks", compound.getInt("domainLifetimeTicks"));
		}
		if (compound.contains("destructionProgress")) {
			this.getPersistentData().putInt("destructionProgress", compound.getInt("destructionProgress"));
		}
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

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 10);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		return builder;
	}
}
