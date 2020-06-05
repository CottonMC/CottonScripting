package io.github.cottonmc.cotton_scripting.api.entity;

import io.github.cottonmc.cotton_scripting.api.world.Dimension;
import io.github.cottonmc.cotton_scripting.api.world.World;
import net.minecraft.util.math.Vec3d;

public interface EntityInterface {
	/**
	 * Returns a boolean which is true if the current source is an entity, otherwise false.
	 * @return {@code boolean} | True if current source is an entity, otherwise false.
	 */
	boolean isEntity();
	
	/**
	 * Returns a boolean which is true if the current source is a player, otherwise false.
	 * @return {@code boolean} | True if current source is a player, otherwise false.
	 */
	boolean isPlayer();
	
	/**
	 * Get entity name.
	 * @return {@code String} | Entity name.
	 */
	String getName();
	
	/**
	 * Get entity display name.
	 * @return {@code String} | Entity display name.
	 */
	String getDisplayName();
	
	/**
	 * Get the dimension the entity is in.
	 * @return {@link Dimension Dimension} | The dimension the entity is in.
	 */
	Dimension getDimension();
	
	/**
	 * Get the world the entity is in.
	 * @return {@link World World} | The world the entity is in.
	 */
	World getWorld();
	
	/**
	 * Get the entity's UUID.
	 * @return {@code String} | The entity's UUID.
	 */
	String getUuid();
	
	/**
	 * Set the entity's position.
	 * @param pos Position
	 */
	void setPosition(Vec3d pos);
	
	/**
	 * Get the entity's position.
	 * @return {@link Vec3d} | The entity's position.
	 */
	Vec3d getPosition();
}
