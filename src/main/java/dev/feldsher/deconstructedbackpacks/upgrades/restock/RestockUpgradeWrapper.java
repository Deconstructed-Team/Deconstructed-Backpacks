package dev.feldsher.deconstructedbackpacks.upgrades.restock;

import dev.feldsher.deconstructedbackpacks.api.IItemHandlerInteractionUpgrade;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.FilteredItemHandler;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilterLogic;
import dev.feldsher.deconstructedcore.upgrades.IContentsFilteredUpgrade;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Takes what its filter matches from the inventory the player interacts with into the backpack. */
public class RestockUpgradeWrapper extends UpgradeWrapperBase<RestockUpgradeWrapper, RestockUpgradeItem>
		implements IContentsFilteredUpgrade, IItemHandlerInteractionUpgrade {
	private final ContentsFilterLogic filterLogic;

	public RestockUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(backpackWrapper, upgrade, upgradeSaveHandler);
		filterLogic = new ContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), backpackWrapper::getInventoryHandler, "filter_attributes");
	}

	@Override
	public ContentsFilterLogic getFilterLogic() {
		return filterLogic;
	}

	@Override
	public void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player) {
		AtomicInteger stacksAdded = new AtomicInteger(0);

		InventoryHelper.transfer(itemHandler,
				new FilteredItemHandler.Modifiable(storageWrapper.getInventoryForUpgradeProcessing(), Collections.singletonList(filterLogic), Collections.emptyList()),
				s -> stacksAdded.incrementAndGet());

		int stacksRestocked = stacksAdded.get();
		String translKey = stacksRestocked > 0 ? "gui.deconstructedbackpacks.status.stacks_restocked" : "gui.deconstructedbackpacks.status.nothing_to_restock";
		player.sendStatusMessage(new TextComponentTranslation(translKey, stacksRestocked), true);
	}
}
