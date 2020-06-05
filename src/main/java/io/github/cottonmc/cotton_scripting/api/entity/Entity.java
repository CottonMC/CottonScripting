package io.github.cottonmc.cotton_scripting.api.entity;

import io.github.cottonmc.cotton_scripting.api.ServerSource;
import io.github.cottonmc.cotton_scripting.api.world.Dimension;
import io.github.cottonmc.cotton_scripting.api.world.World;
import net.minecraft.util.math.Vec3d;

public class Entity extends ServerSource implements EntityInterface {
	protected net.minecraft.entity.Entity entity;
	
	public Entity(net.minecraft.entity.Entity ent) {
		super(ent.getCommandSource());
		
		entity = ent;
	}
	
	@Override
	public Dimension getDimension() {
		return getWorld().getDimension();
	}
	
	@Override
	public World getWorld() {
		return new World(entity.getEntityWorld());
	}
	
	@Override
	public String getUuid() {
		return entity.getUuid().toString();
	}
	
	@Override
	public void setPosition(Vec3d pos) {
		entity.setPos(pos.x, pos.y, pos.z);
	}
	
	@Override
	public Vec3d getPosition() {
		return entity.getPos();
	}
}
