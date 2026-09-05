package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.List;
import java.util.UUID;

/**
 * Hollows out the inside of a domain, and puts it back afterwards.
 *
 * <p>The version this replaces walked a cube of up to 69&sup3; block positions —
 * around 330,000, each with a square root — <em>every tick for forty ticks</em>, and
 * filled the entire lower hemisphere with solid barrier blocks. Here the work is a
 * budget per tick, one horizontal layer at a time. Blocks that are already air cost
 * nothing and are never recorded.
 *
 * <p>The whole ball is cleared, the half below the floor plane included. It used to stop at
 * the plane so the ground you stood on stayed real, and that was the wrong call: a domain
 * that replaces the world cannot have grass and bedrock for a floor, and a mirror floor
 * needs nothing under it to reflect. The pit that leaves under the collision plane is dealt
 * with where it belongs — the phase machine lifts anything below the plane onto it at cast
 * and rescues anything that falls in later — rather than by keeping the terrain around.
 *
 * <p>Blocks are written with {@code UPDATE_CLIENTS} only. Neighbour notification on
 * this many changes would set off gravity and redstone cascades across the whole
 * volume, which is both expensive and wrong — the terrain is coming back shortly.
 */
public final class DomainCarve {

	private static final int SET_FLAGS = net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

	/** Positions examined per tick while carving. */
	public static final int CARVE_BUDGET = 6000;
	/** Blocks restored per tick while collapsing. */
	public static final int RESTORE_BUDGET = 6000;

	private DomainCarve() {
	}

	/**
	 * Carves one tick's worth of the interior.
	 *
	 * @return true once the whole volume has been cleared
	 */
	public static boolean advanceCarve(ServerLevel level, DomainUVEntity domain, DomainSphere sphere) {
		CompoundTag data = domain.getPersistentData();
		if (data.getBoolean("carveComplete"))
			return true;

		double radius = domain.getTargetRadius();
		if (radius <= 0.0)
			return true;

		BlockPos center = BlockPos.containing(sphere.center().x, sphere.center().y, sphere.center().z);
		// The whole ball, floor to crown. Below the world there is nothing to take; getBlockState
		// answers void air there, but the loop should not be spending its budget asking.
		int bottomY = Math.max((int) Math.floor(sphere.center().y - radius), level.getMinBuildHeight());
		int topY = Math.min((int) Math.ceil(sphere.center().y + radius), level.getMaxBuildHeight() - 1);

		int cursor = data.contains("carveY") ? Math.max(data.getInt("carveY"), bottomY) : bottomY;
		DomainSavedData storage = DomainSavedData.get(level);
		DomainSavedData.CarveRecord record = storage.record(domain.getUUID());

		int examined = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		while (cursor <= topY && examined < CARVE_BUDGET) {
			double dy = cursor - sphere.center().y;
			double discSq = radius * radius - dy * dy;
			if (discSq <= 0.0) {
				cursor++;
				continue;
			}
			int disc = (int) Math.floor(Math.sqrt(discSq));
			for (int dx = -disc; dx <= disc; dx++) {
				int span = (int) Math.floor(Math.sqrt(Math.max(0.0, discSq - (double) dx * dx)));
				for (int dz = -span; dz <= span; dz++) {
					pos.set(center.getX() + dx, cursor, center.getZ() + dz);
					examined++;
					BlockState state = level.getBlockState(pos);
					if (state.isAir())
						continue;
					if (isUncarvable(state))
						continue;
					CompoundTag beTag = null;
					BlockEntity be = level.getBlockEntity(pos);
					if (be != null) {
						beTag = be.saveWithoutMetadata(level.registryAccess());
						level.removeBlockEntity(pos);
					}
					record.add(pos.immutable(), state, beTag);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), SET_FLAGS);
				}
			}
			cursor++;
		}

		storage.setDirty();
		data.putInt("carveY", cursor);
		boolean done = cursor > topY;
		if (done)
			data.putBoolean("carveComplete", true);
		return done;
	}

	/**
	 * Puts back one tick's worth of carved blocks.
	 *
	 * @return true once everything has been restored and the record dropped
	 */
	public static boolean advanceRestore(ServerLevel level, DomainUVEntity domain) {
		DomainSavedData storage = DomainSavedData.get(level);
		UUID id = domain.getUUID();
		DomainSavedData.CarveRecord record = storage.peek(id);
		if (record == null)
			return true;

		CompoundTag data = domain.getPersistentData();
		int index = data.getInt("restoreIndex");
		int limit = Math.min(record.size(), index + RESTORE_BUDGET);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (; index < limit; index++) {
			BlockState state = record.stateAt(index);
			if (state == null)
				continue;
			pos.set(BlockPos.of(record.positionAt(index)));
			// Only fill space that is still empty. If somebody built inside the domain
			// while it was open, their block stays put rather than being overwritten by
			// the terrain that used to be there.
			if (!level.getBlockState(pos).isAir())
				continue;
			level.setBlock(pos, state, SET_FLAGS);
		}
		data.putInt("restoreIndex", index);

		if (index < record.size())
			return false;

		applyBlockEntities(level, record);
		storage.drop(id);
		return true;
	}

	/**
	 * What a domain will not take, even temporarily.
	 *
	 * <p>Bedrock is <em>not</em> on this list any more — a domain is supposed to replace
	 * the world inside it, and stopping at bedrock made the sphere visibly wrong wherever
	 * one happened to be. It goes back on collapse like anything else.
	 *
	 * <p>What is left is the set whose removal a restore cannot honestly undo: a portal
	 * carved out is a link broken for as long as the domain stands, and a command or
	 * structure block is somebody's build rather than terrain. The old
	 * {@code getDestroySpeed &lt; 0} test caught most of these by accident; this names them,
	 * so it is clear which are deliberate.
	 */
	private static boolean isUncarvable(BlockState state) {
		return state.is(Blocks.BARRIER) || state.is(Blocks.COMMAND_BLOCK) || state.is(Blocks.CHAIN_COMMAND_BLOCK) || state.is(Blocks.REPEATING_COMMAND_BLOCK) || state.is(Blocks.STRUCTURE_BLOCK)
				|| state.is(Blocks.JIGSAW) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY) || state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.NETHER_PORTAL);
	}

	private static void applyBlockEntities(ServerLevel level, DomainSavedData.CarveRecord record) {
		List<CompoundTag> tags = record.blockEntities();
		for (CompoundTag wrapper : tags) {
			BlockPos pos = BlockPos.of(wrapper.getLong("pos"));
			BlockEntity be = level.getBlockEntity(pos);
			if (be != null)
				be.loadWithComponents(wrapper.getCompound("nbt"), level.registryAccess());
		}
	}

	/**
	 * Restores any carve whose domain entity is gone — a crash, a hard stop, or an
	 * entity culled while its chunk was unloaded. Runs once when a level loads.
	 */
	public static void restoreOrphans(ServerLevel level) {
		DomainSavedData storage = DomainSavedData.get(level);
		for (UUID id : storage.domainIds()) {
			if (level.getEntity(id) instanceof DomainUVEntity)
				continue;
			DomainSavedData.CarveRecord record = storage.peek(id);
			if (record == null)
				continue;
			for (int i = 0; i < record.size(); i++) {
				BlockState state = record.stateAt(i);
				if (state == null)
					continue;
				BlockPos target = BlockPos.of(record.positionAt(i));
				if (!level.getBlockState(target).isAir())
					continue;
				level.setBlock(target, state, SET_FLAGS);
			}
			applyBlockEntities(level, record);
			storage.drop(id);
		}
	}
}
