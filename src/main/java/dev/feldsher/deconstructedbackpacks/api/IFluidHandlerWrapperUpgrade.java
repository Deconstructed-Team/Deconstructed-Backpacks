package dev.feldsher.deconstructedbackpacks.api;

import dev.feldsher.deconstructedcore.api.IStorageFluidHandler;

import javax.annotation.Nullable;

/** An upgrade that changes the backpack's tanks as others see them, by wrapping its fluid handler. */
public interface IFluidHandlerWrapperUpgrade {
	@Nullable
	IStorageFluidHandler wrapHandler(@Nullable IStorageFluidHandler fluidHandler);
}
