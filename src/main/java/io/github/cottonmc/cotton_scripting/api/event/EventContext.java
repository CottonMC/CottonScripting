package io.github.cottonmc.cotton_scripting.api.event;

import io.github.cottonmc.cotton_scripting.api.CottonScript;

import net.minecraft.util.Identifier;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;

/**
 * The
 * @see EventEmitter
 * @extends {@link CottonScript}
 */
public class EventContext extends CottonScript {
	public EventContext(ScriptEngine engine, Identifier name, String contents) {
		super(engine, name, contents);
	}

	//TODO: Add more functionality
}
