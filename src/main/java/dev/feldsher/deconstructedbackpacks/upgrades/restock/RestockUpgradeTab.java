package dev.feldsher.deconstructedbackpacks.upgrades.restock;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.UpgradeSettingsTab;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinition;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilterControl;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilterType;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilteredUpgradeContainer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public abstract class RestockUpgradeTab extends UpgradeSettingsTab<ContentsFilteredUpgradeContainer<RestockUpgradeWrapper>> {
	protected ContentsFilterControl filterLogicControl;

	protected RestockUpgradeTab(ContentsFilteredUpgradeContainer<RestockUpgradeWrapper> upgradeContainer, Position position, StorageScreenBase<?> screen, ITextComponent tabLabel, ITextComponent closedTooltip) {
		super(upgradeContainer, position, screen, tabLabel, closedTooltip);
	}

	protected static ITextComponent tooltip(String upgradeName) {
		return new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey(upgradeName) + TranslationHelper.TOOLTIP_SUFFIX);
	}

	@Override
	protected void moveSlotsToTab() {
		filterLogicControl.moveSlotsToView();
	}

	public static class Basic extends RestockUpgradeTab {
		public Basic(ContentsFilteredUpgradeContainer<RestockUpgradeWrapper> upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsPerRow, ButtonDefinition.Toggle<ContentsFilterType> contentsFilterButton) {
			super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("restock"), tooltip("restock"));
			filterLogicControl = addHideableChild(new ContentsFilterControl.Basic(screen, new Position(x + 3, y + 24), getContainer().getFilterLogicContainer(),
					slotsPerRow, contentsFilterButton));
		}
	}

	public static class Advanced extends RestockUpgradeTab {
		public Advanced(ContentsFilteredUpgradeContainer<RestockUpgradeWrapper> upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsPerRow, ButtonDefinition.Toggle<ContentsFilterType> contentsFilterButton) {
			super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("advanced_restock"), tooltip("advanced_restock"));
			filterLogicControl = addHideableChild(new ContentsFilterControl.Advanced(screen, new Position(x + 3, y + 24), getContainer().getFilterLogicContainer(),
					slotsPerRow, contentsFilterButton));
		}
	}
}
