package io.github.cottonmc.cotton_scripting.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The result of a raytrace called from a script.
 */
public class RayTraceResult {
	private BlockPos pos;
	private BlockState state;
	public static final RayTraceResult ZERO = new RayTraceResult(BlockPos.ORIGIN, Blocks.AIR.getDefaultState());

	public RayTraceResult(BlockPos pos, BlockState state) {
		this.pos = pos;
		this.state = state;
	}

	/**
	 * @return Whether the raytrace ended at an air block or not.
	 */
	public boolean isAir() {
		return state.isAir();
	}

	/**
	 * @return An int array of the X, Y, and Z position of the raytrace endpoint.
	 */
	public int[] getPosition() {
		return new int[]{pos.getX(), pos.getY(), pos.getZ()};
	}

	/**
	 * @return The X position of the raytrace endpoint.
	 */
	public int getXPosition() {
		return pos.getX();
	}

	/**
	 * @return The Y positon of the raytrance endpoint.
	 */
	public int getYPosition() {
		return pos.getY();
	}

	/**
	 * @return The Z position of the raytrace endpoint.
	 */
	public int getZPosition() {
		return pos.getZ();
	}

	/**
	 * @return The ID of the block at the raytrace endpoint.
	 */
	public String getBlockId() {
		return Registry.BLOCK.getId(state.getBlock()).toString();
	}

	/**
	 * @return The properties stored by the blockstate at the raytrace endpoint.
	 */
	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		Collection<Property<?>> props = state.getProperties();
		for (Property<?> prop : props) {
			String name = prop.getName();
			Optional<?> value = prop.parse(name);
			Object res = "";
			if (value.isPresent()) res = value.get();
			properties.put(name, res.toString());
		}
		return properties;
	}
}
