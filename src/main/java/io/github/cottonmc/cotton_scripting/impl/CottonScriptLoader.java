package io.github.cottonmc.cotton_scripting.impl;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.cottonmc.cotton_scripting.CottonScripting;
import io.github.cottonmc.cotton_scripting.api.CottonScript;
import io.github.cottonmc.parchment.api.*;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CottonScriptLoader {
	// ScriptLoader Instance
	public static final CottonScriptLoader INSTANCE = new CottonScriptLoader();
	
	//loading info
	public static final String RESOURCE_TYPE = "scripts";
	public static final int PATH_PREFIX_LENGTH = RESOURCE_TYPE.length() + 1;
	public static final Logger LOGGER = LogManager.getLogger("Cotton Scripting");
	public static final ScriptLoader.ScriptFactory COTTON_SCRIPT = (engine, id, contents) ->
		!(engine instanceof Compilable) ? null : new CottonScript(engine, id, contents);

	private Map<Identifier, CottonScript> SCRIPTS = new HashMap<>();
	public SuggestionProvider<ServerCommandSource> SCRIPT_SUGGESTIONS = SuggestionProviders.register(new Identifier(CottonScripting.MODID, RESOURCE_TYPE),
			(context, builder) -> CommandSource.suggestIdentifiers(SCRIPTS.keySet(), builder));

	public CottonScript getScript(Identifier id) {
		return SCRIPTS.get(id);
	}

	public boolean runScript(Identifier id, CommandContext<ServerCommandSource> context) throws ScriptException {
		// Script and script context
		CottonScript script = getScript(id);
		
		script.withContext(context);
		script.run();
		return script.hadError();
	}

	public boolean runScript(Identifier id, ServerCommandSource source) throws ScriptException {
		// Script and script context
		CottonScript script = getScript(id);

		script.withSource(source);
		script.run();
		return script.hadError();
	}
	
	public List<CompletableFuture<CommandFunction>> load(ResourceManager manager, CommandFunctionManager funcManager, ScriptApplier handler) {
		SCRIPTS.clear();
		List<CompletableFuture<CommandFunction>> futures = new ArrayList<>();
		Collection<Identifier> resources = manager.findResources(RESOURCE_TYPE, (name) -> true);
		for (Identifier id : resources) {
			String path = id.getPath();
			String extension = id.getPath().substring(id.getPath().lastIndexOf('.') + 1);
			Identifier scriptId = new Identifier(id.getNamespace(), path.substring(PATH_PREFIX_LENGTH));
			try {
				Resource res = manager.getResource(id);
				futures.add(CompletableFuture.supplyAsync(() -> readScript(res), ResourceImpl.RESOURCE_IO_EXECUTOR).thenApplyAsync(contents -> {
					if (contents.equals("")) LOGGER.warn("Script {} is empty", scriptId.toString());
					
					CottonScript script = (CottonScript) ScriptLoader.INSTANCE.loadScript(COTTON_SCRIPT, scriptId, contents);
					
					if (script == null) {
						LOGGER.error("Script engine for extension {} is not compilable", extension);
						return null;
					}
					
					SCRIPTS.put(scriptId, script);
					
					List<String> commands = Collections.singletonList("script " + scriptId.toString());
					
					// Create a new function (this script) and parse it as a script
					return CommandFunction.create(id, funcManager, commands);
				}, funcManager.getServer().getWorkerExecutor()).handle((function, throwable) -> { // More function magic
					if (function != null) return handler.load(function, throwable, id);
					else {
						LOGGER.error("Script {} turned up null! That shouldn't happen\nMessage: {}\nCause: {}\nStack Trace:\n{}", id, throwable.getMessage(), throwable.getCause(), throwable.getStackTrace());
						return null;
					}
				}));
			} catch (IOException e) {
				LOGGER.error("Could not load resource {}: {}", id, e.getMessage());
			}
		}
		return futures;
	}

	private String readScript(Resource res) {
		try {
			return IOUtils.toString(res.getInputStream(), Charset.defaultCharset());
		} catch (IOException e) {
			LOGGER.info("IO exception");
			return "";
		}
	}

//	public CompletableFuture<Map<Identifier, String>> load(ResourceManager manager, Profiler profiler, Executor executor) {
//		return CompletableFuture.supplyAsync(() -> {
//			Map<Identifier, String> foundScripts = new HashMap<>();
//			SCRIPTS.clear();
//			Collection<Identifier> resources = manager.findResources("scripts", (name) -> true);
//			for (Identifier fileId : resources) {
//				try {
//					Resource res = manager.getResource(fileId);
//					String script = IOUtils.toString(res.getInputStream(), Charset.defaultCharset());
//					int localPath = fileId.getPath().indexOf('/')+1;
//					Identifier scriptId = new Identifier(fileId.getNamespace(), fileId.getPath().substring(localPath));
//					foundScripts.put(scriptId, script);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			return foundScripts;
//		});
//	}
//
//	public CompletableFuture<Void> apply(Map<Identifier, String> foundScripts, ResourceManager manager, Profiler profiler, Executor executor) {
//		return CompletableFuture.runAsync(() -> {
//			CommandFunctionManager funcManager = null;
//			List<ResourceReloadListener> listeners = ((ResourceManagerImplAccessor)manager).getListeners();
//			for (ResourceReloadListener listener : listeners) {
//				if (listener instanceof CommandFunctionManager) {
//					funcManager = (CommandFunctionManager)listener;
//					break;
//				}
//			}
//			for (Identifier id : foundScripts.keySet()) {
//				String extension = id.getPath().substring(id.getPath().lastIndexOf('.')+1);
//				ScriptEngine engine = CottonScripting.SCRIPT_MANAGER.getEngineByExtension(extension);
//				if (!(engine instanceof Compilable) || !(engine instanceof Invocable)) {
//					LOGGER.info("Script engine is not compilable or is not invocable!");
//					continue;
//				}
//				try {
//					CompiledScript script = ((Compilable)engine).compile(foundScripts.get(id));
//					SCRIPTS.put(id, new CottonScriptContext(script, id));
//					if (funcManager != null) {
//						String path = id.getPath();
//						LOGGER.info("path");
//						Identifier newId = new Identifier(id.getNamespace(), "scripts/" + path.substring(PATH_PREFIX_LENGTH, path.length() - extension.length()));
//						List<String> commands = Collections.singletonList("script " + id.toString());
//						((CommandFunctionManagerAccessor)funcManager).invokeLoad(CommandFunction.create(newId, funcManager, commands), null, id);
//					}
//				} catch (ScriptException e) {
//					LOGGER.info("Encountered a script exception!");
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	public boolean containsScript(Identifier script) {
		return SCRIPTS.containsKey(script);
	}

	public int getScriptCount() {
		return SCRIPTS.size();
	}

	public interface ScriptApplier {
		CommandFunction load(CommandFunction function, Throwable t, Identifier id);
	}
}
