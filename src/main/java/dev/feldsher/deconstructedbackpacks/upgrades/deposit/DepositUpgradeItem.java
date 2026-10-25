package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;

import java.util.function.IntSupplier;

public class DepositUpgradeItem extends UpgradeItemBase<DepositUpgradeWrapper> {
	private static final UpgradeType<DepositUpgradeWrapper> TYPE = new UpgradeType<>(DepositUpgradeWrapper::new);
	private final IntSupplier filterSlotCount;

	public DepositUpgradeItem(IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
		this.filterSlotCount = filterSlotCount;
	}

	@Override
	public UpgradeType<DepositUpgradeWrapper> getType() {
		return TYPE;
	}

	public int getFilterSlotCount() {
		return filterSlotCount.getAsInt();
	}
}
