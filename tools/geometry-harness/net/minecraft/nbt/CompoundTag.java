package net.minecraft.nbt;

import java.util.HashMap;
import java.util.Map;

public class CompoundTag {
	private final Map<String, Object> values = new HashMap<>();
	public void putByteArray(String key, byte[] v) { values.put(key, v); }
	public byte[] getByteArray(String key) { Object v = values.get(key); return v instanceof byte[] b ? b : new byte[0]; }
	public boolean contains(String key) { return values.containsKey(key); }
}
