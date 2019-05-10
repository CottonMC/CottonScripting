package io.github.cottonmc.cotton_scripting;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;

public class CottonScripting implements ModInitializer {

	public static final String MODID = "cotton-scripting";
	public static final ScriptEngineManager SCRIPT_MANAGER = new ScriptEngineManager();

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ScriptLoader());

		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register((
				CommandManager.literal("script").requires((source) -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("script", ScriptArgumentType.create())
								.suggests(ScriptLoader.SCRIPT_SUGGESTIONS)
								.executes(context -> {
									Collection<Identifier> scripts = ScriptArgumentType.getScripts(context, "script");
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
							return callFunction(context, arguments);
						}))))
		)));
	}

	private static int callFunction(CommandContext<ServerCommandSource> context, String... args) {
		Collection<Identifier> scripts;
		try {
			scripts = ScriptArgumentType.getScripts(context, "script");
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
		}
		if (result != null) {
			context.getSource().sendFeedback(new TranslatableComponent("result.cotton-scripting.script_result", result), false);
		}
		return 1;
	}
}
