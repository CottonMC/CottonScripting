package io.github.cottonmc.cotton_scripting.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.cottonmc.cotton_scripting.CottonScripting;
import io.github.cottonmc.cotton_scripting.api.CottonScriptContext;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;

public class ScriptLoader {
	public static final ScriptLoader INSTANCE = new ScriptLoader();
	public static final int PATH_PREFIX_LENGTH = "scripts/".length();
	public static final Logger LOGGER = LogManager.getLogger();

	private Map<Identifier, CottonScriptContext> SCRIPTS = new HashMap<>();
	public SuggestionProvider<ServerCommandSource> SCRIPT_SUGGESTIONS = SuggestionProviders.register(new Identifier(CottonScripting.MODID, "scripts"),
			(context, builder) -> CommandSource.suggestIdentifiers(SCRIPTS.keySet(), builder));

	public CottonScriptContext getScript(Identifier id) {
		return SCRIPTS.get(id);
	}

	public Object runScript(Identifier id, CommandContext<ServerCommandSource> context) throws ScriptException {
		CottonScriptContext scriptctx = getScript(id);
		CompiledScript script = scriptctx.getScript();
		ScriptContext enginectx = script.getEngine().getContext();
		enginectx.setAttribute("cotton_context", scriptctx.withContext(context), 100);
		return script.eval(enginectx);
	}

	public Object runScript(Identifier id, ServerCommandSource source) throws ScriptException {
		CottonScriptContext scriptctx = getScript(id);
		CompiledScript script = scriptctx.getScript();
		ScriptContext enginectx = script.getEngine().getContext();
		enginectx.setAttribute("cotton_context", scriptctx.withSource(source), 100);
		return script.eval(enginectx);
	}

	public List<CompletableFuture<CommandFunction>> load(ResourceManager manager, CommandFunctionManager funcManager, ScriptApplier handler) {
		SCRIPTS.clear();
		List<CompletableFuture<CommandFunction>> futures = new ArrayList<>();
		Collection<Identifier> resources = manager.findResources("scripts", (name) -> true);
		for (Identifier id : resources) {
			String path = id.getPath();
			String extension = id.getPath().substring(id.getPath().lastIndexOf('.') + 1);
			Identifier scriptId = new Identifier(id.getNamespace(), path.substring(PATH_PREFIX_LENGTH));
			try {
				Resource res = manager.getResource(id);
				futures.add(CompletableFuture.supplyAsync(() -> parseScript(res), ResourceImpl.RESOURCE_IO_EXECUTOR).thenApplyAsync(script -> {
					if (script.equals("")) return null;
					ScriptEngine engine = CottonScripting.SCRIPT_MANAGER.getEngineByExtension(extension);
					if (engine == null) {
						System.out.println("Script engine doesn't exist");
						LOGGER.error("Script engine for extension {} doesn't exist", extension);
						return null;
					}
					if (engine instanceof Compilable && engine instanceof Invocable) {
						try {
							System.out.println("Compiling " + scriptId);
							CompiledScript compiled = ((Compilable)engine).compile(script);
							SCRIPTS.put(scriptId, new CottonScriptContext(compiled, scriptId));
							System.out.println(SCRIPTS.keySet().toString());
							List<String> commands = Collections.singletonList("script " + scriptId.toString());
							return CommandFunction.create(id, funcManager, commands);
						} catch (ScriptException e) {
							System.out.println("Script encountered error while compiling");
							LOGGER.error("Script {} encountered an error while compiling: {}", scriptId, e.getMessage());
							return null;
						}
					} else {
						System.out.println("Engine is invalid");
						LOGGER.error("Script engine for extension {} is not both compilable and invocable", extension);
						return null;
					}
				}, funcManager.getServer().getWorkerExecutor()).handle((function, throwable) -> {
					if (function != null) return handler.load(function, throwable, id);
					else {
						LOGGER.error("Script {} turned up null! That shouldn't happen", id);
						return null;
					}
				}));
			} catch (IOException e) {
				LOGGER.error("Could not load resource {}: {}", id, e.getMessage());
			}
		}
		return futures;
	}

	private String parseScript(Resource res) {
		try {
			return IOUtils.toString(res.getInputStream(), Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("IO exception");
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
//					System.out.println("Script engine is not compilable or is not invocable!");
//					continue;
//				}
//				try {
//					CompiledScript script = ((Compilable)engine).compile(foundScripts.get(id));
//					SCRIPTS.put(id, new CottonScriptContext(script, id));
//					if (funcManager != null) {
//						String path = id.getPath();
//						System.out.println("path");
//						Identifier newId = new Identifier(id.getNamespace(), "scripts/" + path.substring(PATH_PREFIX_LENGTH, path.length() - extension.length()));
//						List<String> commands = Collections.singletonList("script " + id.toString());
//						((CommandFunctionManagerAccessor)funcManager).invokeLoad(CommandFunction.create(newId, funcManager, commands), null, id);
//					}
//				} catch (ScriptException e) {
//					System.out.println("Encountered a script exception!");
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
