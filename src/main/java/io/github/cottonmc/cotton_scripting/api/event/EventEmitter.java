package io.github.cottonmc.cotton_scripting.api.event;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public class EventEmitter {
	public Map<String, Function<Object, Object>> events = new Hashtable<>();
	
	public void emit(String k, Function<Object, Object> v) {
		events.put(k, v);
	}
}
