package io.github.cottonmc.cotton_scripting;

import net.minecraft.network.chat.Component;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

public class ScriptCommandExecutor implements CommandOutput {
	private ServerCommandSource source;

	public ScriptCommandExecutor(ServerCommandSource source) {
		this.source = source;
	}

	@Override
	public void sendMessage(Component message) {

	}

	@Override
	public boolean sendCommandFeedback() {
		return source.getWorld().getGameRules().getBoolean("commandBlockOutput");
	}

	@Override
	public boolean shouldTrackOutput() {
		return false;
	}

	@Override
	public boolean shouldBroadcastConsoleToOps() {
		return true;
	}
}
