package dev.feldsher.deconstructedbackpacks.api;

import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/** An upgrade that changes the backpack's energy as others see it, by wrapping its energy storage. */
public interface IEnergyStorageUpgradeWrapper {
	@Nullable
	IEnergyStorage wrapStorage(@Nullable IEnergyStorage energyStorage);
}
