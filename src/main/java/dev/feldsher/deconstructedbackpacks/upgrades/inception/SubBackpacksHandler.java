package dev.feldsher.deconstructedbackpacks.upgrades.inception;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The backpacks in a backpack's inventory, by slot, kept up to date as the inventory changes. Those who use them are
 * told before and after they change.
 */
public class SubBackpacksHandler {
	private final Map<Integer, IStorageWrapper> subBackpacks = new LinkedHashMap<>();
	private final InventoryHandler inventoryHandler;
	private final List<Consumer<Collection<IStorageWrapper>>> refreshListeners = new ArrayList<>();
	private final List<Consumer<Collection<IStorageWrapper>>> beforeRefreshListeners = new ArrayList<>();

	public SubBackpacksHandler(InventoryHandler inventoryHandler) {
		this.inventoryHandler = inventoryHandler;
		// held weakly by the inventory, which outlives the upgrade wrappers that make this
		inventoryHandler.addListener(this, SubBackpacksHandler::onContentsChanged);
		refreshSubBackpacks();
	}

	public void addRefreshListener(Consumer<Collection<IStorageWrapper>> listener) {
		refreshListeners.add(listener);
	}

	public void addBeforeRefreshListener(Consumer<Collection<IStorageWrapper>> listener) {
		beforeRefreshListeners.add(listener);
	}

	public Collection<IStorageWrapper> getSubBackpacks() {
		return subBackpacks.values();
	}

	private void onContentsChanged(int slot) {
		ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
		boolean backpackIsInTheSlot = stackInSlot.getItem() instanceof BackpackItem;
		boolean backpackWasInTheSlot = subBackpacks.containsKey(slot);
		if (!backpackWasInTheSlot && !backpackIsInTheSlot) {
			return;
		}
		if (backpackWasInTheSlot != backpackIsInTheSlot || BackpackWrapper.fromStack(stackInSlot) != subBackpacks.get(slot)) {
			beforeRefreshListeners.forEach(l -> l.accept(subBackpacks.values()));
			refreshSubBackpacks();
			refreshListeners.forEach(l -> l.accept(subBackpacks.values()));
		}
	}

	private void refreshSubBackpacks() {
		subBackpacks.clear();
		for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
			ItemStack slotStack = inventoryHandler.getStackInSlot(slot);
			if (slotStack.getItem() instanceof BackpackItem) {
				subBackpacks.put(slot, BackpackWrapper.fromStack(slotStack));
			}
		}
	}
}
