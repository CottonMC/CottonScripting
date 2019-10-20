package io.github.cottonmc.cotton_scripting.mixin;

import io.github.cottonmc.cotton_scripting.TagContainerAccessor;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(TagContainer.class)
@Implements(@Interface(iface = TagContainerAccessor.class,prefix = "tagContainerAccessor$",remap = Interface.Remap.ALL))
public class TagContainerMixin<T> {

    @Shadow private Map<Identifier, Tag<T>> idMap;

    public Map<Identifier, Tag<T>> tagContainerAccessor$getInternalIdMap(){
        return idMap;
    }
}
