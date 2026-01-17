package net.mcreator.jjkstrongest.entity;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.nbt.CompoundTag;

import net.mcreator.jjkstrongest.init.JjkStrongestModEntities;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class DismantleProjectileEntity extends AbstractArrow implements ItemSupplier {
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);
	// synced slash parameters
	private static final EntityDataAccessor<Float> SLASH_LENGTH = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> SLASH_WIDTH = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> SLASH_STYLE = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> SLASH_ROLL = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> SLASH_SEED = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> COLOR_R = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> COLOR_G = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> COLOR_B = SynchedEntityData.defineId(DismantleProjectileEntity.class, EntityDataSerializers.FLOAT);

	public DismantleProjectileEntity(PlayMessages.SpawnEntity packet, Level world) {
		super(JjkStrongestModEntities.DISMANTLE_PROJECTILE.get(), world);
		setupDefaults();
	}

	public DismantleProjectileEntity(EntityType<? extends DismantleProjectileEntity> type, Level world) {
		super(type, world);
		setupDefaults();
	}

	public DismantleProjectileEntity(EntityType<? extends DismantleProjectileEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world);
		setupDefaults();
	}

	public DismantleProjectileEntity(EntityType<? extends DismantleProjectileEntity> type, LivingEntity entity, Level world) {
		super(type, entity, world);
		setupDefaults();
	}

	private void setupDefaults() {
		this.setBaseDamage(0);
		this.setKnockback(0);
		this.setCritArrow(false);
		this.setSilent(true);
		this.setNoGravity(true);
		this.pickup = AbstractArrow.Pickup.DISALLOWED;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(SLASH_LENGTH, 8.0f);
		this.entityData.define(SLASH_WIDTH, 0.35f);
		this.entityData.define(SLASH_STYLE, 0);
		this.entityData.define(SLASH_ROLL, 0.0f);
		this.entityData.define(SLASH_SEED, 0.0f);
		this.entityData.define(DIR_X, 0.0f);
		this.entityData.define(DIR_Y, 0.0f);
		this.entityData.define(DIR_Z, 1.0f);
		this.entityData.define(COLOR_R, 1.0f);
		this.entityData.define(COLOR_G, 0.2f);
		this.entityData.define(COLOR_B, 0.2f);
	}

	public void setSlashParams(float length, float width, int style, float roll, float seed, float dirX, float dirY, float dirZ, float r, float g, float b) {
		this.entityData.set(SLASH_LENGTH, length);
		this.entityData.set(SLASH_WIDTH, width);
		this.entityData.set(SLASH_STYLE, style);
		this.entityData.set(SLASH_ROLL, roll);
		this.entityData.set(SLASH_SEED, seed);
		this.entityData.set(DIR_X, dirX);
		this.entityData.set(DIR_Y, dirY);
		this.entityData.set(DIR_Z, dirZ);
		this.entityData.set(COLOR_R, r);
		this.entityData.set(COLOR_G, g);
		this.entityData.set(COLOR_B, b);
	}

	public float getSlashLength() {
		return this.entityData.get(SLASH_LENGTH);
	}

	public float getSlashWidth() {
		return this.entityData.get(SLASH_WIDTH);
	}

	public int getSlashStyle() {
		return this.entityData.get(SLASH_STYLE);
	}

	public float getSlashRoll() {
		return this.entityData.get(SLASH_ROLL);
	}

	public float getSlashSeed() {
		return this.entityData.get(SLASH_SEED);
	}

	public float getDirX() {
		return this.entityData.get(DIR_X);
	}

	public float getDirY() {
		return this.entityData.get(DIR_Y);
	}

	public float getDirZ() {
		return this.entityData.get(DIR_Z);
	}

	public float getColorR() {
		return this.entityData.get(COLOR_R);
	}

	public float getColorG() {
		return this.entityData.get(COLOR_G);
	}

	public float getColorB() {
		return this.entityData.get(COLOR_B);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat("slash_length", getSlashLength());
		tag.putFloat("slash_width", getSlashWidth());
		tag.putInt("slash_style", getSlashStyle());
		tag.putFloat("slash_roll", getSlashRoll());
		tag.putFloat("slash_seed", getSlashSeed());
		tag.putFloat("dir_x", getDirX());
		tag.putFloat("dir_y", getDirY());
		tag.putFloat("dir_z", getDirZ());
		tag.putFloat("color_r", getColorR());
		tag.putFloat("color_g", getColorG());
		tag.putFloat("color_b", getColorB());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("slash_length"))
			this.entityData.set(SLASH_LENGTH, tag.getFloat("slash_length"));
		if (tag.contains("slash_width"))
			this.entityData.set(SLASH_WIDTH, tag.getFloat("slash_width"));
		if (tag.contains("slash_style"))
			this.entityData.set(SLASH_STYLE, tag.getInt("slash_style"));
		if (tag.contains("slash_roll"))
			this.entityData.set(SLASH_ROLL, tag.getFloat("slash_roll"));
		if (tag.contains("slash_seed"))
			this.entityData.set(SLASH_SEED, tag.getFloat("slash_seed"));
		if (tag.contains("dir_x"))
			this.entityData.set(DIR_X, tag.getFloat("dir_x"));
		if (tag.contains("dir_y"))
			this.entityData.set(DIR_Y, tag.getFloat("dir_y"));
		if (tag.contains("dir_z"))
			this.entityData.set(DIR_Z, tag.getFloat("dir_z"));
		if (tag.contains("color_r"))
			this.entityData.set(COLOR_R, tag.getFloat("color_r"));
		if (tag.contains("color_g"))
			this.entityData.set(COLOR_G, tag.getFloat("color_g"));
		if (tag.contains("color_b"))
			this.entityData.set(COLOR_B, tag.getFloat("color_b"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack getItem() {
		return PROJECTILE_ITEM;
	}

	@Override
	protected ItemStack getPickupItem() {
		return PROJECTILE_ITEM;
	}

	@Override
	protected void doPostHurtEffects(LivingEntity entity) {
		super.doPostHurtEffects(entity);
		entity.setArrowCount(entity.getArrowCount() - 1);
	}

	@Override
	public void tick() {
		super.tick();
		// Debug output every tick on server
		if (!this.level().isClientSide() && this.tickCount % 5 == 0) {
			//System.out.println("[Dismantle Entity] Tick " + this.tickCount + " at " + this.position() + " | Length: " + getSlashLength() + " | Style: " + getSlashStyle());
		}
		// Spawn particles to show entity location (SERVER SIDE)
		if (!this.level().isClientSide() && this.level() instanceof ServerLevel _level) {
			_level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
		}
		// visual only - short lifetime
		if (this.tickCount >= 12) {
			//System.out.println("[Dismantle Entity] Despawning after 12 ticks");
			this.discard();
		}
	}
}
