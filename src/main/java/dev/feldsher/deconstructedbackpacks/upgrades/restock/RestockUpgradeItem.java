package dev.feldsher.deconstructedbackpacks.upgrades.restock;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;

import java.util.function.IntSupplier;

public class RestockUpgradeItem extends UpgradeItemBase<RestockUpgradeWrapper> {
	private static final UpgradeType<RestockUpgradeWrapper> TYPE = new UpgradeType<>(RestockUpgradeWrapper::new);
	private final IntSupplier filterSlotCount;

	public RestockUpgradeItem(IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
		this.filterSlotCount = filterSlotCount;
	}

	@Override
	public UpgradeType<RestockUpgradeWrapper> getType() {
		return TYPE;
	}

	public int getFilterSlotCount() {
		return filterSlotCount.getAsInt();
	}
}
