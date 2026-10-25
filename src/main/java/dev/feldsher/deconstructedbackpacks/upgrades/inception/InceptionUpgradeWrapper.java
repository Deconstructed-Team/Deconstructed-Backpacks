package dev.feldsher.deconstructedbackpacks.upgrades.inception;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.api.IEnergyStorageUpgradeWrapper;
import dev.feldsher.deconstructedbackpacks.api.IFluidHandlerWrapperUpgrade;
import dev.feldsher.deconstructedbackpacks.api.IInventoryWrapperUpgrade;
import dev.feldsher.deconstructedcore.api.IStorageFluidHandler;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.IItemHandlerSimpleInserter;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeAccessModifier;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeWrapperAccessor;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Wraps the inventory the backpack's upgrades work with into one with the backpacks in it, and how its upgrades are
 * found into one that also finds theirs, each as the config allows; so too its tanks and energy.
 */
public class InceptionUpgradeWrapper extends UpgradeWrapperBase<InceptionUpgradeWrapper, InceptionUpgradeItem>
		implements IInventoryWrapperUpgrade, IUpgradeAccessModifier, IFluidHandlerWrapperUpgrade, IEnergyStorageUpgradeWrapper {
	private static final String INVENTORY_ORDER_TAG = "inventoryOrder";
	@Nullable
	private SubBackpacksHandler subBackpacksHandler = null;

	public InceptionUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(backpackWrapper, upgrade, upgradeSaveHandler);
	}

	@Override
	public boolean hideSettingsTab() {
		return !Config.SERVER.inceptionUpgrade.upgradesUseInventoriesOfBackpacksInBackpack;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	public InventoryOrder getInventoryOrder() {
		NBTTagCompound tag = upgrade.getTagCompound();
		return tag == null ? InventoryOrder.MAIN_FIRST : InventoryOrder.fromName(tag.getString(INVENTORY_ORDER_TAG));
	}

	public void setInventoryOrder(InventoryOrder inventoryOrder) {
		StorageNbt.getOrCreateTag(upgrade).setString(INVENTORY_ORDER_TAG, inventoryOrder.getName());
		save();
		storageWrapper.refreshInventoryForUpgradeProcessing();
	}

	private SubBackpacksHandler getSubBackpacksHandler() {
		if (subBackpacksHandler == null) {
			subBackpacksHandler = new SubBackpacksHandler(storageWrapper.getInventoryHandler());
		}
		return subBackpacksHandler;
	}

	@Override
	public IItemHandlerSimpleInserter wrapInventory(IItemHandlerSimpleInserter inventory) {
		if (Config.SERVER.inceptionUpgrade.upgradesUseInventoriesOfBackpacksInBackpack) {
			return new InceptionInventoryHandler(inventory, getInventoryOrder(), getSubBackpacksHandler());
		}
		return inventory;
	}

	@Override
	public IUpgradeWrapperAccessor wrapAccessor(IUpgradeWrapperAccessor upgradeWrapperAccessor) {
		if (Config.SERVER.inceptionUpgrade.upgradesInContainedBackpacksAreFunctional) {
			return new InceptionWrapperAccessor(storageWrapper, getSubBackpacksHandler());
		}
		return upgradeWrapperAccessor;
	}

	@Nullable
	@Override
	public IStorageFluidHandler wrapHandler(@Nullable IStorageFluidHandler fluidHandler) {
		if (Config.SERVER.inceptionUpgrade.upgradesInContainedBackpacksAreFunctional) {
			return new InceptionFluidHandler(fluidHandler, getInventoryOrder(), getSubBackpacksHandler());
		}
		return fluidHandler;
	}

	@Nullable
	@Override
	public IEnergyStorage wrapStorage(@Nullable IEnergyStorage energyStorage) {
		if (Config.SERVER.inceptionUpgrade.upgradesInContainedBackpacksAreFunctional) {
			return new InceptionEnergyStorage(energyStorage, getInventoryOrder(), getSubBackpacksHandler());
		}
		return energyStorage;
	}
}
