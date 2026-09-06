
package net.efkrdnz.jjkstrongest.entity;

import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.GeoEntity;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;

import net.efkrdnz.jjkstrongest.procedures.MahoragaOnEntityTickUpdateProcedure;
import net.efkrdnz.jjkstrongest.procedures.MahoragaEffectAdaptationEventsProcedure;
import net.efkrdnz.jjkstrongest.domain.DomainSuppression;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

public class MahoragaEntity extends Monster implements GeoEntity {
	public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> DATA_DBG_STATE = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> DATA_DBG_TARGET = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> DATA_DBG_COOLDOWNS = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> DATA_DBG_ADAPT = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> DATA_DBG_EXTRA = SynchedEntityData.defineId(MahoragaEntity.class, EntityDataSerializers.STRING);
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private boolean swinging;
	private boolean lastloop;
	private long lastSwing;
	public String animationprocedure = "empty";
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.YELLOW, ServerBossEvent.BossBarOverlay.PROGRESS);


	public MahoragaEntity(EntityType<MahoragaEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.6f);
		setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SHOOT, false);
		builder.define(ANIMATION, "undefined");
		builder.define(TEXTURE, "mahoraga");
		builder.define(DATA_DBG_STATE, "");
		builder.define(DATA_DBG_TARGET, "");
		builder.define(DATA_DBG_COOLDOWNS, "");
		builder.define(DATA_DBG_ADAPT, "");
		builder.define(DATA_DBG_EXTRA, "");
	}

	public void setTexture(String texture) {
		this.entityData.set(TEXTURE, texture);
	}

	public String getTexture() {
		return this.entityData.get(TEXTURE);
	}


	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Villager.class, false, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Pillager.class, false, false));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1));
		this.targetSelector.addGoal(6, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(8, new FloatGoal(this));
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
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.FALL))
			return false;
		// While suppressed by Unlimited Void, Mahoraga takes 3× damage —
		// he is completely overwhelmed until his adaptation completes.
		if (this.hasEffect(net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects.INFORMATION_OVERLOAD))
			amount *= 3.0f;
		return super.hurt(source, amount);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putString("Texture", this.getTexture());
		compound.putString("DataDBG_STATE", this.entityData.get(DATA_DBG_STATE));
		compound.putString("DataDBG_TARGET", this.entityData.get(DATA_DBG_TARGET));
		compound.putString("DataDBG_COOLDOWNS", this.entityData.get(DATA_DBG_COOLDOWNS));
		compound.putString("DataDBG_ADAPT", this.entityData.get(DATA_DBG_ADAPT));
		compound.putString("DataDBG_EXTRA", this.entityData.get(DATA_DBG_EXTRA));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Texture"))
			this.setTexture(compound.getString("Texture"));
		if (compound.contains("DataDBG_STATE"))
			this.entityData.set(DATA_DBG_STATE, compound.getString("DataDBG_STATE"));
		if (compound.contains("DataDBG_TARGET"))
			this.entityData.set(DATA_DBG_TARGET, compound.getString("DataDBG_TARGET"));
		if (compound.contains("DataDBG_COOLDOWNS"))
			this.entityData.set(DATA_DBG_COOLDOWNS, compound.getString("DataDBG_COOLDOWNS"));
		if (compound.contains("DataDBG_ADAPT"))
			this.entityData.set(DATA_DBG_ADAPT, compound.getString("DataDBG_ADAPT"));
		if (compound.contains("DataDBG_EXTRA"))
			this.entityData.set(DATA_DBG_EXTRA, compound.getString("DataDBG_EXTRA"));
	}

	@Override
	public void baseTick() {
		super.baseTick();
		MahoragaOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
		// Both of these come after the state machine, not before it: whatever move he was
		// committed to when the domain landed is still written in his persistent data, and
		// the suppression has to be the last word on it or it resumes the instant the effect
		// wears off. Running the adaptation clock first means the tick that completes it is
		// also the tick the freeze lifts, rather than leaving him locked for one more.
		MahoragaEffectAdaptationEventsProcedure.tickVoidAdaptation(this);
		DomainSuppression.tick(this);
		// The boss bar is refreshed from customServerAiStep() as well, which vanilla reaches
		// only through serverAiStep() — the very thing isImmobile() skips. Without this line
		// it would freeze along with him.
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
		this.refreshDimensions();
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose p_33597_) {
		return super.getDefaultDimensions(p_33597_).scale((float) 1.5);
	}

	@Override
	public boolean canChangeDimensions(net.minecraft.world.level.Level oldLevel, net.minecraft.world.level.Level newLevel) {
		return false;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	/**
	 * The freeze itself. {@code aiStep()} reads this and skips {@code serverAiStep()} entirely,
	 * which is what finally stops the {@code MeleeAttackGoal} and {@code RandomStrollGoal}
	 * above — they tick after {@code baseTick()}, so no guard written there could ever reach
	 * them. It leaves {@code travel()} alone, so gravity, drag and collision keep working and a
	 * Mahoraga caught mid sky-dive still falls.
	 */
	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || DomainSuppression.isSuppressed(this);
	}

	@Override
	public void customServerAiStep() {
		super.customServerAiStep();
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.4);
		builder = builder.add(Attributes.MAX_HEALTH, 100);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 64);
		return builder;
	}

	private PlayState movementPredicate(AnimationState event) {
		if (this.animationprocedure.equals("empty")) {
			if ((event.isMoving() || !(event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F)) && this.onGround() && !this.isAggressive()) {
				return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
			}
			if (!this.onGround()) {
				return event.setAndContinue(RawAnimation.begin().thenLoop("air"));
			}
			if (this.isAggressive() && event.isMoving()) {
				return event.setAndContinue(RawAnimation.begin().thenLoop("run"));
			}
			return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
		}
		return PlayState.STOP;
	}

	String prevAnim = "empty";

	private PlayState procedurePredicate(AnimationState event) {
		if (!animationprocedure.equals("empty") && event.getController().getAnimationState() == AnimationController.State.STOPPED || (!this.animationprocedure.equals(prevAnim) && !this.animationprocedure.equals("empty"))) {
			if (!this.animationprocedure.equals(prevAnim))
				event.getController().forceAnimationReset();
			event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
			if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
				this.animationprocedure = "empty";
				event.getController().forceAnimationReset();
			}
		} else if (animationprocedure.equals("empty")) {
			prevAnim = "empty";
			return PlayState.STOP;
		}
		prevAnim = this.animationprocedure;
		return PlayState.CONTINUE;
	}

	@Override
	protected void tickDeath() {
		++this.deathTime;
		if (this.deathTime == 20) {
			this.remove(MahoragaEntity.RemovalReason.KILLED);
			this.dropExperience(null);
		}
	}

	public String getSyncedAnimation() {
		return this.entityData.get(ANIMATION);
	}

	public void setAnimation(String animation) {
		this.entityData.set(ANIMATION, animation);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar data) {
		data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
		data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
}
