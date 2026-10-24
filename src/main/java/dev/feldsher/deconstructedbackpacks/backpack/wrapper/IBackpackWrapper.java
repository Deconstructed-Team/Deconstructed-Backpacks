package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IBackpackWrapper extends IStorageWrapper {

	IBackpackWrapper setBackpackStack(ItemStack backpackStack);

	ItemStack getBackpack();

	ItemStack cloneBackpack();

	void copyDataTo(IStorageWrapper otherStorageWrapper);

	void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots);

	/** Grows the slot counts to these, never shrinks them, which would drop what's in the slots past them. */
	void ensureSlotNumbersAtLeast(int numberOfInventorySlots, int numberOfUpgradeSlots);

	void setContentsUuid(UUID storageUuid);

	default void removeContentsUuid() {
		//noop by default
	}

	default void removeContentsUUIDTag() {
		//noop
	}
}
