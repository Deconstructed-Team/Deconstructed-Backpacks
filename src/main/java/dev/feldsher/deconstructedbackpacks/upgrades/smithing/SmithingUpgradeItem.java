package dev.feldsher.deconstructedbackpacks.upgrades.smithing;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;

/** A smithing table in the backpack's upgrade tab, with the recipes Future MC's smithing table has. */
public class SmithingUpgradeItem extends UpgradeItemBase<SmithingUpgradeWrapper> {
	private static final UpgradeType<SmithingUpgradeWrapper> TYPE = new UpgradeType<>(SmithingUpgradeWrapper::new);

	public SmithingUpgradeItem(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
	}

	@Override
	public UpgradeType<SmithingUpgradeWrapper> getType() {
		return TYPE;
	}
}
