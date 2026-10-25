package dev.feldsher.deconstructedbackpacks.registry.tool;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

/** Matches what comes from the mod, by its registry name. */
public class ModMatcher<C> implements Predicate<C> {
	private final String modId;
	private final Function<C, ResourceLocation> getNameFromContext;

	public ModMatcher(String modId, Function<C, ResourceLocation> getNameFromContext) {
		this.modId = modId;
		this.getNameFromContext = getNameFromContext;
		ToolRegistry.addModWithMapping(modId);
	}

	@Override
	public boolean test(C context) {
		@Nullable ResourceLocation name = getNameFromContext.apply(context);
		return name != null && name.getNamespace().equals(modId);
	}
}
