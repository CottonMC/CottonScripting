package io.github.cottonmc.cotton_scripting.api;

import javax.annotation.Nullable;

import io.github.cottonmc.cotton_scripting.CottonScripting;
import io.github.cottonmc.cotton_scripting.impl.EntityWorldStorage;
import io.github.cottonmc.cotton_scripting.impl.GlobalWorldStorage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

/**
 * A tool to read and write persistent data for the world and entities.
 */
public class WorldStorage {

	/**
	 * Get a value saved to the world, independent from any entities.
	 * @param world Pass CottonScriptContext.getCommandWorld() here.
	 * @param name The name of the value. Will create the value if it doesn't yet exist.
	 * @return The value for the world. Returns 0 if the value has never been set.
	 */
	@Nullable
	public static Object getGlobalValue(ServerWorld world, String name) {
		return world.getPersistentStateManager().getOrCreate(GlobalWorldStorage::new, "global_world_storage").get(name);
	}

	/**
	 * Set a value saved to the world, indepedent from any entities.
	 * Also runs any scripts in the "cotton:scorebase_listeners" tag.
	 * @param world Pass CottonScriptContext.getCommandWorld() here.
	 * @param name The name of the value. Will create the value if it doesn't yet exist.
	 * @param value The value to set.
	 */
	public static void setGlobalValue(ServerWorld world, String name, Object value) {
		world.getPersistentStateManager().getOrCreate(GlobalWorldStorage::new, "global_world_storage").put(name, value);
		listen(world.getServer());
	}

	/**
	 * Get a value saved to the world, tied to a specific entity.
	 * Also runs any scripts in the "cotton:scorebase_listeners" tag.
	 * @param source The command source of the entity to check the score of. Must have an entity associated.
	 * @param name The name of the value. Will create the value if it doesn't yet exist.
	 * @return The value for the entity. Returns 0 if the value has never been set for this entity.
	 */
	public static Object getEntityValue(ServerCommandSource source, String name) {
		System.out.println(source.getEntity());
		if (source.getEntity() == null) throw new IllegalArgumentException("Must have an Entity to get a value for!");
		String uuid = source.getEntity().getUuidAsString();
		return source.getWorld().getPersistentStateManager().getOrCreate(EntityWorldStorage::new, "entity_world_storage").get(name, uuid);
	}

	/**
	 * Set a value saved to the world, tied to a specific entity.
	 * @param source The command source of the entity to set the value for. Must have an entity associated.
	 * @param name The name of the value. Will create the value if it doesn't yet exist.
	 * @param value The value to set for the entity.
	 */
	public static void setEntityValue(ServerCommandSource source, String name, Object value) {
		if (source.getEntity() == null) throw new IllegalArgumentException("Must have an Entity to set a value for!");
		String uuid = source.getEntity().getUuidAsString();
		source.getWorld().getPersistentStateManager().getOrCreate(EntityWorldStorage::new, "entity_world_storage").put(name, uuid, value);
		listen(source.getMinecraftServer(), source);
	}

	private static void listen(MinecraftServer server) {
		listen(server, server.getCommandSource());
	}

	private static void listen(MinecraftServer server, ServerCommandSource source) {
		CommandFunctionManager manager = server.getCommandFunctionManager();
		Tag<CommandFunction> listenTag = manager.getTags().getOrCreate(new Identifier(CottonScripting.MODID, "storage_listen"));
		for (CommandFunction command : listenTag.values()) {
			manager.execute(command, source);
		}
	}
}
