
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import net.efkrdnz.jjkstrongest.domain.DomainBarrierKind;
import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.domain.DomainSource;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.procedures.DomainUVEntityTickProcedure;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

public class DomainUVEntity extends PathfinderMob implements DomainSource {

	// The shape of the domain, sent to clients by vanilla's entity tracker. Before
	// this the numbers lived only in server-side persistent data, so every client
	// visual had to guess the phase from the entity's own tick count and the clash
	// HUD — which reads these on the client — could never draw at all.
	private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> TARGET_RADIUS = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> PHASE_PROGRESS = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> FLOOR_OFFSET = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> SHELL_SEED = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> CLASH_HP = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> CLASHING = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.BOOLEAN);
	// How much of the barrier is left, 0..1. Drives the HUD and the shader's crack density
	// even before the per-cell grid arrives.
	private static final EntityDataAccessor<Float> SHELL_INTEGRITY = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> BREACHES = SynchedEntityData.defineId(DomainUVEntity.class, EntityDataSerializers.INT);

	private DomainShell shell;

	public static final float DEFAULT_RADIUS = 30.0f;
	public static final float DEFAULT_FLOOR_OFFSET = -1.0f;

	public DomainUVEntity(EntityType<DomainUVEntity> type, Level world) {
		super(type, world);
		this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6f);
		xpReward = 0;
		setNoAi(true);
		setPersistenceRequired();
		this.moveControl = new FlyingMoveControl(this, 10, true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(RADIUS, 0.0f);
		builder.define(TARGET_RADIUS, DEFAULT_RADIUS);
		builder.define(PHASE, DomainPhase.EXPANDING.ordinal());
		builder.define(PHASE_PROGRESS, 0.0f);
		builder.define(FLOOR_OFFSET, DEFAULT_FLOOR_OFFSET);
		builder.define(SHELL_SEED, 0);
		builder.define(CLASH_HP, 100.0f);
		builder.define(CLASHING, false);
		builder.define(SHELL_INTEGRITY, 1.0f);
		builder.define(BREACHES, 0);
	}

	/**
	 * The domain's shape, as both sides see it. This is the single source of truth
	 * the renderer, the collision hook, the carve and the fog all read.
	 */
	public DomainSphere sphere() {
		return new DomainSphere(this.position(), getShellRadius(), this.getY() + getFloorOffset(), getPhase(), getPhaseProgress());
	}

	@Override
	public DomainSphere volume() {
		return sphere();
	}

	@Override
	public DomainBarrierKind barrierKind() {
		return DomainBarrierKind.CLOSED;
	}

	/**
	 * The shell's per-direction integrity.
	 *
	 * <p>Both sides carry one. The server drives it; the client's copy is filled from
	 * {@code DomainShellSyncPacket} and has to exist because collision consults it — a
	 * client that did not know where the holes were would refuse to walk through one the
	 * server is happy to let it through.
	 */
	@Override
	public DomainShell shell() {
		if (this.shell == null)
			this.shell = new DomainShell();
		return this.shell;
	}

	public float getShellRadius() {
		return this.entityData.get(RADIUS);
	}

	public void setShellRadius(float radius) {
		this.entityData.set(RADIUS, radius);
	}

	public float getTargetRadius() {
		return this.entityData.get(TARGET_RADIUS);
	}

	public void setTargetRadius(float radius) {
		this.entityData.set(TARGET_RADIUS, radius);
	}

	public DomainPhase getPhase() {
		return DomainPhase.byOrdinal(this.entityData.get(PHASE));
	}

	public void setPhase(DomainPhase phase) {
		this.entityData.set(PHASE, phase.ordinal());
	}

	public float getPhaseProgress() {
		return this.entityData.get(PHASE_PROGRESS);
	}

	public void setPhaseProgress(float progress) {
		this.entityData.set(PHASE_PROGRESS, progress);
	}

	public float getFloorOffset() {
		return this.entityData.get(FLOOR_OFFSET);
	}

	public void setFloorOffset(float offset) {
		this.entityData.set(FLOOR_OFFSET, offset);
	}

	public int getShellSeed() {
		return this.entityData.get(SHELL_SEED);
	}

	public void setShellSeed(int seed) {
		this.entityData.set(SHELL_SEED, seed);
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

	public float getShellIntegrity() {
		return this.entityData.get(SHELL_INTEGRITY);
	}

	/** Written once a tick from the shell; only sent when it actually moves. */
	public void setShellIntegrity(float integrity) {
		if (Math.abs(this.entityData.get(SHELL_INTEGRITY) - integrity) > 0.002f)
			this.entityData.set(SHELL_INTEGRITY, integrity);
		int breaches = this.shell == null ? 0 : this.shell.breachCount();
		if (this.entityData.get(BREACHES) != breaches)
			this.entityData.set(BREACHES, breaches);
	}

	public int getBreachCount() {
		return this.entityData.get(BREACHES);
	}


	@Override
	protected PathNavigation createNavigation(Level world) {
		return new FlyingPathNavigation(this, world);
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

	// Vanilla syncs the shape to clients but does not persist it, so a domain that
	// survives a reload would come back with a zero radius and no collision.
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat("shellRadius", getShellRadius());
		compound.putFloat("targetRadius", getTargetRadius());
		compound.putInt("domainPhase", getPhase().ordinal());
		compound.putFloat("phaseProgress", getPhaseProgress());
		compound.putFloat("floorOffset", getFloorOffset());
		compound.putInt("shellSeed", getShellSeed());
		compound.putFloat("clashHP", getClashHP());
		if (this.shell != null)
			compound.put("shell", this.shell.save());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("shellRadius"))
			setShellRadius(compound.getFloat("shellRadius"));
		if (compound.contains("targetRadius"))
			setTargetRadius(compound.getFloat("targetRadius"));
		if (compound.contains("domainPhase"))
			setPhase(DomainPhase.byOrdinal(compound.getInt("domainPhase")));
		if (compound.contains("phaseProgress"))
			setPhaseProgress(compound.getFloat("phaseProgress"));
		if (compound.contains("floorOffset"))
			setFloorOffset(compound.getFloat("floorOffset"));
		if (compound.contains("shellSeed"))
			setShellSeed(compound.getInt("shellSeed"));
		if (compound.contains("clashHP"))
			setClashHP(compound.getFloat("clashHP"));
		if (compound.contains("shell")) {
			DomainShell restored = new DomainShell();
			restored.load(compound.getCompound("shell"));
			this.shell = restored;
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
	public void baseTick() {
		super.baseTick();
		DomainUVEntityTickProcedure.execute(this.level(), this);
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
