package io.github.cottonmc.cotton_scripting.api.world;

import io.github.cottonmc.cotton_scripting.api.entity.Entity;

import java.util.UUID;

public class World {
	protected net.minecraft.world.World world;
	
	public World(net.minecraft.world.World w) {
		world = w;
	}
	
	/**
	 * Get dimension.
	 * @return {@link Dimension Dimension} | Dimension
	 */
	public Dimension getDimension() {
		return new Dimension(world.getDimension());
	}
	
	public Entity getPlayerByUuid(String Uuid) {
		Entity player = new Entity(world.getPlayerByUuid(UUID.fromString(Uuid)));
		
		return player;
	}
	
	//TODO: Add more functionality
}
