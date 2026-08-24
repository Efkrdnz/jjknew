package net.efkrdnz.jjkstrongest.entity;


import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;

import java.util.List;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class DismantleTravelEntity extends AbstractArrow implements ItemSupplier {
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);
	private static final EntityDataAccessor<Float> SLASH_LENGTH = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> SLASH_WIDTH = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> SLASH_STYLE = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> SLASH_ROLL = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> SLASH_SEED = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> COLOR_R = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> COLOR_G = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> COLOR_B = SynchedEntityData.defineId(DismantleTravelEntity.class, EntityDataSerializers.FLOAT);


	public DismantleTravelEntity(EntityType<? extends DismantleTravelEntity> type, Level world) {
		super(type, world);
		setupDefaults();
	}

	public DismantleTravelEntity(EntityType<? extends DismantleTravelEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world, new ItemStack(Items.ARROW), null);
		setupDefaults();
	}

	public DismantleTravelEntity(EntityType<? extends DismantleTravelEntity> type, LivingEntity entity, Level world) {
		super(type, entity, world, new ItemStack(Items.ARROW), null);
		setupDefaults();
	}

	private void setupDefaults() {
		this.setBaseDamage(0);
		this.setCritArrow(false);
		this.setSilent(true);
		this.setNoGravity(true);
		this.pickup = AbstractArrow.Pickup.DISALLOWED;
		this.noPhysics = true;
	}


	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SLASH_LENGTH, 10.0f);
		builder.define(SLASH_WIDTH, 0.18f);
		builder.define(SLASH_STYLE, 0);
		builder.define(SLASH_ROLL, 0.0f);
		builder.define(SLASH_SEED, 0.0f);
		builder.define(DIR_X, 0.0f);
		builder.define(DIR_Y, 0.0f);
		builder.define(DIR_Z, 1.0f);
		builder.define(COLOR_R, 1.0f);
		builder.define(COLOR_G, 0.2f);
		builder.define(COLOR_B, 0.2f);
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
	protected float getWaterInertia() {
		return 1.0f;
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide()) {
			this.getPersistentData().putInt("hit_this_tick", 0);
			this.getPersistentData().putInt("block_hit_this_tick", 0);
			int arm = this.getPersistentData().getInt("arm_ticks");
			if (arm > 0) {
				this.getPersistentData().putInt("arm_ticks", arm - 1);
			}
			int armBreak = this.getPersistentData().getInt("arm_break_ticks");
			if (armBreak > 0) {
				this.getPersistentData().putInt("arm_break_ticks", armBreak - 1);
			}
			applyForcedVelocity();
			// growing slash for charged shots
			updateGrowingSlash();
		}
		super.tick();
		this.inGround = false;
		if (!this.level().isClientSide()) {
			applyForcedVelocity();
			runCutLogicServer();
			startTimerIfHit();
			despawnAfterTimer();
		}
	}

	private void applyForcedVelocity() {
		double spd = this.getPersistentData().getDouble("fixed_speed");
		if (spd <= 0)
			return;
		double vx = this.getPersistentData().getDouble("fixed_vx");
		double vy = this.getPersistentData().getDouble("fixed_vy");
		double vz = this.getPersistentData().getDouble("fixed_vz");
		Vec3 dir = new Vec3(vx, vy, vz);
		if (dir.lengthSqr() < 1.0e-8)
			return;
		dir = dir.normalize().scale(spd);
		this.setDeltaMovement(dir);
	}

	// grow slash size if charged and no block hit yet
	private void updateGrowingSlash() {
		boolean isCharged = this.getPersistentData().getBoolean("is_charged");
		if (!isCharged)
			return;
		int armBreak = this.getPersistentData().getInt("arm_break_ticks");
		if (armBreak > 0)
			return;
		boolean hasHitBlock = this.getPersistentData().getBoolean("has_hit_block_ever");
		if (hasHitBlock)
			return;
		double baseHalfWidth = this.getPersistentData().getDouble("base_halfWidth");
		double baseThickness = this.getPersistentData().getDouble("base_thickness");
		int ticksSinceArm = this.tickCount - 4;
		if (ticksSinceArm < 0)
			ticksSinceArm = 0;
		double secondsElapsed = ticksSinceArm / 20.0;
		double growthPerSecond = baseHalfWidth;
		double maxGrowthMultiplier = 3.0;
		double maxGrowth = baseHalfWidth * (maxGrowthMultiplier - 1.0);
		double growth = Math.min(secondsElapsed * growthPerSecond, maxGrowth);
		this.getPersistentData().putDouble("halfWidth", baseHalfWidth + growth);
		this.getPersistentData().putDouble("thickness", baseThickness + (growth / baseHalfWidth) * baseThickness);
	}

	private void startTimerIfHit() {
		int start = this.getPersistentData().getInt("despawn_start_tick");
		if (start != 0)
			return;
		boolean hit = false;
		if (this.getPersistentData().getInt("hit_this_tick") == 1)
			hit = true;
		int arm = this.getPersistentData().getInt("arm_ticks");
		if (arm <= 0) {
			if (this.getPersistentData().getInt("block_hit_this_tick") == 1)
				hit = true;
		}
		if (hit) {
			this.getPersistentData().putInt("despawn_start_tick", this.tickCount);
		}
	}

	private void despawnAfterTimer() {
		int start = this.getPersistentData().getInt("despawn_start_tick");
		if (start == 0)
			return;
		int linger = this.getPersistentData().getInt("linger_after_hit");
		if (linger <= 0)
			linger = 6;
		if (this.tickCount - start >= linger) {
			this.discard();
		}
	}

	private void runCutLogicServer() {
		double dmg = this.getPersistentData().getDouble("dmg");
		if (dmg <= 0)
			dmg = 6.0;
		double halfWidth = this.getPersistentData().getDouble("halfWidth");
		if (halfWidth <= 0)
			halfWidth = 2.0;
		double thickness = this.getPersistentData().getDouble("thickness");
		if (thickness <= 0)
			thickness = 0.12;
		int maxHits = this.getPersistentData().getInt("maxHits");
		if (maxHits <= 0)
			maxHits = 9999;
		int hits = this.getPersistentData().getInt("hits");
		Vec3 cutDir = new Vec3(this.getPersistentData().getDouble("cut_x"), this.getPersistentData().getDouble("cut_y"), this.getPersistentData().getDouble("cut_z"));
		if (cutDir.lengthSqr() < 1.0e-6)
			cutDir = new Vec3(1, 0, 0);
		cutDir = cutDir.normalize();
		Vec3 vel = this.getDeltaMovement();
		if (vel.lengthSqr() < 1.0e-6)
			vel = new Vec3(0, 0, 1);
		Vec3 forward = vel.normalize();
		Vec3 normal = forward.cross(cutDir);
		if (normal.lengthSqr() < 1.0e-6)
			normal = new Vec3(0, 1, 0);
		normal = normal.normalize();
		boolean doBreak = this.getPersistentData().getBoolean("breakBlocks");
		int armBreak = this.getPersistentData().getInt("arm_break_ticks");
		if (armBreak > 0) {
			doBreak = false;
		}
		// direct collision check at current position every tick
		Vec3 currentPos = this.position();
		if (doBreak) {
			int destroyed = breakSlashPlane(this.level(), currentPos, cutDir, normal, halfWidth, thickness, this);
			if (destroyed >= 4) {
				this.getPersistentData().putInt("block_hit_this_tick", 1);
				this.getPersistentData().putBoolean("has_hit_block_ever", true);
			}
		}
		if (hits < maxHits) {
			int newHits = damageEntitiesInPlane(this.level(), currentPos, cutDir, normal, halfWidth, thickness, dmg, maxHits - hits, this.getOwner(), this);
			if (newHits > 0) {
				hits += newHits;
				this.getPersistentData().putInt("hits", hits);
				this.getPersistentData().putInt("hit_this_tick", 1);
			}
		}
	}

	// check blocks in plane around current position
	private static int breakSlashPlane(Level world, Vec3 center, Vec3 cutDir, Vec3 normal, double halfWidth, double thickness, Entity breaker) {
		Vec3 cut = cutDir.lengthSqr() < 1.0e-8 ? new Vec3(1, 0, 0) : cutDir.normalize();
		Vec3 nrm = normal.lengthSqr() < 1.0e-8 ? new Vec3(0, 1, 0) : normal.normalize();
		double margin = 0.866;
		double inflate = halfWidth + thickness + 2.0;
		double minX = center.x - inflate;
		double minY = center.y - inflate;
		double minZ = center.z - inflate;
		double maxX = center.x + inflate;
		double maxY = center.y + inflate;
		double maxZ = center.z + inflate;
		int iMinX = (int) Math.floor(minX);
		int iMinY = (int) Math.floor(minY);
		int iMinZ = (int) Math.floor(minZ);
		int iMaxX = (int) Math.floor(maxX);
		int iMaxY = (int) Math.floor(maxY);
		int iMaxZ = (int) Math.floor(maxZ);
		int destroyed = 0;
		for (int x = iMinX; x <= iMaxX; x++) {
			for (int y = iMinY; y <= iMaxY; y++) {
				for (int z = iMinZ; z <= iMaxZ; z++) {
					BlockPos bp = new BlockPos(x, y, z);
					if (world.getBlockState(bp).isAir())
						continue;
					Vec3 bc = new Vec3(x + 0.5, y + 0.5, z + 0.5);
					Vec3 d = bc.subtract(center);
					double w = d.dot(cut);
					double nn = d.dot(nrm);
					if (Math.abs(w) <= halfWidth + margin && Math.abs(nn) <= thickness + margin) {
						if (destroyOneBlock(world, bp, breaker)) {
							destroyed++;
						}
					}
				}
			}
		}
		return destroyed;
	}

	private static boolean destroyOneBlock(Level world, BlockPos bp, Entity breaker) {
		BlockState state = world.getBlockState(bp);
		if (state.isAir())
			return false;
		if (state.getDestroySpeed(world, bp) < 0)
			return false;
		BlockEntity be = world.getBlockEntity(bp);
		if (be != null)
			return false;
		if (state.getBlock().getExplosionResistance() > 30.0f)
			return false;
		world.destroyBlock(bp, false, breaker);
		return true;
	}

	// damage entities in plane around current position
	private static int damageEntitiesInPlane(Level world, Vec3 center, Vec3 cutDir, Vec3 normal, double halfWidth, double thickness, double dmg, int remainingHits, Entity owner, Entity projectile) {
		double searchRadius = halfWidth + 3.0;
		AABB box = new AABB(center.x - searchRadius, center.y - searchRadius, center.z - searchRadius, center.x + searchRadius, center.y + searchRadius, center.z + searchRadius);
		List<Entity> entities = world.getEntities(projectile, box, e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator());
		int hits = 0;
		long now = world.getGameTime();
		for (Entity e : entities) {
			if (hits >= remainingHits)
				break;
			if (e == owner)
				continue;
			Vec3 entityCenter = e.position().add(0, e.getBbHeight() * 0.5, 0);
			Vec3 rel = entityCenter.subtract(center);
			double w = rel.dot(cutDir);
			double n = rel.dot(normal);
			if (Math.abs(w) > halfWidth + 1.0)
				continue;
			if (Math.abs(n) > thickness + 1.0)
				continue;
			long last = e.getPersistentData().getLong("dismantle_travel_last_hit");
			if (now - last < 2)
				continue;
			e.getPersistentData().putLong("dismantle_travel_last_hit", now);
			Entity src = owner != null ? owner : projectile;
			DamageSource damageSource = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_dismantle"))), src);
			e.hurt(damageSource, (float) dmg);
			hits++;
		}
		return hits;
	}

	private static Vec3 rotateAroundAxis(Vec3 v, Vec3 axis, double radians) {
		Vec3 k = axis.normalize();
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		double dot = v.dot(k);
		Vec3 term1 = v.scale(cos);
		Vec3 term2 = k.cross(v).scale(sin);
		Vec3 term3 = k.scale(dot * (1.0 - cos));
		return term1.add(term2).add(term3);
	}

	public static DismantleTravelEntity shoot(Level world, LivingEntity shooter, RandomSource random, float power, double techniquePower, double output, double dmg, boolean breakBlocks, int slashMode, boolean diagonalFlip, int chargeTicks) {
		DismantleTravelEntity proj = new DismantleTravelEntity(JjkStrongestModEntities.DISMANTLE_TRAVEL.get(), shooter, world);
		Vec3 forward = shooter.getViewVector(1).normalize();
		Vec3 right = new Vec3(0, 1, 0).cross(forward);
		if (right.lengthSqr() < 1.0e-6)
			right = new Vec3(1, 0, 0);
		right = right.normalize();
		double baseDeg;
		if (slashMode == 1) {
			baseDeg = 90.0;
		} else {
			// weighted random: 70% horizontal, 15% +45°, 15% -45°
			double roll = random.nextDouble();
			if (roll < 0.70)
				baseDeg = 0.0;
			else if (roll < 0.85)
				baseDeg = 45.0;
			else
				baseDeg = -45.0;
		}
		double maxOffset = baseDeg == 90.0 ? 7.0 : 10.0;
		double offsetDeg = (random.nextDouble() * 2.0 - 1.0) * maxOffset;
		double rollDeg = baseDeg + offsetDeg;
		float rollRad = (float) Math.toRadians(rollDeg);
		Vec3 visualDir = forward;
		Vec3 cutAxis = rotateAroundAxis(right, forward, Math.toRadians(rollDeg)).normalize();
		double fixedSpeed = 2.35;
		proj.getPersistentData().putDouble("fixed_vx", forward.x);
		proj.getPersistentData().putDouble("fixed_vy", forward.y);
		proj.getPersistentData().putDouble("fixed_vz", forward.z);
		proj.getPersistentData().putDouble("fixed_speed", fixedSpeed);
		proj.setDeltaMovement(forward.scale(fixedSpeed));
		float tp = (float) Math.max(1.0, techniquePower);
		float out = (float) Math.max(1.0, output);
		float sizeMul = (float) (Math.sqrt(tp) * (0.85f + 0.15f * out));
		float visualLength = (float) (10.0 + 8.0 * sizeMul + (chargeTicks * 0.25));
		float visualWidth = (float) (0.14 + 0.07 * sizeMul);
		float seed = random.nextFloat() * 1000.0f;
		proj.setSlashParams(visualLength, visualWidth, 0, rollRad, seed, (float) visualDir.x, (float) visualDir.y, (float) visualDir.z, 1.0f, 0.2f, 0.2f);
		double extraFromCharge = Math.min(Math.max(chargeTicks, 0), 120) / 12.0;
		double halfWidth = (1.6 + 1.2 * Math.sqrt(tp)) * (0.85 + 0.15 * out) + 5.0 + extraFromCharge;
		double thickness = (0.09 + 0.03 * Math.sqrt(tp)) * (0.9 + 0.1 * out);
		proj.getPersistentData().putDouble("dmg", dmg);
		proj.getPersistentData().putDouble("halfWidth", halfWidth);
		proj.getPersistentData().putDouble("thickness", thickness);
		// store base values for growing mechanic
		proj.getPersistentData().putDouble("base_halfWidth", halfWidth);
		proj.getPersistentData().putDouble("base_thickness", thickness);
		proj.getPersistentData().putBoolean("breakBlocks", breakBlocks);
		proj.getPersistentData().putDouble("cut_x", cutAxis.x);
		proj.getPersistentData().putDouble("cut_y", cutAxis.y);
		proj.getPersistentData().putDouble("cut_z", cutAxis.z);
		proj.getPersistentData().putInt("hits", 0);
		proj.getPersistentData().putInt("maxHits", 9999);
		proj.getPersistentData().putInt("despawn_start_tick", 0);
		int linger = 5 + Math.min(Math.max(chargeTicks, 0), 120) / 40;
		proj.getPersistentData().putInt("linger_after_hit", linger);
		// check if this is a charged shot (tp = 2)
		boolean isCharged = (techniquePower >= 1.95 && techniquePower <= 2.05);
		proj.getPersistentData().putBoolean("is_charged", isCharged);
		proj.getPersistentData().putBoolean("has_hit_block_ever", false);
		proj.getPersistentData().putInt("arm_ticks", 6);
		proj.getPersistentData().putInt("arm_break_ticks", 4);
		Vec3 spawnPos = shooter.getEyePosition().add(forward.scale(5.0));
		proj.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
		world.addFreshEntity(proj);
		return proj;
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
	protected ItemStack getDefaultPickupItem() {
		// never actually picked up: this projectile sets Pickup.DISALLOWED
		return new ItemStack(Items.ARROW);
	}
}
