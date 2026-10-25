package dev.feldsher.deconstructedbackpacks.upgrades.anvil;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;

/** An anvil in the backpack's upgrade tab. */
public class AnvilUpgradeItem extends UpgradeItemBase<AnvilUpgradeWrapper> {
	private static final UpgradeType<AnvilUpgradeWrapper> TYPE = new UpgradeType<>(AnvilUpgradeWrapper::new);

	public AnvilUpgradeItem(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
	}

	@Override
	public UpgradeType<AnvilUpgradeWrapper> getType() {
		return TYPE;
	}
}
