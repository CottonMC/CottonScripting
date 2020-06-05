package io.github.cottonmc.cotton_scripting;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.cottonmc.cotton_scripting.command.ScriptCommand;
import io.github.cottonmc.cotton_scripting.impl.CottonScriptLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.TranslatableText;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class CottonScripting implements ModInitializer {

	public static final String MODID = "cotton-scripting";
	public static final ScriptEngineManager SCRIPT_MANAGER = new ScriptEngineManager();

	@Override
	public void onInitialize() {
//		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ScriptLoader());
		
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register((
				CommandManager.literal("script").requires((source) -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("script", IdentifierArgumentType.identifier())
								.suggests(CottonScriptLoader.INSTANCE.SCRIPT_SUGGESTIONS)
								.executes(new ScriptCommand())
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
	}


}
