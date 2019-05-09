package io.github.cottonmc.cotton_scripting;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.World;

public class ScriptContext {
	private CommandContext<ServerCommandSource> commandContext;
	private ServerCommandSource commandSource;
	private ServerWorld commandWorld;
	private BlockPos commandPosition;
	private Identifier script;
	private String[] arguments;

	public ScriptContext(CommandContext<ServerCommandSource> context, Identifier script, String...arguments) {
		this.commandContext = context;
		this.commandSource = context.getSource();
		this.commandWorld = context.getSource().getWorld();
		this.commandPosition = new BlockPos(context.getSource().getPosition());
		this.script = script;
		this.arguments = arguments;
	}

	public CommandContext<ServerCommandSource> getCommandContext() {
		return commandContext;
	}

	public ServerCommandSource getCommandSource() {
		return commandSource;
	}

	public World getCommandWorld() {
		return commandWorld;
	}

	public BlockPos getCommandPosition() {
		return commandPosition;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void sendFeedBack(String feedback, boolean sendToStatusBar) {
		commandSource.sendFeedback(new TextComponent(feedback), sendToStatusBar);
	}

	public void sendError(String error) {
		commandSource.sendError(new TextComponent(error));
	}

	public boolean runCommand(String command) {
		MinecraftServer server = commandWorld.getServer();
		if (server.method_3814() && !ChatUtil.isEmpty(command)) {
			final boolean[] successful = {false};
			try {
				ServerCommandSource source = new ServerCommandSource(
						new ScriptCommandExecutor(commandSource),
						commandSource.getPosition(),
						Vec2f.ZERO,
						commandWorld,
						2,
						script.toString(),
						new TextComponent(script.toString()),
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
				caller.add("Script ID", this.script.toString());
				throw new CrashException(report);
			}
			return successful[0];
		}
		return false;
	}
}
