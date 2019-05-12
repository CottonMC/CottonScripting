package io.github.cottonmc.cotton_scripting.impl;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class ScoresDataboard extends PersistentState {
	private Map<String, Map<String, Object>> scores;

	public ScoresDataboard() {
		super("scorebase_scores");
		scores = new HashMap<>();
	}

	public void put(String name, String entity, Object value) {
		if (!scores.containsKey(name)) scores.put(name, new HashMap<>());
		Map<String, Object> score = scores.get(name);
		score.put(entity, value);
		markDirty();
	}

	public Object get(String name, String entity) {
		if (!scores.containsKey(name)) scores.put(name, new HashMap<>());
		Map<String, Object> score = scores.get(name);
		return score.get(entity);
	}

	@Override
	public void fromTag(CompoundTag compoundTag) {
		scores.clear();
		for (String name : compoundTag.getKeys()) {
			if (compoundTag.getType(name) == NbtType.COMPOUND) {
				CompoundTag scoreTag = compoundTag.getCompound(name);
				Map<String, Object> score = new HashMap<>();
				for (String player : scoreTag.getKeys()) {
					Tag tag = scoreTag.getTag("player");
					switch(tag.getType()) {
						case NbtType.BYTE:
							score.put(player, compoundTag.getBoolean(player));
							break;
						case NbtType.INT:
							score.put(player, compoundTag.getInt(player));
							break;
						case NbtType.DOUBLE:
							score.put(player, compoundTag.getDouble(player));
							break;
						case NbtType.FLOAT:
							score.put(player, compoundTag.getFloat(player));
							break;
						case NbtType.STRING:
							score.put(player, compoundTag.getString(player));
							break;
						default:
							break;
					}
				}
				scores.put(name, score);
			}
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		for (String score : scores.keySet()) {
			CompoundTag scoreTag = new CompoundTag();
			Map<String, Object> board = scores.get(score);
			for (String player : board.keySet()) {
				Object value = board.get(player);
				if (value instanceof Boolean) {
					scoreTag.putBoolean(player, (Boolean)value);
				} else if (value instanceof Integer) {
					scoreTag.putInt(player, (Integer)value);
				} else if (value instanceof Double) {
					scoreTag.putDouble(player, (Double)value);
				} else if (value instanceof Float) {
					scoreTag.putFloat(player, (Float)value);
				} else if (value instanceof String) {
					scoreTag.putString(player, (String)value);
				}
			}
			tag.put(score, scoreTag);
		}
		return tag;
	}
}
