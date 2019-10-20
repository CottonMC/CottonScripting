package io.github.cottonmc.cotton_scripting.event;

import io.github.cottonmc.functionapi.api.EventRunner;
import io.github.cottonmc.functionapi.api.EventRunnerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class ScriptedEventRunnerFactory implements EventRunnerFactory<MinecraftServer, ServerCommandSource> {
    @Override
    public EventRunner<MinecraftServer, ServerCommandSource> newEvent(io.github.cottonmc.functionapi.api.Identifier id) {

        return new ScriptedEventRunner(new Identifier(id.getNamespace(),id.getPath()));
    }
}
