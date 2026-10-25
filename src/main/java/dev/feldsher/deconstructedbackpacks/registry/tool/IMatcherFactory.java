package dev.feldsher.deconstructedbackpacks.registry.tool;

import com.google.gson.JsonElement;

import java.util.Optional;
import java.util.function.Predicate;

/** Makes a predicate from the json entries it recognizes. */
public interface IMatcherFactory<T> {
	boolean appliesTo(JsonElement jsonElement);

	Optional<Predicate<T>> getPredicate(JsonElement jsonElement);
}
