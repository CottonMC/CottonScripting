package io.github.cottonmc.cotton_scripting.api;

import com.mojang.brigadier.context.CommandContext;
import io.github.cottonmc.cotton_scripting.impl.ScriptCommandExecutor;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.script.CompiledScript; //TODO: Upgrade to Parchment

/**
 * An object storing various context about how a script was called.
 * REMEMBER: Any Minecraft classes will be obfuscated in a production environment! Don't call Minecraft classes or methods from scripts!
 * Running vanilla commands will do most of you would want to do for you.
 * If you *absolutely* need to use MC classes, write a plugin mod.
 * TODO: big changes for cotton scripting 2.0
 */
public class CottonScriptContext {
	private CompiledScript script; //TODO: Upgrade to Parchment
	private CommandContext<ServerCommandSource> commandContext;
	private ServerCommandSource commandSource;
	private ServerWorld commandWorld;
	private BlockPos commandPosition;
	private Identifier scriptId;

	public CottonScriptContext(CompiledScript script, Identifier scriptId) {
		this.script = script;
		this.scriptId = scriptId;
	}

	public CompiledScript getScript() {
		return script;
	}

	public CottonScriptContext withContext(CommandContext<ServerCommandSource> context) {
		this.commandContext = context;
		this.commandSource = context.getSource();
		this.commandWorld = context.getSource().getWorld();
		this.commandPosition = new BlockPos(context.getSource().getPosition());
		return this;
	}
	
	public CottonScriptContext withSource(ServerCommandSource source) {
		this.commandContext = null;
		this.commandSource = source;
		this.commandWorld = source.getWorld();
		this.commandPosition = new BlockPos(source.getPosition());
		return this;
	}

	/**
	 * Change the ServerCommandSource of this context.
	 * @param source The source to set to.
	 * @return This object with the source, world, and position changed to match the new source.
	 */
	public CottonScriptContext runBy(ServerCommandSource source) {
		this.commandSource = source;
		this.commandWorld = source.getWorld();
		this.commandPosition = new BlockPos(source.getPosition());
		return this;
	}

	/**
	 * DO NOT CALL FROM SCRIPT. Only here for the sake of plug-ins. Pass this on to compiled methods ONLY.
	 * @return The vanilla command context a script-call command was run with.
	 */
	@Nullable
	public CommandContext<ServerCommandSource> getCommandContext() {
		return commandContext;
	}

	/**
	 * DO NOT CALL FROM SCRIPT. Only here for the sake of plug-ins. Pass this on to compiled methods ONLY.
	 * @return The source that ran a script-call command.
	 */
	public ServerCommandSource getCommandSource() {
		return commandSource;
	}

	/**
	 * DO NOT CALL FROM SCRIPT. Only here for the sake of plug-ins. Pass this on to compiled methods ONLY.
	 * @return The world that a script-call command was run from.
	 */
	public World getCommandWorld() {
		return commandWorld;
	}

	/**
	 * @return The ID of the dimension a script was called from.
	 */
	public String getCommandDimension() {
		return Registry.DIMENSION_TYPE.getId(commandWorld.dimension.getType()).toString();
	}

	/**
	 * @return The UUID of the entity that called a script. Returns an empty string if the entity is null (like a command block)
	 */
	public String getCallerUuid() {
		Entity caller = commandSource.getEntity();
		if (caller == null) return "";
		return caller.getUuidAsString();
	}

	public String getCallerName() {
		return commandSource.getName();
	}

	/**
	 * @return The X, Y, and Z position that a script was called at. Will be [0, 0, 0] if run from the server or a tick/load tag.
	 */
	public int[] getCommandPosition() {
		return new int[]{commandPosition.getX(), commandPosition.getY(), commandPosition.getZ()};
	}

	/**
	 * Send a command feedback component to the script caller.
	 * @param feedback The message to send.
	 * @param sendToStatusBar If false, this will appear in the caller's chat box.
	 */
	public void sendFeedback(String feedback, boolean sendToStatusBar) {
		commandSource.sendFeedback(new LiteralText(feedback), sendToStatusBar);
	}

	/**
	 * Send an error feedback component to the script caller.
	 * @param error The message to send.
	 */
	public void sendError(String error) {
		commandSource.sendError(new LiteralText(error));
	}

	/**
	 * Run a vanilla command from the script.
	 * @param command The command to run, with or without the leading /.
	 * @return Whether the command returned successfully.
	 */
	public boolean runCommand(String command) {
		MinecraftServer server = commandWorld.getServer();
		if (server.hasGameDir() && !ChatUtil.isEmpty(command)) {
			//this is *very* ugly, but lambdas require finality, so what can ya do
			final boolean[] successful = {false};
			try {
				ServerCommandSource source = new ServerCommandSource(
						new ScriptCommandExecutor(commandSource.getWorld()),
						commandSource.getPosition(),
						Vec2f.ZERO,
						commandWorld,
						2,
						scriptId.toString(),
						new LiteralText(scriptId.toString()),
						commandWorld.getServer(),
						null)
						.withConsumer(((context, success, result) -> {
							if (success) successful[0] = true;
						}));
				server.getCommandManager().execute(source, command);
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
