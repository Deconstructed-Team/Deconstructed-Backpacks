package dev.feldsher.deconstructedbackpacks.upgrades.inception;

import dev.feldsher.deconstructedcore.api.IStorageFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The backpack's tanks and those of the backpacks in it, as one handler, in the inventory order. */
public class InceptionFluidHandler implements IStorageFluidHandler {
	@Nullable
	private final IStorageFluidHandler wrappedFluidHandler;
	private final InventoryOrder inventoryOrder;
	private final SubBackpacksHandler subBackpacksHandler;
	private IStorageFluidHandler[] fluidHandlers;

	public InceptionFluidHandler(@Nullable IStorageFluidHandler wrappedFluidHandler, InventoryOrder inventoryOrder, SubBackpacksHandler subBackpacksHandler) {
		this.wrappedFluidHandler = wrappedFluidHandler;
		this.inventoryOrder = inventoryOrder;
		this.subBackpacksHandler = subBackpacksHandler;
		subBackpacksHandler.addRefreshListener(sbs -> refreshHandlers());
		refreshHandlers();
	}

	private void refreshHandlers() {
		List<IStorageFluidHandler> handlers = new ArrayList<>();
		if (wrappedFluidHandler != null && inventoryOrder == InventoryOrder.MAIN_FIRST) {
			handlers.add(wrappedFluidHandler);
		}
		subBackpacksHandler.getSubBackpacks().forEach(sbp -> sbp.getFluidHandler().ifPresent(handlers::add));
		if (wrappedFluidHandler != null && inventoryOrder == InventoryOrder.INCEPTED_FIRST) {
			handlers.add(wrappedFluidHandler);
		}
		fluidHandlers = handlers.toArray(new IStorageFluidHandler[0]);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		List<IFluidTankProperties> properties = new ArrayList<>();
		for (IStorageFluidHandler fluidHandler : fluidHandlers) {
			Collections.addAll(properties, fluidHandler.getTankProperties());
		}
		return properties.toArray(new IFluidTankProperties[0]);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill, boolean ignoreInOutLimit) {
		if (resource == null || resource.amount <= 0) {
			return 0;
		}
		int filled = 0;
		FluidStack toFill = resource;
		for (IStorageFluidHandler fluidHandler : fluidHandlers) {
			filled += fluidHandler.fill(toFill, doFill, ignoreInOutLimit);
			if (filled == resource.amount) {
				return resource.amount;
			}
			toFill = new FluidStack(resource, resource.amount - filled);
		}

		return filled;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain, boolean ignoreInOutLimit) {
		if (resource == null || resource.amount <= 0) {
			return null;
		}
		int drained = 0;
		FluidStack toDrain = resource;
		for (IStorageFluidHandler fluidHandler : fluidHandlers) {
			FluidStack drainedFromHandler = fluidHandler.drain(toDrain, doDrain, ignoreInOutLimit);
			drained += drainedFromHandler == null ? 0 : drainedFromHandler.amount;
			if (drained == resource.amount) {
				return resource.copy();
			}
			toDrain = new FluidStack(resource, resource.amount - drained);
		}

		return drained == 0 ? null : new FluidStack(resource, drained);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain, boolean ignoreInOutLimit) {
		for (IStorageFluidHandler fluidHandler : fluidHandlers) {
			FluidStack drained = fluidHandler.drain(maxDrain, doDrain, ignoreInOutLimit);
			if (drained != null && drained.amount > 0) {
				return drained;
			}
		}
		return null;
	}
}
