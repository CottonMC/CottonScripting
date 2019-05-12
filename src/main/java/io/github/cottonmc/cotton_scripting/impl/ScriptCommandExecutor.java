package io.github.cottonmc.cotton_scripting.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.world.World;

public class ScriptCommandExecutor implements CommandOutput {

	private World world;

	public ScriptCommandExecutor(World world) {
		this.world = world;
	}

	@Override
	public void sendMessage(Component message) {

	}

	@Override
	public boolean sendCommandFeedback() {
		return this.world.getGameRules().getBoolean("commandBlockOutput");
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
