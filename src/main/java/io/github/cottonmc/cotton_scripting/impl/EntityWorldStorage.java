package io.github.cottonmc.cotton_scripting.impl;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class EntityWorldStorage extends PersistentState {
	private Map<String, Map<String, Object>> values;

	public EntityWorldStorage() {
		super("entity_world_storage");
		values = new HashMap<>();
	}

	public void put(String name, String entity, Object value) {
		if (!values.containsKey(name)) values.put(name, new HashMap<>());
		Map<String, Object> val = values.get(name);
		val.put(entity, value);
		markDirty();
	}

	public Object get(String name, String entity) {
		if (!values.containsKey(name)) values.put(name, new HashMap<>());
		Map<String, Object> value = values.get(name);
		if (!value.containsKey(entity)) return 0;
		return value.get(entity);
	}

	@Override
	public void fromTag(CompoundTag compoundTag) {
		values.clear();
		for (String name : compoundTag.getKeys()) {
			if (compoundTag.getType(name) == NbtType.COMPOUND) {
				CompoundTag valueTag = compoundTag.getCompound(name);
				Map<String, Object> value = new HashMap<>();
				for (String entity : valueTag.getKeys()) {
					Tag tag = valueTag.get(entity);
					switch(tag.getType()) {
						case NbtType.BYTE:
							value.put(entity, compoundTag.getBoolean(entity));
							break;
						case NbtType.INT:
							value.put(entity, compoundTag.getInt(entity));
							break;
						case NbtType.DOUBLE:
							value.put(entity, compoundTag.getDouble(entity));
							break;
						case NbtType.FLOAT:
							value.put(entity, compoundTag.getFloat(entity));
							break;
						case NbtType.STRING:
							value.put(entity, compoundTag.getString(entity));
							break;
						default:
							break;
					}
				}
				values.put(name, value);
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		for (String value : values.keySet()) {
			CompoundTag valueTag = new CompoundTag();
			Map<String, Object> board = values.get(value);
			for (String entity : board.keySet()) {
				Object res = board.get(entity);
				if (res instanceof Boolean) {
					valueTag.putBoolean(entity, (Boolean)res);
				} else if (res instanceof Integer) {
					valueTag.putInt(entity, (Integer)res);
				} else if (res instanceof Double) {
					valueTag.putDouble(entity, (Double)res);
				} else if (res instanceof Float) {
					valueTag.putFloat(entity, (Float)res);
				} else if (res instanceof String) {
					valueTag.putString(entity, (String)res);
				}
			}
			tag.put(value, valueTag);
		}
		return tag;
	}
}
