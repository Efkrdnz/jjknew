package net.efkrdnz.jjkstrongest.entity;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.procedures.SukunaNPCTickProcedure;
import net.efkrdnz.jjkstrongest.domain.DomainSuppression;
import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

public class SukunaEntity extends Monster {
	private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.RED, ServerBossEvent.BossBarOverlay.PROGRESS);


	public SukunaEntity(EntityType<SukunaEntity> type, Level world) {
		super(type, world);
		this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.3f);
		xpReward = 0;
		setNoAi(false);
	}


	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, 10, false, false,
				e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, MahoragaEntity.class, false, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, false, false));
		this.targetSelector.addGoal(4, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(6, new FloatGoal(this));
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
		if (damagesource.is(DamageTypes.FALL))
			return false;
		// While suppressed by Unlimited Void, Sukuna takes 3× damage —
		// he is completely overwhelmed until his domain counter fires.
		if (this.hasEffect(net.efkrdnz.jjkstrongest.init.JjkStrongestModMobEffects.INFORMATION_OVERLOAD))
			amount *= 3.0f;
		// ...and his guard drops with the rest of him. Blocking is armed from in here rather
		// than from the AI, so it is the one piece of him DomainSuppression cannot reach — and
		// a man who cannot move should not be parrying. It also very nearly undid the triple
		// above: three quarters off a tripled hit lands under a normal one, so a frozen Sukuna
		// was harder to kill than an awake one the moment you got three swings in. Reverse
		// cursed technique goes with it; the AI cannot start a burst while frozen, but one
		// already running would have kept mitigating.
		if (DomainSuppression.isSuppressed(this)) {
			this.getPersistentData().putBoolean("is_blocking", false);
			this.getPersistentData().putInt("ai_block_timer", 0);
			// Hits landed on a man who cannot answer them do not add up to a parry the moment
			// the domain drops.
			this.getPersistentData().putInt("consecutive_hits", 0);
			return super.hurt(damagesource, amount);
		}
		// hit counter for block trigger
		long now = this.level().getGameTime();
		long lastHit = this.getPersistentData().getLong("last_hit_time");
		int hits = this.getPersistentData().getInt("consecutive_hits");
		// reset counter if gap between hits is too long
		if (now - lastHit > 40)
			hits = 0;
		hits++;
		this.getPersistentData().putLong("last_hit_time", now);
		this.getPersistentData().putInt("consecutive_hits", hits);
		// trigger block on 3 consecutive hits
		if (hits >= 3 && !this.getPersistentData().getBoolean("is_blocking")) {
			this.getPersistentData().putBoolean("is_blocking", true);
			this.getPersistentData().putInt("ai_block_timer", 40);
			this.getPersistentData().putInt("consecutive_hits", 0);
		}
		// 75% damage reduction while blocking
		if (this.getPersistentData().getBoolean("is_blocking"))
			amount *= 0.25f;
		// 75% damage reduction while using rct
		if ("rct".equals(this.getPersistentData().getString("ai_action")))
			amount *= 0.25f;
		return super.hurt(damagesource, amount);
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
	 * Caught in the Void, Sukuna stops.
	 *
	 * <p>The freeze itself is {@link #isImmobile()}; what has to happen here is the boss bar.
	 * It used to be refreshed from {@code customServerAiStep()}, which vanilla reaches only
	 * through {@code serverAiStep()} — the very thing being skipped — so hitting a frozen
	 * Sukuna would have left the bar stuck at whatever it read when the domain landed.
	 */
	@Override
	public void baseTick() {
		super.baseTick();
		DomainSuppression.tick(this);
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
	}

	/**
	 * The freeze itself. {@code aiStep()} reads this and skips {@code serverAiStep()} entirely
	 * — goals, navigation, the controls and {@code customServerAiStep()}, which is where his AI
	 * runs — while leaving {@code travel()} alone, so gravity, drag and collision all keep
	 * working and a Sukuna caught in mid-air still falls.
	 */
	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || DomainSuppression.isSuppressed(this);
	}

	@Override
	public void customServerAiStep() {
		super.customServerAiStep();
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
		// run sukuna ai every tick from here
		SukunaNPCTickProcedure.execute((Level) this.level(), this);
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
		event.register(JjkStrongestModEntities.SUKUNA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(world, pos, random) && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)),
				RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.65);
		builder = builder.add(Attributes.MAX_HEALTH, 250);
		builder = builder.add(Attributes.ARMOR, 20);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 14);
		builder = builder.add(Attributes.FOLLOW_RANGE, 128);
		return builder;
	}
}
