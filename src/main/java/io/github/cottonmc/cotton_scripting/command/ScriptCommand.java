package io.github.cottonmc.cotton_scripting.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.api.ServerSource;
import io.github.cottonmc.cotton_scripting.impl.CottonScriptLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import javax.script.ScriptException;

public class ScriptCommand implements Command<ServerCommandSource> {
	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		try {
			Identifier id = context.getArgument("script", Identifier.class);
			if (!CottonScriptLoader.INSTANCE.containsScript(id)) {
				context.getSource().sendError(new TranslatableText("result.cotton-scripting.no_script"));
				return -1;
			}
			boolean errored = CottonScriptLoader.INSTANCE.runScript(id, context);
			if (errored) {
				context.getSource().sendError(new TranslatableText("error.cotton-scripting.script_error", CottonScriptLoader.INSTANCE.getScript(id).getErrorMessage()));
				return -1;
			} else {
				context.getSource().sendFeedback(new TranslatableText("result.cotton-scripting.script_result"), false);
			}
		} catch (ScriptException e) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.script_error", e.getMessage()));
			return -1;
		} catch (Throwable t) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.unknown_error", t.getMessage()));
			t.printStackTrace();
			return -1;
		}
		return 1;
	}
}
