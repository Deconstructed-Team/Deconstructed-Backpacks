package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedbackpacks.api.IItemHandlerInteractionUpgrade;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.FilteredItemHandler;
import dev.feldsher.deconstructedcore.upgrades.IFilteredUpgrade;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Puts what its filter matches from the backpack into the inventory the player interacts with. */
public class DepositUpgradeWrapper extends UpgradeWrapperBase<DepositUpgradeWrapper, DepositUpgradeItem>
		implements IFilteredUpgrade, IItemHandlerInteractionUpgrade {
	private final DepositFilterLogic filterLogic;

	public DepositUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(backpackWrapper, upgrade, upgradeSaveHandler);
		filterLogic = new DepositFilterLogic(upgrade, this.upgradeSaveHandler, upgradeItem.getFilterSlotCount(), "filter_attributes");
	}

	@Override
	public DepositFilterLogic getFilterLogic() {
		return filterLogic;
	}

	@Override
	public void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player) {
		if (filterLogic.getDepositFilterType() == DepositFilterType.INVENTORY) {
			filterLogic.setInventory(itemHandler);
		}
		AtomicInteger stacksAdded = new AtomicInteger(0);

		InventoryHelper.transfer(storageWrapper.getInventoryForUpgradeProcessing(),
				new FilteredItemHandler<>(itemHandler, Collections.singletonList(filterLogic), Collections.emptyList()),
				s -> stacksAdded.incrementAndGet());

		int stacksDeposited = stacksAdded.get();
		String translKey = stacksDeposited > 0 ? "gui.deconstructedbackpacks.status.stacks_deposited" : "gui.deconstructedbackpacks.status.nothing_to_deposit";
		player.sendStatusMessage(new TextComponentTranslation(translKey, stacksDeposited), true);
	}
}
