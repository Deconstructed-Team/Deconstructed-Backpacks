package dev.feldsher.deconstructedbackpacks.registry.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;

import java.util.Optional;
import java.util.function.Predicate;

/** Recognizes json objects of its "type". */
abstract class TypedMatcherFactory<T> implements IMatcherFactory<T> {
	private final String typeName;

	protected TypedMatcherFactory(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public boolean appliesTo(JsonElement jsonElement) {
		return jsonElement.isJsonObject() && JsonUtils.getString(jsonElement.getAsJsonObject(), "type", "").equals(typeName);
	}

	@Override
	public Optional<Predicate<T>> getPredicate(JsonElement jsonElement) {
		return getPredicateFromObject(jsonElement.getAsJsonObject());
	}

	protected abstract Optional<Predicate<T>> getPredicateFromObject(JsonObject jsonObject);
}
