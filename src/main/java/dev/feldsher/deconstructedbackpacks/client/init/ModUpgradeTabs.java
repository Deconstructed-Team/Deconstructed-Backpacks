package dev.feldsher.deconstructedbackpacks.client.init;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.client.gui.SBPButtonDefinitions;
import dev.feldsher.deconstructedbackpacks.init.ModUpgradeContainers;
import dev.feldsher.deconstructedcore.client.gui.UpgradeGuiManager;
import dev.feldsher.deconstructedcore.upgrades.filter.FilterUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.filter.FilterUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.magnet.MagnetUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.pickup.PickupUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.voiding.VoidUpgradeTab;

/** The settings tabs of the backpack upgrades. */
public class ModUpgradeTabs {
	private ModUpgradeTabs() {}

	public static void register() {
		UpgradeGuiManager.registerTab(ModUpgradeContainers.PICKUP_BASIC_TYPE, (uc, p, s) ->
				new PickupUpgradeTab.Basic(uc, p, s, Config.SERVER.pickupUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.PICKUP_ADVANCED_TYPE, (uc, p, s) ->
				new PickupUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedPickupUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(FilterUpgradeContainer.BASIC_TYPE, (uc, p, s) ->
				new FilterUpgradeTab.Basic(uc, p, s, Config.SERVER.filterUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(FilterUpgradeContainer.ADVANCED_TYPE, (uc, p, s) ->
				new FilterUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedFilterUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.MAGNET_BASIC_TYPE, (uc, p, s) ->
				new MagnetUpgradeTab.Basic(uc, p, s, Config.SERVER.magnetUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.MAGNET_ADVANCED_TYPE, (uc, p, s) ->
				new MagnetUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedMagnetUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.VOID_BASIC_TYPE, (uc, p, s) ->
				new VoidUpgradeTab.Basic(uc, p, s, Config.SERVER.voidUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.VOID_ADVANCED_TYPE, (uc, p, s) ->
				new VoidUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedVoidUpgrade.slotsInRow));
	}
}
