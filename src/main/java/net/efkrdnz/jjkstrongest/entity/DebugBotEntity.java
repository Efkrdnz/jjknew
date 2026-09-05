package net.efkrdnz.jjkstrongest.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.procedures.DebugBotTickProcedure;

/**
 * A sorcerer you can drive from the command line.
 *
 * <p>Exists because there was no way to watch a domain clash. Standing in one tells you
 * almost nothing — the interior fills the screen, the barrier is behind you, and the thing
 * you actually want to see is two shells pressing on each other from outside. Two of these,
 * aimed at each other, is that view.
 *
 * <p>A pure puppet: no goals, no target selector, no AI procedure. That is deliberate and
 * not laziness. The mod's existing NPCs overwrite velocity and rotation every tick, re-face
 * their target before every attack, and re-acquire targets through vanilla goals — all of
 * which fight manual control, and none of which can be switched off without also switching
 * off {@code customServerAiStep}, where their technique casting lives. A bot that only does
 * what it is told is worth more for debugging than one that fights well.
 *
 * <p>Deliberately <em>not</em> invulnerable: an open domain is beaten by damaging its
 * caster, so a shrine bot you cannot hit is a shrine that always wins.
 */
public class DebugBotEntity extends PathfinderMob {

	/** Its name, which is also how commands address it and how it labels itself. */
	private static final EntityDataAccessor<String> BOT_NAME = SynchedEntityData.defineId(DebugBotEntity.class, EntityDataSerializers.STRING);
	/** Which sorcerer's abilities it has. Mirrors {@code PLAYER_VARIABLES.sorcerer}. */
	private static final EntityDataAccessor<String> CHARACTER = SynchedEntityData.defineId(DebugBotEntity.class, EntityDataSerializers.STRING);
	/** Held in place against knockback, so a clash cannot shove it out of position. */
	private static final EntityDataAccessor<Boolean> FROZEN = SynchedEntityData.defineId(DebugBotEntity.class, EntityDataSerializers.BOOLEAN);

	public DebugBotEntity(EntityType<DebugBotEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(true);
		setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(BOT_NAME, "");
		builder.define(CHARACTER, "");
		builder.define(FROZEN, false);
	}

	public String getBotName() {
		return this.entityData.get(BOT_NAME);
	}

	public void setBotName(String name) {
		this.entityData.set(BOT_NAME, name == null ? "" : name);
		// The display name is what Blue, Red and Purple stamp onto their projectiles as
		// "caster", and they compare it as a string. Two bots sharing a name would be
		// immune to each other's orbs, which in a duel looks like the ability doing nothing.
		this.setCustomName(net.minecraft.network.chat.Component.literal(getBotName()));
		this.setCustomNameVisible(true);
	}

	public String getCharacter() {
		return this.entityData.get(CHARACTER);
	}

	public void setCharacter(String character) {
		this.entityData.set(CHARACTER, character == null ? "" : character);
	}

	public boolean isFrozen() {
		return this.entityData.get(FROZEN);
	}

	public void setFrozen(boolean frozen) {
		this.entityData.set(FROZEN, frozen);
	}

	/** Points the bot at a spot and pins the previous rotation with it. */
	public void lookAt(Vec3 target) {
		Vec3 from = this.position().add(0.0, this.getEyeHeight(), 0.0);
		double dx = target.x - from.x;
		double dy = target.y - from.y;
		double dz = target.z - from.z;
		double flat = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) (-Math.toDegrees(Math.atan2(dy, flat)));
		this.setYRot(yaw);
		this.setYHeadRot(yaw);
		this.setYBodyRot(yaw);
		this.setXRot(Math.max(-90.0f, Math.min(90.0f, pitch)));
		// getViewVector interpolates against the previous rotation, so without pinning
		// these an ability fired on the same tick aims at where the bot used to be looking.
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
		this.yHeadRotO = this.getYRot();
	}

	@Override
	public void baseTick() {
		super.baseTick();
		DebugBotTickProcedure.execute(this);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (isFrozen())
			this.setDeltaMovement(Vec3.ZERO);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putString("botName", getBotName());
		compound.putString("botCharacter", getCharacter());
		compound.putBoolean("botFrozen", isFrozen());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("botName"))
			setBotName(compound.getString("botName"));
		if (compound.contains("botCharacter"))
			setCharacter(compound.getString("botCharacter"));
		if (compound.contains("botFrozen"))
			setFrozen(compound.getBoolean("botFrozen"));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.MAX_HEALTH, 200).add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3)
				.add(Attributes.FOLLOW_RANGE, 64).add(Attributes.STEP_HEIGHT, 0.6);
	}
}
