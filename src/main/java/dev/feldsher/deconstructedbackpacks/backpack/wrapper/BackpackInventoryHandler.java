package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BackpackInventoryHandler extends InventoryHandler {
	public BackpackInventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper, NBTTagCompound contentsNbt, Runnable saveHandler, int slotLimit) {
		super(numberOfInventorySlots, storageWrapper, contentsNbt, saveHandler, slotLimit);
	}

	@Override
	protected boolean isAllowed(ItemStack stack) {
		// A backpack goes into another only with the inception upgrade, which comes with the upgrades
		return !Config.SERVER.isItemDisallowed(stack.getItem()) && !(stack.getItem() instanceof BackpackItem);
	}
}
