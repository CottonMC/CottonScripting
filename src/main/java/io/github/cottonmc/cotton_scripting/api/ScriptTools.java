package io.github.cottonmc.cotton_scripting.api;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.impl.ScriptCommandExecutor;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A toolset for performing common actions without the need for accessing obfuscated code.
 */
public class ScriptTools {
	private static final float DEGREE = (float)Math.toRadians(1);
	private static final float PI = (float)Math.PI;

	/**
	 * Lets you call localizations from lang files.
	 * @param key The localization key to use.
	 * @param objects Any objects to add for formatting.
	 * @return The translated text with objects added in.
	 */
	public static String translate(String key, Object...objects) {
		return new TranslatableComponent(key, objects).getText();
	}

	/**
	 * Get the localized name of a registered object.
	 * @param id The ID of the object.
	 * @param type The type of object.
	 * @return The localized name.
	 */
	public static String getTranslatedName(String id, String type) {
		Identifier formattedId = new Identifier(id);
		return new TranslatableComponent(type + "." + formattedId.getNamespace() + "." + formattedId.getPath()).getText();
	}

	/**
	 * Get the block at the end of a raytrace from an entity's position and look vector.
	 * @param source The source to raytrace from.
	 * @param limit The maximum number of blocks to project.
	 * @return The resulting block position and block state of the raytrace.
	 */
	public static RayTraceResult rayTraceFromLook(ServerCommandSource source, double limit) {
		return rayTrace(source, source.getPosition().x, source.getPosition().y + source.getEntity().getEyeHeight(source.getEntity().getPose()), source.getPosition().z, source.getEntity().getPitch(1f), source.getEntity().getHeadYaw(), limit);
	}

	/**
	 * Get the block at the end of a raytrace from an arbitrary position and look vector.
	 * @param source The command source to run from (to obtain the world and entity to run from).
	 * @param x The X position to start from.
	 * @param y The Y position to start from.
	 * @param z The Z position to start from.
	 * @param pitch The pitch of the look vector to use.
	 * @param yaw The yaw of the look vector to use.
	 * @param limit The maximum number of blocks to project.
	 * @return The resulting block position and block state of the raytrace.
	 */
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

	/**
	 * Obtain the command source to emulate running from a server command line.
	 * @param world The world to execute in.
	 * @return a wrapped source of the server, with no entity associated with it.
	 */
	public static ServerCommandSource getServerExecutor(ServerWorld world) {
		return new ServerCommandSource(MinecraftServer.DUMMY,
				Vec3d.ZERO,
				Vec2f.ZERO,
				world,
				2,
				"Server",
				new TextComponent("Server"),
				world.getServer(),
				null);
	}

	/**
	 * Obtain a command source from a player's name.
	 * @param original The original command source to obtain world, executor and fallback from.
	 * @param name The name of the player to execute as.
	 * @return A wrapped source of the player, or the original source if the player wasn't found.
	 */
	public static ServerCommandSource getPlayerExecutor(ServerCommandSource original, String name) {
		Stream<ServerPlayerEntity> players = PlayerStream.all(original.getMinecraftServer());
		for (Object obj : players.toArray()) {
			ServerPlayerEntity player = (ServerPlayerEntity)obj;
			if (player.getName().getText().equals(name)) return new ServerCommandSource(new ScriptCommandExecutor(player.getEntityWorld()),
					player.getPos(),
					player.getRotationClient(),
					(ServerWorld)player.getEntityWorld(),
					2,
					player.getDisplayName().getText(), player.getDisplayName(),
					player.getEntityWorld().getServer(),
					player);
		}
		original.sendError(new TranslatableComponent("error.cotton-scripting.no_executor"));
		return original;
	}

	/**
	 * Obtain a new command source to execute from, based on an Entity UUID.
	 * @param original The original command source to obtain world, executor and fallback from.
	 * @param entityUuid The UUID of the entity to use.
	 * @return A wrapped source of the entity called, or the original source if the entity wasn't found.
	 */
	public static ServerCommandSource getExecutorFromUuid(ServerCommandSource original, String entityUuid) {
		Entity entity = original.getWorld().getEntity(UUID.fromString(entityUuid));
		if (entity == null) {
			original.sendError(new TranslatableComponent("error.cotton-scripting.no_executor"));
			return original;
		}
		return new ServerCommandSource(new ScriptCommandExecutor(entity.getEntityWorld()),
				entity.getPos(),
				entity.getRotationClient(),
				(ServerWorld)entity.getEntityWorld(),
				2,
				entity.getDisplayName().getText(), entity.getDisplayName(),
				entity.getEntityWorld().getServer(),
				entity);
	}

	/**
	 * Obtain a new command source to execute from, based on an Entity selector.
	 * @param original The original command source to obtain world, executor and fallback from.
	 * @param options The target selectors to obtain an entity with, as would be called from an @e selector in a command
	 * @see <a href="https://minecraft.gamepedia.com/Commands#Target_selector_arguments">Target selector options</a>
	 * @return The wrapped source of the entity found, or the original source if no entity matching the selector was found.
	 */
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
