package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedcore.inventory.ItemStackKey;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/** Allow or block by its filter slots, or match what the target inventory already holds. */
public class DepositFilterLogic extends FilterLogic {
	private static final String FILTER_BY_INVENTORY_TAG = "filterByInventory";
	private Set<ItemStackKey> inventoryFilterStacks = new HashSet<>();

	public DepositFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, String attributesKey) {
		super(upgrade, saveHandler, filterSlotCount, attributesKey);
	}

	public DepositFilterType getDepositFilterType() {
		if (shouldFilterByInventory()) {
			return DepositFilterType.INVENTORY;
		}
		return isAllowList() ? DepositFilterType.ALLOW : DepositFilterType.BLOCK;
	}

	public void setDepositFilterType(DepositFilterType depositFilterType) {
		switch (depositFilterType) {
			case ALLOW:
				setFilterByInventory(false);
				setAllowList(true);
				break;
			case BLOCK:
				setFilterByInventory(false);
				setAllowList(false);
				break;
			case INVENTORY:
			default:
				setFilterByInventory(true);
		}
	}

	public void setInventory(IItemHandler inventory) {
		inventoryFilterStacks = InventoryHelper.getUniqueStacks(inventory);
	}

	@Override
	public boolean matchesFilter(ItemStack stack) {
		if (!shouldFilterByInventory()) {
			return super.matchesFilter(stack);
		}

		for (ItemStackKey filterStack : inventoryFilterStacks) {
			if (stackMatchesFilter(stack, filterStack.getStack())) {
				return true;
			}
		}
		return false;
	}

	private void setFilterByInventory(boolean filterByInventory) {
		setBoolean(FILTER_BY_INVENTORY_TAG, filterByInventory);
	}

	private boolean shouldFilterByInventory() {
		return getBoolean(FILTER_BY_INVENTORY_TAG, false);
	}
}
