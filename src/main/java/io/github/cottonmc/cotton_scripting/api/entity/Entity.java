package io.github.cottonmc.cotton_scripting.api.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.api.world.Dimension;
import io.github.cottonmc.cotton_scripting.api.world.World;
import io.github.cottonmc.cotton_scripting.impl.entity.EntityImpl;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

public class Entity implements EntityImpl {
	protected net.minecraft.entity.Entity entity;
	
	public Entity(net.minecraft.entity.Entity ent) {
		entity = ent;
	}
	
	@Override
	public boolean isEntity() {
		return entity != null;
	}
	
	@Override
	public boolean isPlayer() {
		try {
			return entity.getCommandSource().getPlayer() != null;
		} catch(CommandSyntaxException e) {
			return false;
		}
	}
	
	@Override
	public String getName() {
		return entity.getName().asFormattedString();
	}
	
	@Override
	public String getDisplayName() {
		return entity.getDisplayName().asFormattedString();
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
	
	public io.github.cottonmc.cotton_scripting.api.entity.Entity getEntity() {
		return new io.github.cottonmc.cotton_scripting.api.entity.Entity(entity);
	}
	
	public static Entity getEntityFromSource(ServerCommandSource src) {
		return new Entity(src.getEntity());
	}
}
