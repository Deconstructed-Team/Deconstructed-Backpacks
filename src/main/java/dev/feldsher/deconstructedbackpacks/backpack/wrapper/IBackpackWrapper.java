package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.UUID;

public interface IBackpackWrapper extends IStorageWrapper {

	IBackpackWrapper setBackpackStack(ItemStack backpackStack);

	ItemStack getBackpack();

	ItemStack cloneBackpack();

	void copyDataTo(IStorageWrapper otherStorageWrapper);

	void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots);

	/** Grows the slot counts to these, and the handlers with them, never shrinks them, which would drop what's in the slots past them. */
	void ensureSlotNumbersAtLeast(int numberOfInventorySlots, int numberOfUpgradeSlots);

	/** The shared handlers grown to the stack's slot counts: for the stack that's this backpack now, not a preview. */
	void growHandlersToSlotNumbers();

	void setContentsUuid(UUID storageUuid);

	/** Loot to put in as it's first opened: the share of what the table gives, 0 to 1. */
	void setLoot(ResourceLocation lootTableName, float lootFactor);

	/** Items to put in its inventory as it's first opened, or to drop if they don't fit. */
	void setExtraItems(List<ItemStack> extraItems);

	default void removeContentsUuid() {
		//noop by default
	}

	default void removeContentsUUIDTag() {
		//noop
	}
}
