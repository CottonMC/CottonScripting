package io.github.cottonmc.cotton_scripting;

import io.github.cottonmc.cotton_scripting.api.CottonScriptContext;
import net.minecraft.util.Identifier;

public interface ExecutableScript {

    /**
     * Runs the script's main method.
     *
     * @return false, if the script has errored.
     * */
    boolean runMain(CottonScriptContext commandSource);

    public Identifier getID();

}
