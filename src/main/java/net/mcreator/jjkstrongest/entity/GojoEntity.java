package net.mcreator.jjkstrongest.entity;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;

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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;

import net.mcreator.jjkstrongest.procedures.GojoNPCTickProcedure;
import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;
import net.mcreator.jjkstrongest.init.JjkStrongestModMobEffects;

public class GojoEntity extends Monster {
	private final ServerBossEvent bossInfo = new ServerBossEvent(
			this.getDisplayName(), ServerBossEvent.BossBarColor.BLUE, ServerBossEvent.BossBarOverlay.PROGRESS);

	public GojoEntity(PlayMessages.SpawnEntity packet, Level world) {
		this(JjkStrongestModEntities.GOJO.get(), world);
	}

	public GojoEntity(EntityType<GojoEntity> type, Level world) {
		super(type, world);
		setMaxUpStep(1.3f);
		xpReward = 0;
		setNoAi(false);
		setPersistenceRequired();
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void registerGoals() {
		// Never target creative / spectator players
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, 10, false, false,
				e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, MahoragaEntity.class, false, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, false, false));
		this.targetSelector.addGoal(4, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(6, new FloatGoal(this));
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEFINED;
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
	}

	// ── Infinity: Gojo never takes fall damage ────────────────────────────────
	@Override
	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.FALL))
			return false;
		if (this.hasEffect(JjkStrongestModMobEffects.INFORMATION_OVERLOAD.get())) {
			// Unlimited Void penetrates Infinity and deals 3× damage —
			// Gojo is overwhelmed until his adaptation (domain counter) fires.
			amount *= 3.0f;
		} else {
			// Infinity converges all attacks to near-zero; 50% effective reduction.
			amount *= 0.5f;
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean canChangeDimensions() {
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

	@Override
	public void customServerAiStep() {
		super.customServerAiStep();
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
		GojoNPCTickProcedure.execute(this.level(), this);
	}

	public static void init() {
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.50)
				.add(Attributes.MAX_HEALTH, 250)
				.add(Attributes.ARMOR, 8)
				.add(Attributes.ATTACK_DAMAGE, 10)
				.add(Attributes.FOLLOW_RANGE, 128);
	}
}
