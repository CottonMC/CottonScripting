package io.github.cottonmc.cotton_scripting.impl;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface TagContainerAccessor<T> {

    Map<Identifier, Tag<T>> getInternalIdMap();
}
