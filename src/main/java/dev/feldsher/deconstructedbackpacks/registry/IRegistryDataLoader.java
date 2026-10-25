package dev.feldsher.deconstructedbackpacks.registry;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

/** Reads one kind of registry data file, named as the loader is or by the file's "type". */
public interface IRegistryDataLoader {
	String getName();

	/** @param modId the mod the file is for, when it says one, which is then loaded */
	void parse(JsonObject json, @Nullable String modId);

	void clear();
}
