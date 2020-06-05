package io.github.cottonmc.cotton_scripting.mixin;

import io.github.cottonmc.cotton_scripting.impl.CottonScriptLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandFunctionManager.class)
public abstract class MixinCommandFunctionManager {

	@Shadow @Nullable protected abstract CommandFunction load(CommandFunction function, @Nullable Throwable exception, Identifier id);

	@Shadow @Final private static Logger LOGGER;

	@Shadow @Final private Map<Identifier, CommandFunction> idMap;

//	/**
//	 * @reason Have to inject scripts to load before we script tags get loaded
//	 */
//	@Inject(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;allOf([Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
//	private void loadCottonScripts(ResourceManager manager, CallbackInfo info, Collection<Identifier> ids, List<CompletableFuture<CommandFunction>> futures) {
//
//		LOGGER.info("Adding {} futures", scriptFutures.size());
//		futures.addAll(scriptFutures);
//	}

	/**
	 * @author B0undarybreaker
	 * @reason the commented code above doesn't work
	 */
	@Redirect(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;allOf([Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Void> rediretFutureApply(CompletableFuture<CommandFunction>[] futures, ResourceManager manager) {
		List<CompletableFuture<CommandFunction>> scriptFutures = CottonScriptLoader.INSTANCE.load(manager, (CommandFunctionManager)(Object)this, this::load);
		scriptFutures.addAll(Arrays.asList(futures));
		return CompletableFuture.allOf(scriptFutures.toArray(new CompletableFuture[0]));
	}

	/**
	 * @author B0undarybreaker
	 * @reason Properly state the number of functions registered, since scripts are appended
	 */
	@Redirect(method = "apply", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private void redirectLoadMessage(Logger logger, String message, Object arg) {
		int totalCount = (Integer)arg;
		int scriptCount = CottonScriptLoader.INSTANCE.getScriptCount();
		int funcCount = totalCount - scriptCount;
		if (funcCount > 0) logger.info("Loaded {} custom command functions", funcCount);
		if (scriptCount > 0) logger.info("Loaded {} scripts", scriptCount);
	}
}
