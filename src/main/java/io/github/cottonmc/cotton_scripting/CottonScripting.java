package io.github.cottonmc.cotton_scripting;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.cottonmc.cotton_scripting.api.CottonScriptContext;
import io.github.cottonmc.cotton_scripting.impl.ScriptLoader;
import io.github.cottonmc.cotton_scripting.impl.ScriptTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.text.TranslatableText;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import javax.script.*;
import java.util.Collection;

public class CottonScripting implements ModInitializer {

	public static final String MODID = "cotton-scripting";
	public static final ScriptEngineManager SCRIPT_MANAGER = new ScriptEngineManager();

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ScriptLoader());

		CommandRegistry.INSTANCE.register(false, dispatcher -> dispatcher.register((
				CommandManager.literal("script").requires((source) -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("run")
								.then(CommandManager.argument("script", IdentifierArgumentType.identifier())
										.suggests(ScriptLoader.SCRIPT_SUGGESTIONS)
										.executes(context -> runScript(context, null))
										.then(CommandManager.argument("function", StringArgumentType.word())
												.executes(context -> runScript(context, context.getArgument("function", String.class)))
												.then(CommandManager.argument("arguments", StringArgumentType.greedyString())
														.executes(context -> {
															String arguments = context.getArgument("arguments", String.class);
															String[] args = arguments.split(",");
															for (int i = 0; i < args.length; i++) {
																String arg = args[i];
																if (arg.charAt(0) == ' ') args[i] = arg.substring(1);
															}
															return runScript(context, context.getArgument("function", String.class), args);
														})
												)
										)
								)
						).then(CommandManager.literal("tag")
								.then(CommandManager.argument("tag", IdentifierArgumentType.identifier())
										.suggests(ScriptLoader.SCRIPT_TAG_SUGGESTIONS)
										.executes(CottonScripting::runScriptTag)
								)
						).then(CommandManager.literal("engines")
								.then(CommandManager.literal("list")
										.executes(context -> {
											for (ScriptEngineFactory factory : SCRIPT_MANAGER.getEngineFactories()) {
												context.getSource().sendFeedback(new TranslatableText("engines.cotton-scripting.engine", factory.getEngineName(), factory.getLanguageName()), false);
												context.getSource().sendFeedback(new TranslatableText("engines.cotton-scripting.languages", factory.getExtensions().toString()), false);
											}
											return 1;
										})
								).then(CommandManager.literal("for")
										.then(CommandManager.argument("extension", StringArgumentType.word())
												.executes(context -> {
													ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(context.getArgument("extension", String.class));
													if (engine == null) {
														context.getSource().sendError(new TranslatableText("engines.cotton-scripting.no_engines", context.getArgument("extension", String.class)));
														return -1;
													}
													ScriptEngineFactory factory = engine.getFactory();
													context.getSource().sendFeedback(new TranslatableText("engines.cotton-scripting.engine", factory.getEngineName(), factory.getLanguageName()), false);
													return 1;
												})
										)
								)
						)
				)
		));

		ServerTickCallback.EVENT.register((server) -> {
			for (Identifier id : ScriptTags.TICK.values()) {
				runScriptFromServer(id, server);
			}
		});
	}

	private static int runScript(CommandContext<ServerCommandSource> context, @Nullable String funcName, String... args) {
		Identifier scriptName = context.getArgument("script", Identifier.class);
		String extension = scriptName.getPath().substring(scriptName.getPath().lastIndexOf('.')+1);
		String script = ScriptLoader.SCRIPTS.get(scriptName);
		if (script == null) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.no_script"));
			return -1;
		}
		ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(extension);
		if (engine == null) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.no_engine"));
			return -1;
		}
		ScriptContext enginectx = engine.getContext();
		CottonScriptContext scriptctx = new CottonScriptContext(context, scriptName, args);
		enginectx.setAttribute("cotton_context", scriptctx, 100);
		Object result;
		try {
			result = engine.eval(script, enginectx);
			if (funcName != null) {
				Invocable invocable = (Invocable) engine;
				result = invocable.invokeFunction(funcName, scriptctx);
			}
		} catch (ScriptException e) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.script_error", e.getMessage()));
			return -1;
		} catch (NoSuchMethodException e) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.no_function", funcName, scriptName));
			return -1;
		} catch (Throwable t) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.unknown_error", t.getMessage()));
			return -1;
		}
		if (result != null) {
			context.getSource().sendFeedback(new TranslatableText("result.cotton-scripting.script_result", result), false);
		}
		return 1;
	}

	private static int runScriptTag(CommandContext<ServerCommandSource> context) {
		Collection<Identifier> scripts = ScriptTags.getContainer().getOrCreate(context.getArgument("tag", Identifier.class)).values();
		int successful = 0;
		for (Identifier scriptName : scripts) {
			String extension = scriptName.getPath().substring(scriptName.getPath().lastIndexOf('.') + 1);
			String script = ScriptLoader.SCRIPTS.get(scriptName);
			if (script == null) {
				context.getSource().sendError(new TranslatableText("error.cotton-scripting.no_script"));
				continue;
			}
			ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(extension);
			if (engine == null) {
				context.getSource().sendError(new TranslatableText("error.cotton-scripting.no_engine"));
				continue;
			}
			Object result;
			try {
				ScriptContext enginectx = engine.getContext();
				CottonScriptContext scriptctx = new CottonScriptContext(context, scriptName);
				enginectx.setAttribute("cotton_context", scriptctx, 100);
				result = engine.eval(script);
			} catch (ScriptException e) {
				context.getSource().sendError(new TranslatableText("error.cotton-scripting.script_error", e.getMessage()));
				continue;
			} catch (Throwable t) {
				context.getSource().sendError(new TranslatableText("error.cotton-scripting.unknown_error", t.getMessage()));
				continue;
			}
			if (result != null) {
				if (scripts.size() == 1) context.getSource().sendFeedback(new TranslatableText("result.cotton-scripting.script_result", result), false);
			}
			successful++;
		}
		if (scripts.size() != 1) context.getSource().sendFeedback(new TranslatableText("result.cotton-scripting.tag_result", successful), false);
		return successful;
	}

	public static void runScriptFromServer(Identifier id, MinecraftServer server) {
		ServerCommandSource source = server.getCommandSource();
		String extension = id.getPath().substring(id.getPath().lastIndexOf('.') + 1);
		String script = ScriptLoader.SCRIPTS.get(id);
		if (script == null) {
			source.sendError(new TranslatableText("error.cotton-scripting.no_script"));
			return;
		}
		ScriptEngine engine = SCRIPT_MANAGER.getEngineByExtension(extension);
		if (engine == null) {
			source.sendError(new TranslatableText("error.cotton-scripting.no_engine"));
			return;
		}
		Object result;
		try {
			ScriptContext enginectx = engine.getContext();
			CottonScriptContext scriptctx = new CottonScriptContext(source, id);
			enginectx.setAttribute("cotton_context", scriptctx, 100);
			result = engine.eval(script);
		} catch (ScriptException e) {
			source.sendError(new TranslatableText("error.cotton-scripting.script_error", e.getMessage()));
			return;
		} catch (Throwable t) {
			source.sendError(new TranslatableText("error.cotton-scripting.unknown_error", t.getMessage()));
			return;
		}
		if (result != null) {
			source.sendFeedback(new TranslatableText("result.cotton-scripting.script_result", result), false);
		}
	}
}
