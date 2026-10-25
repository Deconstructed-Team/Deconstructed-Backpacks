package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedcore.api.IStorageFluidHandler;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.upgrades.tank.TankUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.tank.TankUpgradeWrapper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.List;

/** The backpack's tank upgrades, filled and drained one after another. */
public class BackpackFluidHandler implements IStorageFluidHandler {
	private final IStorageWrapper backpackWrapper;

	public BackpackFluidHandler(IStorageWrapper backpackWrapper) {
		this.backpackWrapper = backpackWrapper;
	}

	private List<TankUpgradeWrapper> getAllTanks() {
		return backpackWrapper.getUpgradeHandler().getTypeWrappers(TankUpgradeItem.TYPE);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		List<TankUpgradeWrapper> tanks = getAllTanks();
		IFluidTankProperties[] properties = new IFluidTankProperties[tanks.size()];
		for (int tank = 0; tank < tanks.size(); tank++) {
			FluidStack contents = tanks.get(tank).getContents();
			properties[tank] = new FluidTankProperties(contents == null ? null : contents.copy(), tanks.get(tank).getTankCapacity());
		}
		return properties;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill, boolean ignoreInOutLimit) {
		if (resource == null || resource.amount <= 0) {
			return 0;
		}
		int filled = 0;
		FluidStack toFill = resource;
		for (TankUpgradeWrapper tank : getAllTanks()) {
			filled += tank.fill(toFill, doFill, ignoreInOutLimit);
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
		int toDrain = resource.amount;
		for (TankUpgradeWrapper tank : getAllTanks()) {
			if (resource.isFluidEqual(tank.getContents())) {
				FluidStack drainedFromTank = tank.drain(toDrain, doDrain, ignoreInOutLimit);
				drained += drainedFromTank == null ? 0 : drainedFromTank.amount;
				if (drained == resource.amount) {
					return resource.copy();
				}
				toDrain = resource.amount - drained;
			}
		}

		return drained == 0 ? null : new FluidStack(resource, drained);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain, boolean ignoreInOutLimit) {
		for (TankUpgradeWrapper tank : getAllTanks()) {
			FluidStack drained = tank.drain(maxDrain, doDrain, ignoreInOutLimit);
			if (drained != null) {
				return drained;
			}
		}
		return null;
	}
}
