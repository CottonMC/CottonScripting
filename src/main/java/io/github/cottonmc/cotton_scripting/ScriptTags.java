package io.github.cottonmc.cotton_scripting;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

public class ScriptTags {
	private static TagContainer<Identifier> container = new TagContainer<>(id -> Optional.empty(), "tags/scripts", true, "script");
	private static int latestVersion;

	public static void setContainer(TagContainer<Identifier> id) {
		container = id;
		++latestVersion;
	}

	public static TagContainer<Identifier> getContainer() {
		return container;
	}

	private static Tag<Identifier> register(String id) {
		return new CachingTag(new Identifier(id));
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
