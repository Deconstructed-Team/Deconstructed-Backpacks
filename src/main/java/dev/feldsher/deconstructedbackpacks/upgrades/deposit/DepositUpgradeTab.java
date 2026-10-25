package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.UpgradeSettingsTab;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class DepositUpgradeTab extends UpgradeSettingsTab<DepositUpgradeContainer> {
	protected DepositFilterLogicControl filterLogicControl;

	protected DepositUpgradeTab(DepositUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, ITextComponent tabLabel, ITextComponent closedTooltip) {
		super(upgradeContainer, position, screen, tabLabel, closedTooltip);
	}

	protected static ITextComponent tooltip(String upgradeName) {
		return new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey(upgradeName) + TranslationHelper.TOOLTIP_SUFFIX);
	}

	@Override
	protected void moveSlotsToTab() {
		filterLogicControl.moveSlotsToView();
	}

	public static class Basic extends DepositUpgradeTab {
		public Basic(DepositUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsPerRow) {
			super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("deposit"), tooltip("deposit"));
			filterLogicControl = addHideableChild(new DepositFilterLogicControl.Basic(screen, new Position(x + 3, y + 24), getContainer().getFilterLogicContainer(), slotsPerRow));
		}
	}

	public static class Advanced extends DepositUpgradeTab {
		public Advanced(DepositUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsPerRow) {
			super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("advanced_deposit"), tooltip("advanced_deposit"));
			filterLogicControl = addHideableChild(new DepositFilterLogicControl.Advanced(screen, new Position(x + 3, y + 24), getContainer().getFilterLogicContainer(), slotsPerRow));
		}
	}
}
