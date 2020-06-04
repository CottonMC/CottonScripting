package io.github.cottonmc.cotton_scripting.api.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.api.exception.EntityNotFoundException;
import io.github.cottonmc.cotton_scripting.api.world.World;
import net.minecraft.server.command.ServerCommandSource;

public class EntitySource {
	protected ServerCommandSource source;
	
	public EntitySource(ServerCommandSource src) {
		source = src;
	}
	
	/**
	 * Returns a boolean which is true if the current source is an entity, otherwise false.
	 * @return {@code boolean} | True if current source is an entity, otherwise false.
	 */
	public boolean isEntity() {
		return source.getEntity() != null;
	}
	
	/**
	 * Returns a boolean which is true if the current source is a player, otherwise false.
	 * @return {@code boolean} | True if current source is a player, otherwise false.
	 */
	public boolean isPlayer() {
		try {
			return source.getPlayer() != null;
		} catch(CommandSyntaxException e) {
			return false;
		}
	}
	
	/**
	 * Get the world object.
	 * @return The world that a script-call command was run from.
	 */
	public World getWorld() {
		return new World(source.getWorld());
	}
	
	/**
	 * Get name.
	 * @return {@code String} | Name.
	 */
	public String getName() {
		return source.getName();
	}
	
	/**
	 * Get display name.
	 * @return {@code String} | Display name.
	 */
	public String getDisplayName() {
		return source.getDisplayName().asFormattedString();
	}
	
	/**
	 * Get entity object.
	 * @return {@link Entity} | Entity object.
	 * @throws EntityNotFoundException Thrown when the source is not an entity or {@link ServerCommandSource#getEntity() source.getEntity()} returns null.
	 */
	public Entity getEntity() throws EntityNotFoundException {
		if (source.getEntity() != null) {
			return new Entity(source.getEntity());
		} else {
			throw new EntityNotFoundException();
		}
	}
}
