package io.github.cottonmc.cotton_scripting.mixin;

import io.github.cottonmc.cotton_scripting.CottonScripting;
import io.github.cottonmc.cotton_scripting.impl.ExecutableScript;
import io.github.cottonmc.cotton_scripting.impl.ScriptTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

	@Inject(method = "reloadDataPacks", at = @At("TAIL"))
	private void injectReloadScripts(LevelProperties props, CallbackInfo ci) {
		for (ExecutableScript id : ScriptTags.LOAD.values()) {
			CottonScripting.runScriptFromServer(id, ((MinecraftServer)(Object)this));
		}
	}
}
