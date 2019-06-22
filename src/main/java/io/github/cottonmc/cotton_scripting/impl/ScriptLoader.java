package io.github.cottonmc.cotton_scripting.impl;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.cottonmc.cotton_scripting.CottonScripting;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ScriptLoader implements SimpleResourceReloadListener {
	public static Map<Identifier, String> SCRIPTS = new HashMap<>();
	public static SuggestionProvider<ServerCommandSource> SCRIPT_SUGGESTIONS = SuggestionProviders.register(new Identifier(CottonScripting.MODID, "scripts"),
			(context, builder) -> CommandSource.suggestIdentifiers(SCRIPTS.keySet(), builder));
	public static SuggestionProvider<ServerCommandSource> SCRIPT_TAG_SUGGESTIONS = SuggestionProviders.register(new Identifier(CottonScripting.MODID, "script_tags"),
			(context, builder) -> CommandSource.suggestIdentifiers(ScriptTags.getContainer().getKeys(), builder));
	private final TagContainer<Identifier> SCRIPT_TAGS = ScriptTags.getContainer();
	Map<Identifier, Tag.Builder<Identifier>> scriptBuilder;
	CompletableFuture<Map<Identifier, Tag.Builder<Identifier>>> tagFuture;

	@Override
	public CompletableFuture load(ResourceManager manager, Profiler profiler, Executor executor) {
		SCRIPT_TAGS.clear();
		return CompletableFuture.supplyAsync(() -> {
			SCRIPTS.clear();
			Collection<Identifier> resources = manager.findResources("scripts", (name) -> true);
			for (Identifier fileId : resources) {
				try {
					Resource res = manager.getResource(fileId);
					String script = IOUtils.toString(res.getInputStream());
					int localPath = fileId.getPath().indexOf('/')+1;
					Identifier scriptId = new Identifier(fileId.getNamespace(), fileId.getPath().substring(localPath));
					SCRIPTS.put(scriptId, script);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			tagFuture = SCRIPT_TAGS.prepareReload(manager, executor);
			return SCRIPTS;
		});
	}

	@Override
	public CompletableFuture<Void> apply(Object data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			try {
				scriptBuilder = tagFuture.get();
				this.SCRIPT_TAGS.applyReload(scriptBuilder);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(CottonScripting.MODID, "script_loader");
	}

	public static Optional<Identifier> getScriptKey(Identifier id) {
		System.out.println(SCRIPTS.keySet());
		if (SCRIPTS.containsKey(id)) return Optional.of(id);
		else return Optional.empty();
	}
}
