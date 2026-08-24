package net.efkrdnz.jjkstrongest.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;
import net.efkrdnz.jjkstrongest.procedures.BlueVortexProcedure;

public class BlueVortexEntity extends Entity {

	public BlueVortexEntity(EntityType<? extends BlueVortexEntity> type, Level world) {
		super(type, world);
		this.noPhysics = true;
		this.setNoGravity(true);
	}


	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	public void tick() {
		super.tick();
		this.noPhysics = true;
		this.setNoGravity(true);
		if (!this.level().isClientSide()) {
			BlueVortexProcedure.tickAnchor(this.level(), this);
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}
}
