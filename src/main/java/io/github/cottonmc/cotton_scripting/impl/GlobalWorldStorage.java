package io.github.cottonmc.cotton_scripting.impl;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class GlobalWorldStorage extends PersistentState {
	private Map<String, Object> values;

	public GlobalWorldStorage() {
		super("global_world_storage");
		values = new HashMap<>();
	}

	public void put(String name, Object value) {
		values.put(name, value);
		markDirty();
	}

	public Object get(String name) {
		if (!values.containsKey(name)) return 0;
		return values.get(name);
	}

	@Override
	public void fromTag(CompoundTag compoundTag) {
		values.clear();
		for (String value : compoundTag.getKeys()) {
			Tag tag = compoundTag.getTag(value);
			switch(tag.getType()) {
				case NbtType.BYTE:
					values.put(value, compoundTag.getBoolean(value));
					break;
				case NbtType.INT:
					values.put(value, compoundTag.getInt(value));
					break;
				case NbtType.DOUBLE:
					values.put(value, compoundTag.getDouble(value));
					break;
				case NbtType.FLOAT:
					values.put(value, compoundTag.getFloat(value));
					break;
				case NbtType.STRING:
					values.put(value, compoundTag.getString(value));
					break;
				default:
					break;
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		for (String value : values.keySet()) {
			Object result = values.get(value);
			if (result instanceof Boolean) {
				tag.putBoolean(value, (Boolean)result);
			} else if (result instanceof Integer) {
				tag.putInt(value, (Integer)result);
			} else if (result instanceof Double) {
				tag.putDouble(value, (Double)result);
			} else if (result instanceof Float) {
				tag.putFloat(value, (Float)result);
			} else if (result instanceof String) {
				tag.putString(value, (String)result);
			}
		}
		return tag;
	}
}
