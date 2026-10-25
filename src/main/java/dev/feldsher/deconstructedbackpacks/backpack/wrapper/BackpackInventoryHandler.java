package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedbackpacks.upgrades.inception.InceptionUpgradeItem;
import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BackpackInventoryHandler extends InventoryHandler {
	public BackpackInventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper, NBTTagCompound contentsNbt, Runnable saveHandler, int slotLimit) {
		super(numberOfInventorySlots, storageWrapper, contentsNbt, saveHandler, slotLimit, Config.SERVER.stackUpgrade.config);
	}

	/** A backpack goes into another only with the inception upgrade, and only one without it itself. */
	@Override
	protected boolean isAllowed(ItemStack stack) {
		return !Config.SERVER.isItemDisallowed(stack.getItem())
				&& (!(stack.getItem() instanceof BackpackItem) || hasInceptionUpgrade() && isBackpackWithoutInceptionUpgrade(stack));
	}

	private boolean hasInceptionUpgrade() {
		return storageWrapper.getUpgradeHandler().hasUpgrade(InceptionUpgradeItem.TYPE);
	}

	private static boolean isBackpackWithoutInceptionUpgrade(ItemStack stack) {
		return !BackpackWrapper.fromStack(stack).getUpgradeHandler().hasUpgrade(InceptionUpgradeItem.TYPE);
	}
}
