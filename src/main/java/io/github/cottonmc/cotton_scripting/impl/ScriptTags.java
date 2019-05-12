package io.github.cottonmc.cotton_scripting.impl;

import io.github.cottonmc.cotton_scripting.CottonScripting;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

public class ScriptTags {
	private static TagContainer<Identifier> container = new TagContainer<>(id -> Optional.empty(), "tags/scripts", true, "script");
	private static int latestVersion;

	public static final Tag<Identifier> LOAD = register(new Identifier(CottonScripting.MODID, "load"));
	public static final Tag<Identifier> TICK = register(new Identifier(CottonScripting.MODID, "tick"));
	public static final Tag<Identifier> WORLD_STORAGE_LISTENERS = register(new Identifier(CottonScripting.MODID, "world_storage_listeners"));

	public static void setContainer(TagContainer<Identifier> id) {
		container = id;
		++latestVersion;
	}

	public static TagContainer<Identifier> getContainer() {
		return container;
	}

	public static Tag<Identifier> register(Identifier id) {
		return new CachingTag(id);
	}

	static class CachingTag extends Tag<Identifier> {
		private int version = -1;
		private Tag<Identifier> delegate;

		public CachingTag(Identifier id) {
			super(id);
		}

		public boolean containsTag(Identifier id) {
			if (this.version != latestVersion) {
				this.delegate = container.getOrCreate(this.getId());
				this.version = latestVersion;
			}

			return this.delegate.contains(id);
		}

		public Collection<Identifier> values() {
			if (this.version != latestVersion) {
				this.delegate = container.getOrCreate(this.getId());
				this.version = latestVersion;
			}

			return this.delegate.values();
		}

		public Collection<Entry<Identifier>> entries() {
			if (this.version != latestVersion) {
				this.delegate = container.getOrCreate(this.getId());
				this.version = latestVersion;
			}

			return this.delegate.entries();
		}
	}

}
