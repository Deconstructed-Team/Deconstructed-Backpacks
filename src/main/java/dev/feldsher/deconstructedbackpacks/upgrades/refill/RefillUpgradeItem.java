package dev.feldsher.deconstructedbackpacks.upgrades.refill;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;

import java.util.function.IntSupplier;

/**
 * Keeps a stack of each item in its filter in the player's inventory, from the backpack. The advanced one refills each
 * into the slot chosen for it, and gives the player a block they pick that's only in the backpack.
 */
public class RefillUpgradeItem extends UpgradeItemBase<RefillUpgradeWrapper> {
	private static final UpgradeType<RefillUpgradeWrapper> TYPE = new UpgradeType<>(RefillUpgradeWrapper::new);
	private final IntSupplier filterSlotCount;
	private final boolean targetSlotSelection;
	private final boolean supportsBlockPick;

	public RefillUpgradeItem(IntSupplier filterSlotCount, boolean targetSlotSelection, boolean supportsBlockPick, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
		this.filterSlotCount = filterSlotCount;
		this.targetSlotSelection = targetSlotSelection;
		this.supportsBlockPick = supportsBlockPick;
	}

	@Override
	public UpgradeType<RefillUpgradeWrapper> getType() {
		return TYPE;
	}

	public boolean supportsBlockPick() {
		return supportsBlockPick;
	}

	public int getFilterSlotCount() {
		return filterSlotCount.getAsInt();
	}

	public boolean allowsTargetSlotSelection() {
		return targetSlotSelection;
	}
}
