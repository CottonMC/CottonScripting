package io.github.cottonmc.cotton_scripting.impl;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class RulesDataboard extends PersistentState {
	private Map<String, Object> rules;

	public RulesDataboard() {
		super("scorebase_rules");
		rules = new HashMap<>();
	}

	public void put(String name, Object value) {
		rules.put(name, value);
		markDirty();
	}

	public Object get(String name) {
		return rules.get(name);
	}

	@Override
	public void fromTag(CompoundTag compoundTag) {
		rules.clear();
		for (String rule : compoundTag.getKeys()) {
			Tag tag = compoundTag.getTag(rule);
			switch(tag.getType()) {
				case NbtType.BYTE:
					rules.put(rule, compoundTag.getBoolean(rule));
					break;
				case NbtType.INT:
					rules.put(rule, compoundTag.getInt(rule));
					break;
				case NbtType.DOUBLE:
					rules.put(rule, compoundTag.getDouble(rule));
					break;
				case NbtType.FLOAT:
					rules.put(rule, compoundTag.getFloat(rule));
					break;
				case NbtType.STRING:
					rules.put(rule, compoundTag.getString(rule));
					break;
				default:
					break;
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		for (String rule : rules.keySet()) {
			Object value = rules.get(rule);
			if (value instanceof Boolean) {
				tag.putBoolean(rule, (Boolean)value);
			} else if (value instanceof Integer) {
				tag.putInt(rule, (Integer)value);
			} else if (value instanceof Double) {
				tag.putDouble(rule, (Double)value);
			} else if (value instanceof Float) {
				tag.putFloat(rule, (Float)value);
			} else if (value instanceof String) {
				tag.putString(rule, (String)value);
			}
		}
		return tag;
	}
}
