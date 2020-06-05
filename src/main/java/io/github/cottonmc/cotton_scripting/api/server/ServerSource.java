package io.github.cottonmc.cotton_scripting.api.server;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.api.entity.Entity;
import io.github.cottonmc.cotton_scripting.api.exception.EntityNotFoundException;
import io.github.cottonmc.cotton_scripting.api.world.World;
import net.minecraft.server.command.ServerCommandSource;

import javax.annotation.Nullable;

public class ServerSource {
	protected ServerCommandSource source;
	
	public ServerSource(ServerCommandSource src) {
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
	 * Get the source name.
	 * @return {@code String} | Name.
	 */
	public String getName() {
		return source.getName();
	}
	
	/**
	 * Get the source display name.
	 * @return {@code String} | Display name.
	 */
	public String getDisplayName() {
		return source.getDisplayName().asFormattedString();
	}
	
	/**
	 * Get the entity object.
	 * @return {@link Entity} | Entity object.
	 * @see ServerSource#getEntityOrThrow()
	 */
	@Nullable
	public Entity getEntity() {
		return new Entity(source.getEntity());
	}
	
	/**
	 * Get the entity object or throw an error if null.
	 * @return {@link Entity} | Entity object.
	 * @see ServerSource#getEntity()
	 * @throws EntityNotFoundException Thrown when the source is not an entity or {@link ServerCommandSource#getEntity()} throws an {@link EntityNotFoundException}.
	 */
	public Entity getEntityOrThrow() throws EntityNotFoundException {
		if (isEntity()) {
			return new Entity(source.getEntity());
		} else {
			throw new EntityNotFoundException();
		}
	}
	
	/**
	 * <p style="font-weight:bold;font-size:120%">DO NOT CALL FROM SCRIPT.</p> Only here to be used by plug-ins and the CottonScripting API.
	 * Get the source object.
	 * @return {@link ServerCommandSource} | Source object.
	 */
	public ServerCommandSource getSource() {
		return source;
	}
}
