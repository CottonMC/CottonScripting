package io.github.cottonmc.cotton_scripting.api;

import io.github.cottonmc.cotton_scripting.ExecutableScript;
import net.minecraft.util.Identifier;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class InvocableScript implements ExecutableScript {

    private final ScriptEngine invocable;
    private final Identifier identifier;


    public InvocableScript(ScriptEngine engine, Identifier identifier) {
        this.invocable = engine;
        this.identifier = identifier;
    }

    @Override
    public boolean runMain(CottonScriptContext commandSource) {
        try {
            ((Invocable)invocable).invokeFunction("main",commandSource);
            return true;
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Identifier getID() {
        return identifier;
    }
}
