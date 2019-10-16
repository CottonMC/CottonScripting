package io.github.cottonmc.cotton_scripting.impl;

import net.minecraft.text.Text;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class ScriptCommandExecutor implements CommandOutput {

	private World world;

	public ScriptCommandExecutor(World world) {
		this.world = world;
	}

	@Override
	public void sendMessage(Text message) {

	}

	@Override
	public boolean sendCommandFeedback() {
		return this.world.getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT);
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
