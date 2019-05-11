package io.github.cottonmc.cotton_scripting.api;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.impl.ScriptCommandExecutor;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

import java.util.UUID;

public class ScriptTools {
	private static final float DEGREE = (float)Math.toRadians(1);
	private static final float PI = (float)Math.PI;

	public static RayTraceResult rayTraceFromLook(ServerCommandSource source, double limit) {
		return rayTrace(source, source.getPosition().x, source.getPosition().y, source.getPosition().z, source.getEntity().getPitch(1f), source.getEntity().getHeadYaw(), limit);
	}

	public static RayTraceResult rayTrace(ServerCommandSource source, double x, double y, double z, float pitch, float yaw, double limit) {
		if (source.getEntity() == null) return RayTraceResult.ZERO;
		Vec3d originPos = new Vec3d(x, y, z);
		float yawCos = MathHelper.cos(-yaw * DEGREE - PI);
		float yawSin = MathHelper.sin(-yaw * DEGREE - PI);
		float pitchCos = -MathHelper.cos(-pitch * DEGREE);
		float pitchSin = MathHelper.sin(-pitch * DEGREE);
		float xOffset = yawSin * pitchCos;
		float zOffset = yawCos * pitchCos;
		Vec3d projected = originPos.add((double)xOffset * limit, (double)pitchSin * limit, (double)zOffset * limit);
		BlockHitResult res = source.getWorld().rayTrace(new RayTraceContext(originPos, projected, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, source.getEntity()));
		return new RayTraceResult(res.getBlockPos(), source.getWorld().getBlockState(res.getBlockPos()));
	}

	public static ServerCommandSource getExecutorFromUuid(ServerCommandSource original, String entityUuid) {
		Entity entity = original.getWorld().getEntity(UUID.fromString(entityUuid));
		if (entity == null) return original;
		return new ServerCommandSource(new ScriptCommandExecutor(original),
				entity.getPos(),
				entity.getRotationClient(),
				(ServerWorld)entity.getEntityWorld(),
				2,
				entity.getDisplayName().toString(), entity.getDisplayName(),
				entity.getEntityWorld().getServer(),
				entity);
	}

	public static ServerCommandSource getExecutorFromSelector(ServerCommandSource original, String options) {
		EntitySelector selector;
		try {
			selector = createEntitySelector(options, false, true);
			Entity result = selector.getEntity(original);
			return getExecutorFromUuid(original, result.getUuidAsString());
		} catch (CommandSyntaxException e) {
			original.sendError(new TranslatableComponent("error.cotton-scripting.syntax_exception", e.getMessage()));
			return original;
		}

	}

	private static EntitySelector createEntitySelector(String options, boolean playersOnly, boolean singleTarget) throws CommandSyntaxException {
		StringReader reader = new StringReader(options);
		EntitySelector selector = new EntitySelectorReader(reader).read();
		if (selector.getCount() > 1 && singleTarget) {
			if (playersOnly) {
				reader.setCursor(0);
				throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.createWithContext(reader);
			} else {
				reader.setCursor(0);
				throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.createWithContext(reader);
			}
		} else if (selector.includesNonPlayers() && playersOnly && !selector.isSenderOnly()) {
			reader.setCursor(0);
			throw EntityArgumentType.PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION.createWithContext(reader);
		} else {
			return selector;
		}
	}
}
