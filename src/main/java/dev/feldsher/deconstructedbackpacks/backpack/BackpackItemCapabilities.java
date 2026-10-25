package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedcore.api.IStorageFluidHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A backpack item's inventory, tanks and energy, to other mods. Its tanks only as the config allows: filled and
 * drained in item form, by other mods' default handling, backpacks could be duplicated.
 */
public class BackpackItemCapabilities implements ICapabilityProvider {
	private final ItemStack backpack;

	public BackpackItemCapabilities(ItemStack backpack) {
		this.backpack = backpack;
	}

	private IBackpackWrapper getWrapper() {
		return BackpackWrapper.fromStack(backpack);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return getCapability(capability, facing) != null;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			IBackpackWrapper wrapper = getWrapper();
			return wrapper.getContentsUuid().isPresent() ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(wrapper.getInventoryForInputOutput()) : null;
		}
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
			if (!Config.SERVER.itemFluidHandlerEnabled) {
				return null;
			}
			IStorageFluidHandler fluidHandler = getWrapper().getFluidHandler().orElse(null);
			return fluidHandler == null ? null : CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(new FluidHandlerItemWrapper(backpack, fluidHandler));
		}
		if (capability == CapabilityEnergy.ENERGY) {
			IEnergyStorage energyStorage = getWrapper().getEnergyStorage().orElse(null);
			return energyStorage == null ? null : CapabilityEnergy.ENERGY.cast(energyStorage);
		}
		return null;
	}

	private static class FluidHandlerItemWrapper implements IFluidHandlerItem {
		private final ItemStack container;
		private final IStorageFluidHandler delegate;

		private FluidHandlerItemWrapper(ItemStack container, IStorageFluidHandler delegate) {
			this.container = container;
			this.delegate = delegate;
		}

		@Nonnull
		@Override
		public ItemStack getContainer() {
			return container;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return delegate.getTankProperties();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			return delegate.fill(resource, doFill);
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return delegate.drain(resource, doDrain);
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return delegate.drain(maxDrain, doDrain);
		}
	}
}
