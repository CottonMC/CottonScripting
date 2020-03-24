package io.github.cottonmc.cotton_scripting.command;

import javax.script.ScriptException;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.cotton_scripting.impl.ScriptLoader;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ScriptCommand implements Command<ServerCommandSource> {
	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		try {
			Identifier id = context.getArgument("script", Identifier.class);
			Object result = ScriptLoader.INSTANCE.runScript(id, context);
			if (result != null) {
				context.getSource().sendFeedback(new TranslatableText("result.cotton-scripting.script_result", result), false);
			}
		} catch (ScriptException e) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.script_error", e.getMessage()));
			return -1;
		} catch (Throwable t) {
			context.getSource().sendError(new TranslatableText("error.cotton-scripting.unknown_error", t.getMessage()));
			return -1;
		}
		return 1;
	}
}
