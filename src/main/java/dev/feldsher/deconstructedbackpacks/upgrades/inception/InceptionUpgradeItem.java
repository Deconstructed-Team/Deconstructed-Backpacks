package dev.feldsher.deconstructedbackpacks.upgrades.inception;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.common.gui.UpgradeSlotChangeResult;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lets backpacks without it into the backpack, and the backpack's upgrades use their inventories, and theirs work.
 * Only in a top level backpack, and it comes out only once no backpack is left in it.
 */
public class InceptionUpgradeItem extends UpgradeItemBase<InceptionUpgradeWrapper> {
	public static final UpgradeType<InceptionUpgradeWrapper> TYPE = new UpgradeType<>(InceptionUpgradeWrapper::new);
	private static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = Collections.singletonList(
			new UpgradeConflictDefinition(InceptionUpgradeItem.class::isInstance, 0, SBPTranslationHelper.INSTANCE.translError("add.inception_exists")));

	public InceptionUpgradeItem(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
	}

	@Override
	public UpgradeType<InceptionUpgradeWrapper> getType() {
		return TYPE;
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return UPGRADE_CONFLICT_DEFINITIONS;
	}

	@Override
	public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelStorage, boolean isClientSide) {
		UpgradeSlotChangeResult result = super.canAddUpgradeTo(storageWrapper, upgradeStack, firstLevelStorage, isClientSide);
		if (!result.successful()) {
			return result;
		}
		if (!firstLevelStorage) {
			return UpgradeSlotChangeResult.fail(SBPTranslationHelper.INSTANCE.translError("add.inception_sub_backpack"), Collections.emptySet(), Collections.emptySet());
		}
		return UpgradeSlotChangeResult.success();
	}

	@Override
	public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper, boolean isClientSide) {
		Set<Integer> slots = new HashSet<>();
		InventoryHandler inventory = storageWrapper.getInventoryHandler();
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			if (inventory.getStackInSlot(slot).getItem() instanceof BackpackItem) {
				slots.add(slot);
			}
		}
		if (!slots.isEmpty()) {
			return UpgradeSlotChangeResult.fail(SBPTranslationHelper.INSTANCE.translError("remove.inception_sub_backpack"), Collections.emptySet(), slots);
		}
		return UpgradeSlotChangeResult.success();
	}

	@Override
	public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, int upgradeSlot, IStorageWrapper storageWrapper, boolean isClientSide) {
		if (upgradeStackToPut.getItem() == this) {
			return UpgradeSlotChangeResult.success();
		}
		return canRemoveUpgradeFrom(storageWrapper, isClientSide);
	}
}
