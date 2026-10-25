package dev.feldsher.deconstructedbackpacks.upgrades.smithing;

import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Consumer;

/** The smithing table's item and material slots, saved into the upgrade. */
public class SmithingUpgradeWrapper extends UpgradeWrapperBase<SmithingUpgradeWrapper, SmithingUpgradeItem> {
	private static final String INVENTORY_TAG = "inventory";
	private static final String SHIFT_CLICK_INTO_STORAGE_TAG = "shiftClickIntoStorage";
	private final ItemStackHandler inventory;

	protected SmithingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		inventory = new ItemStackHandler(2) {
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				StorageNbt.getOrCreateTag(upgrade).setTag(INVENTORY_TAG, serializeNBT());
				save();
			}
		};
		NBTTagCompound tag = upgrade.getTagCompound();
		if (tag != null && tag.hasKey(INVENTORY_TAG)) {
			inventory.deserializeNBT(tag.getCompoundTag(INVENTORY_TAG));
		}
	}

	public ItemStackHandler getInventory() {
		return inventory;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	public boolean shouldShiftClickIntoStorage() {
		NBTTagCompound tag = upgrade.getTagCompound();
		return tag == null || !tag.hasKey(SHIFT_CLICK_INTO_STORAGE_TAG) || tag.getBoolean(SHIFT_CLICK_INTO_STORAGE_TAG);
	}

	public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
		StorageNbt.getOrCreateTag(upgrade).setBoolean(SHIFT_CLICK_INTO_STORAGE_TAG, shiftClickIntoStorage);
		save();
	}
}
