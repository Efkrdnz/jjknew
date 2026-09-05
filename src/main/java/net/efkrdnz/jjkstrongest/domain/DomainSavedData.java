package net.efkrdnz.jjkstrongest.domain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Level-scoped storage for the blocks a domain has carved out, so they can be put
 * back when it closes.
 *
 * <p>The old system kept this on the entity, as one {@code CompoundTag} per block
 * keyed by a {@code "x,y,z"} string — a six-figure map of compounds serialised with
 * the entity on every save. Here a record is a shared palette plus two flat arrays,
 * twelve bytes per block, and air is never recorded at all: an above-ground cast
 * carves mostly empty space and writes almost nothing.
 *
 * <p>Living in level storage rather than on the entity also means an unloaded chunk
 * or a hard server stop cannot strand the record — {@link #restoreOrphans} puts back
 * anything whose domain no longer exists.
 */
public class DomainSavedData extends SavedData {

	public static final String NAME = "jjk_strongest_domains";

	private final Map<UUID, CarveRecord> records = new LinkedHashMap<>();

	public static DomainSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(DomainSavedData::new, DomainSavedData::load, null), NAME);
	}

	public CarveRecord record(UUID domainId) {
		return records.computeIfAbsent(domainId, id -> new CarveRecord());
	}

	public CarveRecord peek(UUID domainId) {
		return records.get(domainId);
	}

	public void drop(UUID domainId) {
		if (records.remove(domainId) != null)
			setDirty();
	}

	public List<UUID> domainIds() {
		return new ArrayList<>(records.keySet());
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, CarveRecord> entry : records.entrySet()) {
			CompoundTag one = entry.getValue().save();
			one.putUUID("domain", entry.getKey());
			list.add(one);
		}
		tag.put("records", list);
		return tag;
	}

	public static DomainSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		DomainSavedData data = new DomainSavedData();
		ListTag list = tag.getList("records", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag one = list.getCompound(i);
			if (!one.hasUUID("domain"))
				continue;
			data.records.put(one.getUUID("domain"), CarveRecord.load(one, registries));
		}
		return data;
	}

	/**
	 * One domain's carved blocks: a palette of distinct states, then a position and a
	 * palette index per block.
	 */
	public static final class CarveRecord {

		private final List<BlockState> palette = new ArrayList<>();
		private final Map<BlockState, Integer> paletteIndex = new HashMap<>();
		private long[] positions = new long[1024];
		private int[] states = new int[1024];
		private int size = 0;
		private final List<CompoundTag> blockEntities = new ArrayList<>();

		public int size() {
			return size;
		}

		public void add(BlockPos pos, BlockState state, CompoundTag blockEntity) {
			if (size == positions.length) {
				positions = Arrays.copyOf(positions, size * 2);
				states = Arrays.copyOf(states, size * 2);
			}
			Integer index = paletteIndex.get(state);
			if (index == null) {
				index = palette.size();
				palette.add(state);
				paletteIndex.put(state, index);
			}
			positions[size] = pos.asLong();
			states[size] = index;
			size++;
			if (blockEntity != null) {
				CompoundTag wrapper = new CompoundTag();
				wrapper.putLong("pos", pos.asLong());
				wrapper.put("nbt", blockEntity);
				blockEntities.add(wrapper);
			}
		}

		public long positionAt(int index) {
			return positions[index];
		}

		public BlockState stateAt(int index) {
			int paletteSlot = states[index];
			return paletteSlot >= 0 && paletteSlot < palette.size() ? palette.get(paletteSlot) : null;
		}

		public List<CompoundTag> blockEntities() {
			return blockEntities;
		}

		CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			ListTag paletteTag = new ListTag();
			for (BlockState state : palette)
				paletteTag.add(NbtUtils.writeBlockState(state));
			tag.put("palette", paletteTag);
			tag.put("positions", new LongArrayTag(Arrays.copyOf(positions, size)));
			tag.put("states", new IntArrayTag(Arrays.copyOf(states, size)));
			ListTag beTag = new ListTag();
			beTag.addAll(blockEntities);
			tag.put("blockEntities", beTag);
			return tag;
		}

		static CarveRecord load(CompoundTag tag, HolderLookup.Provider registries) {
			CarveRecord record = new CarveRecord();
			ListTag paletteTag = tag.getList("palette", Tag.TAG_COMPOUND);
			for (int i = 0; i < paletteTag.size(); i++) {
				BlockState state = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), paletteTag.getCompound(i));
				record.palette.add(state);
				record.paletteIndex.putIfAbsent(state, i);
			}
			long[] loadedPositions = tag.getLongArray("positions");
			int[] loadedStates = tag.getIntArray("states");
			int count = Math.min(loadedPositions.length, loadedStates.length);
			record.positions = Arrays.copyOf(loadedPositions, Math.max(1024, count));
			record.states = Arrays.copyOf(loadedStates, Math.max(1024, count));
			record.size = count;
			ListTag beTag = tag.getList("blockEntities", Tag.TAG_COMPOUND);
			for (int i = 0; i < beTag.size(); i++)
				record.blockEntities.add(beTag.getCompound(i));
			return record;
		}
	}
}
