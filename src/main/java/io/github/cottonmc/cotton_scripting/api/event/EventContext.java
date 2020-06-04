package io.github.cottonmc.cotton_scripting.api.event;

import io.github.cottonmc.cotton_scripting.api.CottonScriptContext;
import io.github.cottonmc.parchment.api.CompilableScript;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

/**
 * The
 * @see EventEmitter
 * @extends {@link CottonScriptContext}
 */
public class EventContext extends CottonScriptContext {
	public EventContext(CompilableScript script, Identifier scriptId) {
		super(script, scriptId);
	}
	
	//TODO: Add more functionality
}
