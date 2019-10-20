package io.github.cottonmc.cotton_scripting.api;

import io.github.cottonmc.cotton_scripting.ExecutableScript;
import net.minecraft.server.command.CommandSource;

import javax.script.Invocable;
import javax.script.ScriptException;

public class InvocableScript implements ExecutableScript {

    private final Invocable invocable;

    public InvocableScript(Invocable invocable) {
        this.invocable = invocable;
    }

    @Override
    public boolean runMain(CommandSource commandSource) {
        try {
            invocable.invokeFunction("main",commandSource);
            return true;
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }
}
