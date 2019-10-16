package io.github.cottonmc.cotton_scripting.mixin;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TagContainer.class)
public interface TagContainerAccessor<T> {

    @Accessor
    Map<Identifier, Tag<T>> getIdMap();
}
