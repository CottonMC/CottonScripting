package io.github.cottonmc.cotton_scripting.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ScriptArgumentType implements ArgumentType<ScriptArgumentType.ScriptArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
	private static final DynamicCommandExceptionType UNKNOWN_TAG_EXCEPTION = new DynamicCommandExceptionType((object_1) -> new TranslatableComponent("arguments.function.tag.unknown", new Object[]{object_1}));
	private static final DynamicCommandExceptionType UNKNOWN_ID_EXCEPTION = new DynamicCommandExceptionType((object_1) -> new TranslatableComponent("arguments.function.unknown", new Object[]{object_1}));

	public ScriptArgumentType() {
	}

	public static ScriptArgumentType create() {
		return new ScriptArgumentType();
	}

	@Override
	public ScriptArgument parse(StringReader reader) throws CommandSyntaxException {
		final Identifier id;
		if (reader.canRead() && reader.peek() == '#') {
			reader.skip();
			id = Identifier.parse(reader);
			return new ScriptArgument() {
				public Collection<Identifier> getScripts(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
					Tag<Identifier> tag = getIdTag(id);
					return tag.values();
				}

				public Either<Identifier, Tag<Identifier>> getScriptOrTag(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
					return Either.right(getIdTag(id));
				}
			};
		} else {
			id = Identifier.parse(reader);
			return new ScriptArgument() {
				public Collection<Identifier> getScripts(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
					return Collections.singleton(getId(id));
				}

				public Either<Identifier, Tag<Identifier>> getScriptOrTag(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
					return Either.left(getId(id));
				}
			};
		}
	}

	private static Identifier getId(Identifier id) throws CommandSyntaxException {
		if (!ScriptLoader.SCRIPTS.containsKey(id)) throw UNKNOWN_ID_EXCEPTION.create(id.toString());
		return id;
	}

	private static Tag<Identifier> getIdTag(Identifier id) throws CommandSyntaxException {
		Tag<Identifier> tag = ScriptTags.getContainer().get(id);
		if (tag == null) {
			throw UNKNOWN_TAG_EXCEPTION.create(id.toString());
		} else {
			return tag;
		}
	}

	public static Collection<Identifier> getScripts(CommandContext<ServerCommandSource> context, String id) throws CommandSyntaxException {
		return context.getArgument(id, ScriptArgumentType.ScriptArgument.class).getScripts(context);
	}

	public static Either<Identifier, Tag<Identifier>> getScriptOrTag(CommandContext<ServerCommandSource> context, String id) throws CommandSyntaxException {
		return context.getArgument(id, ScriptArgumentType.ScriptArgument.class).getScriptOrTag(context);
	}

	public interface ScriptArgument {
		Collection<Identifier> getScripts(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

		Either<Identifier, Tag<Identifier>> getScriptOrTag(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
	}
}
