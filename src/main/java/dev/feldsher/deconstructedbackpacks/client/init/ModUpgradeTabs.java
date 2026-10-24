package dev.feldsher.deconstructedbackpacks.client.init;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.client.gui.SBPButtonDefinitions;
import dev.feldsher.deconstructedbackpacks.init.ModUpgradeContainers;
import dev.feldsher.deconstructedcore.client.gui.UpgradeGuiManager;
import dev.feldsher.deconstructedcore.upgrades.pickup.PickupUpgradeTab;

/** The settings tabs of the backpack upgrades. */
public class ModUpgradeTabs {
	private ModUpgradeTabs() {}

	public static void register() {
		UpgradeGuiManager.registerTab(ModUpgradeContainers.PICKUP_BASIC_TYPE, (uc, p, s) ->
				new PickupUpgradeTab.Basic(uc, p, s, Config.SERVER.pickupUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.PICKUP_ADVANCED_TYPE, (uc, p, s) ->
				new PickupUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedPickupUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
	}
}
