package io.github.cottonmc.cotton_scripting.api;

import io.github.cottonmc.cotton_scripting.CottonScripting;
import io.github.cottonmc.cotton_scripting.impl.RulesDataboard;
import io.github.cottonmc.cotton_scripting.impl.ScoresDataboard;
import io.github.cottonmc.cotton_scripting.impl.ScriptTags;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

/**
 * Store and persistent data for players and the world. Name not final.
 */
public class Databoards {

	/**
	 * Get a value saved to the world, independent from any entities.
	 * @param world Pass ScriptContext.getCommandWorld() here.
	 * @param name The name of the rule. Will create the rule if it doesn't yet exist.
	 * @return The value of the rule. Returns null if the rule has never been set.
	 */
	@Nullable
	public static Object getRule(ServerWorld world, String name) {
		return world.getPersistentStateManager().getOrCreate(RulesDataboard::new, "rules_databoard").get(name);
	}

	/**
	 * Set a value saved to the world, indepedent from any entities.
	 * Also runs any scripts in the "cotton:scorebase_listeners" tag.
	 * @param world Pass ScriptContext.getCommandWorld() here.
	 * @param name The name of the rule. Will create the rule if it doesn't yet exist.
	 * @param value The value to set the rule to.
	 */
	public static void setRule(ServerWorld world, String name, Object value) {
		world.getPersistentStateManager().getOrCreate(RulesDataboard::new, "rules_databoard").put(name, value);
		for (Identifier id : ScriptTags.DATABOARD_LISTENERS.values()) {
			CottonScripting.runScriptFromServer(id, world);
		}
	}

	/**
	 * Get a value saved to the world, tied to a specific entity.
	 * Also runs any scripts in the "cotton:scorebase_listeners" tag.
	 * @param name The name of the score. Will create the score if it doesn't yet exist.
	 * @param source The command source of the entity to check the score of. Must have an entity associated.
	 * @return The value of the score. Returns null if the score has never been set for this entity.
	 */
	@Nullable
	public static Object getScore(String name, ServerCommandSource source) {
		if (source.getEntity() == null) throw new IllegalArgumentException("Must have an Entity to get a score for!");
		String uuid = source.getEntity().getUuidAsString();
		return source.getWorld().getPersistentStateManager().getOrCreate(ScoresDataboard::new, "scores_databoard").get(name, uuid);
	}

	/**
	 * Set a value saved to the world, tied to a specific entity.
	 * @param name The name of the score. Will create the score if it doesn't yet exist.
	 * @param source The command source of the entity to set the score for. Must have an entity associated.
	 * @param value The value to set the score to.
	 */
	public static void setScore(String name, ServerCommandSource source, Object value) {
		if (source.getEntity() == null) throw new IllegalArgumentException("Must have an Entity to set a score for!");
		String uuid = source.getEntity().getUuidAsString();
		source.getWorld().getPersistentStateManager().getOrCreate(ScoresDataboard::new, "scores_databoard").put(name, uuid, value);
		for (Identifier id : ScriptTags.DATABOARD_LISTENERS.values()) {
			CottonScripting.runScriptFromServer(id, source.getWorld());
		}
	}
}
