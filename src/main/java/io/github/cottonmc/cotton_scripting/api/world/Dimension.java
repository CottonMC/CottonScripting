package io.github.cottonmc.cotton_scripting.api.world;

public class Dimension {
	protected net.minecraft.world.dimension.Dimension dimension;
	
	public Dimension(net.minecraft.world.dimension.Dimension dim) {
		dimension = dim;
	}
	
	/**
	 * Get name.
	 * @return {@code String} | Name.
	 */
	public String getName() {
		return dimension.getType().toString();
	}
	
	/**
	 * Is in the nether.
	 * @return {@code boolean} | In nether.
	 */
	public boolean isNether() {
		return dimension.isNether();
	}
	
	/**
	 * Can players sleep.
	 * @return {@code boolean} | Can players sleep.
	 */
	public boolean canPlayersSleep() {
		return dimension.canPlayersSleep();
	}
	
	//TODO: Add more functionality
}
