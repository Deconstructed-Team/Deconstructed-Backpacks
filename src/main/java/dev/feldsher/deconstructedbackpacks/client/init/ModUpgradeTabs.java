package dev.feldsher.deconstructedbackpacks.client.init;

import dev.feldsher.deconstructedcore.upgrades.battery.BatteryInventoryPart;
import dev.feldsher.deconstructedcore.upgrades.battery.BatteryUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.xppump.XpPumpUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.pump.PumpUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.tank.TankInventoryPart;
import dev.feldsher.deconstructedcore.upgrades.tank.TankUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.jukebox.JukeboxUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.cooking.AutoCookingUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.cooking.CookingUpgradeTab;
import dev.feldsher.deconstructedbackpacks.upgrades.smithing.SmithingUpgradeTab;
import dev.feldsher.deconstructedbackpacks.upgrades.anvil.AnvilUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.stonecutter.StonecutterUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.crafting.CraftingUpgradeTab;
import dev.feldsher.deconstructedbackpacks.upgrades.inception.InceptionUpgradeTab;
import dev.feldsher.deconstructedbackpacks.upgrades.refill.RefillUpgradeTab;
import dev.feldsher.deconstructedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.compacting.CompactingUpgradeTab;
import dev.feldsher.deconstructedcore.upgrades.feeding.FeedingUpgradeTab;
import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.client.gui.SBPButtonDefinitions;
import dev.feldsher.deconstructedbackpacks.init.ModUpgradeContainers;
import dev.feldsher.deconstructedbackpacks.upgrades.deposit.DepositUpgradeTab;
import dev.feldsher.deconstructedbackpacks.upgrades.restock.RestockUpgradeTab;
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
		UpgradeGuiManager.registerTab(ModUpgradeContainers.COMPACTING_BASIC_TYPE, (uc, p, s) ->
				new CompactingUpgradeTab.Basic(uc, p, s, Config.SERVER.compactingUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.COMPACTING_ADVANCED_TYPE, (uc, p, s) ->
				new CompactingUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedCompactingUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.FEEDING_BASIC_TYPE, (uc, p, s) ->
				new FeedingUpgradeTab.Basic(uc, p, s, Config.SERVER.feedingUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.FEEDING_ADVANCED_TYPE, (uc, p, s) ->
				new FeedingUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedFeedingUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.RESTOCK_BASIC_TYPE, (uc, p, s) ->
				new RestockUpgradeTab.Basic(uc, p, s, Config.SERVER.restockUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.RESTOCK_ADVANCED_TYPE, (uc, p, s) ->
				new RestockUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedRestockUpgrade.slotsInRow, SBPButtonDefinitions.BACKPACK_CONTENTS_FILTER_TYPE));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.DEPOSIT_BASIC_TYPE, (uc, p, s) ->
				new DepositUpgradeTab.Basic(uc, p, s, Config.SERVER.depositUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.DEPOSIT_ADVANCED_TYPE, (uc, p, s) ->
				new DepositUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedDepositUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.CRAFTING_TYPE, (uc, p, s) -> new CraftingUpgradeTab(uc, p, s, SBPButtonDefinitions.SHIFT_CLICK_TARGET));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.STONECUTTER_TYPE, (uc, p, s) -> new StonecutterUpgradeTab(uc, p, s, SBPButtonDefinitions.SHIFT_CLICK_TARGET));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.ANVIL_TYPE, AnvilUpgradeTab::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.SMITHING_TYPE, SmithingUpgradeTab::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.SMELTING_TYPE, (uc, p, s) -> new CookingUpgradeTab(uc, p, s, "smelting"));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.AUTO_SMELTING_TYPE, (uc, p, s) -> new AutoCookingUpgradeTab(uc, p, s, "auto_smelting",
				Config.SERVER.autoSmeltingUpgrade.inputFilterSlotsInRow, Config.SERVER.autoSmeltingUpgrade.fuelFilterSlotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.SMOKING_TYPE, (uc, p, s) -> new CookingUpgradeTab(uc, p, s, "smoking"));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.AUTO_SMOKING_TYPE, (uc, p, s) -> new AutoCookingUpgradeTab(uc, p, s, "auto_smoking",
				Config.SERVER.autoSmokingUpgrade.inputFilterSlotsInRow, Config.SERVER.autoSmokingUpgrade.fuelFilterSlotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.BLASTING_TYPE, (uc, p, s) -> new CookingUpgradeTab(uc, p, s, "blasting"));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.AUTO_BLASTING_TYPE, (uc, p, s) -> new AutoCookingUpgradeTab(uc, p, s, "auto_blasting",
				Config.SERVER.autoBlastingUpgrade.inputFilterSlotsInRow, Config.SERVER.autoBlastingUpgrade.fuelFilterSlotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.JUKEBOX_TYPE, JukeboxUpgradeTab.Basic::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.ADVANCED_JUKEBOX_TYPE, (uc, p, s) -> new JukeboxUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedJukeboxUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.TANK_TYPE, TankUpgradeTab::new);
		UpgradeGuiManager.registerInventoryPart(ModUpgradeContainers.TANK_TYPE, TankInventoryPart::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.PUMP_TYPE, PumpUpgradeTab.Basic::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.ADVANCED_PUMP_TYPE, PumpUpgradeTab.Advanced::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.XP_PUMP_TYPE, (uc, p, s) -> new XpPumpUpgradeTab(uc, p, s, Config.SERVER.xpPumpUpgrade.mendingOn));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.BATTERY_TYPE, BatteryUpgradeTab::new);
		UpgradeGuiManager.registerInventoryPart(ModUpgradeContainers.BATTERY_TYPE, BatteryInventoryPart::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.INCEPTION_TYPE, InceptionUpgradeTab::new);
		UpgradeGuiManager.registerTab(ModUpgradeContainers.REFILL_BASIC_TYPE, (uc, p, s) ->
				new RefillUpgradeTab.Basic(uc, p, s, Config.SERVER.refillUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.REFILL_ADVANCED_TYPE, (uc, p, s) ->
				new RefillUpgradeTab.Advanced(uc, p, s, Config.SERVER.advancedRefillUpgrade.slotsInRow));
		UpgradeGuiManager.registerTab(ModUpgradeContainers.TOOL_SWAPPER_TYPE, (uc, p, s) ->
				new ToolSwapperUpgradeTab(uc, p, s, Config.SERVER.toolSwapperUpgrade.slotsInRow));
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
