package io.github.cottonmc.cotton_scripting.api;

import com.mojang.brigadier.context.CommandContext;
import io.github.cottonmc.cotton_scripting.api.entity.Entity;
import io.github.cottonmc.cotton_scripting.api.exception.EntityNotFoundException;
import io.github.cottonmc.cotton_scripting.api.world.Dimension;
import io.github.cottonmc.cotton_scripting.api.world.World;
import io.github.cottonmc.cotton_scripting.impl.ScriptCommandExecutor;
import io.github.cottonmc.parchment.api.SimpleCompilableScript;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * An object storing various context about how a script was called.
 * REMEMBER: Any Minecraft classes will be obfuscated in a production environment! Don't call Minecraft classes or methods from scripts!
 * Running vanilla commands will do most of you would want to do for you.
 * If you *absolutely* need to use MC classes, write a plugin mod.
 * TODO: Register game events (block break, item used, block placed, etc.), check if there are any event listeners, then bind the listeners to their events.
 */
public class CottonScript extends SimpleCompilableScript {
	protected CommandContext<ServerCommandSource> context;
	protected ServerSource source;
	protected ServerWorld world;
	protected BlockPos position;
	protected Identifier scriptId;

	private String errorMessage = "";

	private static final Object[] globals = {Entity.class, Dimension.class, World.class, ScriptTools.class, WorldStorage.class};

	public CottonScript(ScriptEngine engine, Identifier name, String contents) {
		super(engine, name, contents);
	}

	@Override
	public void run() {
		// Iterate through every global object
		for (Object c : globals) {
			// Define a new variable with the current Object's name and
			getEngine().getContext().setAttribute(c.getClass().getName(), c, ScriptContext.ENGINE_SCOPE);
			//TODO: After Parchment update, change method above to script.setVar
		}

		if (!this.hadCompileError) {
			try {
				this.compiled.eval();
			} catch (Exception e) {
				this.getLogger().error("Script {} encountered error while running: {}", this.getId().toString(), e.getMessage());
				e.printStackTrace();
				this.errorMessage = e.getMessage();
				this.hadError = true;
			}

			this.hasRun = true;
		} else {
			this.getLogger().error("Script {} could not be run because it failed while compiling", this.getId().toString());
		}
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public CottonScript withContext(CommandContext<ServerCommandSource> ctx) {
		context = ctx;
		source = new ServerSource(ctx.getSource());
		return this;
	}
	
	public CottonScript withSource(ServerCommandSource src) {
		context = null;
		source = new ServerSource(src);
		return this;
	}

	/**
	 * Change the ServerCommandSource of this context.
	 * @param src The source to set to.
	 * @return This object with the source, world, and position changed to match the new source.
	 */
	public CottonScript runBy(ServerSource src) {
		source = src;
		return this;
	}

	/**
	 * @return The source that ran a script-call command.
	 * @see ServerSource
	 */
	public ServerSource getSource() {
		return source;
	}

	/**
	 * <p style="font-weight:bold;font-size:120%">DO NOT CALL FROM SCRIPT.</p> Only here for the sake of plug-ins. Pass this on to compiled methods ONLY.
	 * @return The world that a script-call command was run from.
	 * @deprecated since 2.0.0
	 */
	@Deprecated
	public World getCommandWorld() {
		return source.getWorld();
	}

	/**
	 * @return The ID of the dimension a script was called from.
	 * @deprecated since 2.0.0
	 */
	@Deprecated
	public String getCommandDimension() {
		return source.getWorld().getDimension().getName();
	}

	/**
	 * @return The UUID of the entity that called a script. Returns an empty string if the entity is null (like a command block)
	 * @deprecated since 2.0.0
	 */
	@Deprecated
	public String getCallerUuid() throws EntityNotFoundException {
		return source.getEntity().getUuid();
	}
	
	/**
	 * @return The name of the entity that called the script.
	 * @deprecated since 2.0.0
	 */
	@Deprecated
	public String getCallerName() {
		return source.getName();
	}

	/**
	 * @return The X, Y, and Z position that a script was called at. Will be [0, 0, 0] if run from the server or a tick/load tag.
	 * @deprecated since 2.0.0
	 */
	@Deprecated
	public int[] getCommandPosition() throws EntityNotFoundException {
		Vec3d pos = source.getEntity().getPosition();
		
		return new int[]{(int) pos.getX(), (int) pos.getY(), (int) pos.getZ()};
	}

	/**
	 * Send a command feedback component to the script caller.
	 * @see CottonScript#sendFeedback(String)
	 * @param feedback The message to send.
	 * @param sendToStatusBar If false, this will appear in the caller's chat box.
	 */
	public void sendFeedback(String feedback, boolean sendToStatusBar) {
		source.getSource().sendFeedback(new LiteralText(feedback), sendToStatusBar);
	}
	
	/**
	 * Send a command feedback component to the script caller.
	 * @overload
	 * @see CottonScript#sendFeedback(String, boolean)
	 * @param feedback The message to send.
	 */
	public void sendFeedback(String feedback) {
		sendFeedback(feedback, false);
	}

	/**
	 * Send an error feedback component to the script caller.
	 * @param error The message to send.
	 */
	public void sendError(String error) {
		source.getSource().sendError(new LiteralText(error));
	}

	/**
	 * Run a vanilla command from the script.
	 * @param command The command to run, with or without the leading /.
	 * @return Whether the command returned successfully.
	 */
	public boolean runCommand(String command) {
		MinecraftServer server = source.getSource().getMinecraftServer();
		if (server.hasGameDir() && !ChatUtil.isEmpty(command)) {
			//this is *very* ugly, but lambdas require finality, so what can ya do
			final boolean[] successful = {false};
			try {
				ServerCommandSource src = new ServerCommandSource(
						new ScriptCommandExecutor(source.getSource().getWorld()),
						source.getEntity().getPosition(),
						Vec2f.ZERO,
						source.getSource().getWorld(),
						2,
						scriptId.toString(),
						new LiteralText(scriptId.toString()),
						source.getSource().getMinecraftServer(),
						null)
						.withConsumer(((context, success, result) -> {
							if (success) successful[0] = true;
						}));
				server.getCommandManager().execute(source.getSource(), command);
			} catch (Throwable t) {
				CrashReport report = CrashReport.create(t, "Executing command from script");
				CrashReportSection executed = report.addElement("Command to be executed");
				executed.add("Command", command);
				CrashReportSection caller = report.addElement("Script command called from");
				caller.add("Script ID", this.scriptId.toString());
				throw new CrashException(report);
			}
			return successful[0];
		}
		return false;
	}
}
