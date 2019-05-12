package io.github.cottonmc.cotton_scripting;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.api.ScriptContext;
import io.github.cottonmc.cotton_scripting.api.ScriptTools;
import io.github.cottonmc.cotton_scripting.impl.ScriptArgumentType;
import io.github.cottonmc.cotton_scripting.impl.ScriptLoader;
import io.github.cottonmc.cotton_scripting.impl.ScriptTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import javax.script.*;
import java.util.Collection;

public class CottonScripting implements ModInitializer {

	public static final String MODID = "cotton-scripting";
	public static final ScriptEngineManager SCRIPT_MANAGER = new ScriptEngineManager();

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ScriptLoader());

		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register(
				CommandManager.literal("script-engines")
						.then(CommandManager.literal("list")
								.executes(context -> {
									for (ScriptEngineFactory factory : SCRIPT_MANAGER.getEngineFactories()) {
										context.getSource().sendFeedback(new TranslatableComponent("engines.cotton-scripting.engine", factory.getEngineName(), factory.getLanguageName()), false);
										context.getSource().sendFeedback(new TranslatableComponent("engines.cotton-scripting.languages", factory.getExtensions().toString()), false);
									}
									return 1;
								})
						)
						.then(CommandManager.literal("for")
								.then(CommandManager.argument("extension", StringArgumentType.word())
										.executes(context -> {
											ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(context.getArgument("extension", String.class));
											if (engine == null) {
												context.getSource().sendError(new TranslatableComponent("engines.cotton-scripting.no_engines", context.getArgument("extension", String.class)));
												return -1;
											}
											ScriptEngineFactory factory = engine.getFactory();
											context.getSource().sendFeedback(new TranslatableComponent("engines.cotton-scripting.engine", factory.getEngineName(), factory.getLanguageName()), false);
											return 1;
										})
								)
						)
		));

		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register((
				CommandManager.literal("script").requires((source) -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("script", StringArgumentType.string())
								.suggests(ScriptLoader.SCRIPT_SUGGESTIONS)
								.executes(context -> {
									Collection<Identifier> scripts = new ScriptArgumentType().parse(new StringReader(context.getArgument("script", String.class))).getScripts(context);
									int successful = 0;
									for (Identifier scriptName : scripts) {
										String extension = scriptName.getPath().substring(scriptName.getPath().lastIndexOf('.') + 1);
										String script = ScriptLoader.SCRIPTS.get(scriptName);
										if (script == null) {
											context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.no_script"));
											continue;
										}
										ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(extension);
										if (engine == null) {
											context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.no_engine"));
											continue;
										}
										Object result;
										try {
											result = engine.eval(script);
										} catch (ScriptException e) {
											context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.script_error", e.getMessage()));
											continue;
										} catch (Throwable t) {
											context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.unknown_error", t.getMessage()));
											return -1;
										}
										if (result != null) {
											if (scripts.size() == 1) context.getSource().sendFeedback(new TranslatableComponent("result.cotton-scripting.script_result", result), false);
										}
										successful++;
									}
									if (scripts.size() != 1) context.getSource().sendFeedback(new TranslatableComponent("result.cotton-scripting.tag_result", successful), false);
									return successful;
								})
						.then(CommandManager.argument("function", StringArgumentType.word())
								.executes(context -> callFunction(context))
						.then(CommandManager.argument("arguments", StringArgumentType.greedyString()).executes(context -> {
							String arguments = context.getArgument("arguments", String.class);
							String[] args = arguments.split(",");
							for (int i = 0; i < args.length; i++) {
								String arg = args[i];
								if (arg.charAt(0) == ' ') args[i] = arg.substring(1);
							}
							return callFunction(context, args);
						}))))
		)));
	}

	private static int callFunction(CommandContext<ServerCommandSource> context, String... args) {
		Collection<Identifier> scripts;
		try {
			scripts = new ScriptArgumentType().parse(new StringReader(context.getArgument("script", String.class))).getScripts(context);
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.syntax_exception", e.getMessage()));
			return -1;
		}
		if (scripts.size() != 1) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.only_one_script"));
			return -1;
		}
		Identifier scriptName = null;
		for (Identifier id : scripts) {
			scriptName = id;
		}
		String funcName = context.getArgument("function", String.class);
		String extension = scriptName.getPath().substring(scriptName.getPath().lastIndexOf('.')+1);
		String script = ScriptLoader.SCRIPTS.get(scriptName);
		if (script == null) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.no_script"));
			return -1;
		}
		ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(extension);
		if (engine == null) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.no_engine"));
			return -1;
		}
		Object result;
		try {
			engine.eval(script);
			Invocable invocable = (Invocable) engine;
			ScriptContext scriptctx = new ScriptContext(context, scriptName, args);
			result = invocable.invokeFunction(funcName, scriptctx);
		} catch (ScriptException e) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.script_error", e.getMessage()));
			return -1;
		} catch (NoSuchMethodException e) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.no_function", funcName, scriptName));
			return -1;
		} catch (Throwable t) {
			context.getSource().sendError(new TranslatableComponent("error.cotton-scripting.unknown_error", t.getMessage()));
			return -1;
		}
		if (result != null) {
			context.getSource().sendFeedback(new TranslatableComponent("result.cotton-scripting.script_result", result), false);
		}
		return 1;
	}

	public static void runScriptFromServer(Identifier id, ServerWorld world) {
		ServerCommandSource source = ScriptTools.getServerExecutor(world);
		String extension = id.getPath().substring(id.getPath().lastIndexOf('.') + 1);
		String script = ScriptLoader.SCRIPTS.get(id);
		if (script == null) {
			source.sendError(new TranslatableComponent("error.cotton-scripting.no_script"));
			return;
		}
		ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(extension);
		if (engine == null) {
			source.sendError(new TranslatableComponent("error.cotton-scripting.no_engine"));
			return;
		}
		Object result;
		try {
			result = engine.eval(script);
		} catch (ScriptException e) {
			source.sendError(new TranslatableComponent("error.cotton-scripting.script_error", e.getMessage()));
			return;
		} catch (Throwable t) {
			source.sendError(new TranslatableComponent("error.cotton-scripting.unknown_error", t.getMessage()));
			return;
		}
		if (result != null) {
			source.sendFeedback(new TranslatableComponent("result.cotton-scripting.script_result", result), false);
		}
	}
}
