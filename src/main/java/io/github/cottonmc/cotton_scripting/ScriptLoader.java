package io.github.cottonmc.cotton_scripting;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ScriptLoader implements SimpleResourceReloadListener {
	public static Map<Identifier, String> SCRIPTS = new HashMap<>();
	public static SuggestionProvider<ServerCommandSource> SCRIPT_SUGGESTIONS = SuggestionProviders.register(new Identifier(CottonScripting.MODID, "suggestions"),
			(context, builder) -> CommandSource.suggestIdentifiers(SCRIPTS.keySet(), builder));

	@Override
	public CompletableFuture load(ResourceManager manager, Profiler profiler, Executor executor) {
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
			return SCRIPTS;
		});
	}

	@Override
	public CompletableFuture<Void> apply(Object data, ResourceManager manager, Profiler profiler, Executor executor) {
		return null;
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(CottonScripting.MODID, "script_loader");
	}
}
