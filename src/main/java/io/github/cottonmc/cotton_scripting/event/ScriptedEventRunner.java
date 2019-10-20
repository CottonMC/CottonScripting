package io.github.cottonmc.cotton_scripting.event;

import io.github.cottonmc.cotton_scripting.ExecutableScript;
import io.github.cottonmc.cotton_scripting.impl.ScriptTags;
import io.github.cottonmc.functionapi.api.EventRunner;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class ScriptedEventRunner implements EventRunner<MinecraftServer, ServerCommandSource> {
    private Collection<ExecutableScript> scripts;
    private final Identifier identifier;

    private boolean needsReload = true;

    ScriptedEventRunner(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public void fire(ServerCommandSource serverCommandSource) {
        reload(serverCommandSource.getMinecraftServer());
        for (ExecutableScript script : scripts) {
            script.runMain(serverCommandSource);
        }
    }

    @Override
    public void markDirty() {
        needsReload = true;
    }

    @Override
    public void reload(MinecraftServer server) {
        if(needsReload) {
            scripts = ScriptTags.getContainer().get(identifier).values();
            needsReload = false;
        }
    }

    @Override
    public boolean hasEvents() {
        return scripts.size() >0;
    }
}
