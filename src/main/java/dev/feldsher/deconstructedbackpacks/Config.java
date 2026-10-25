package dev.feldsher.deconstructedbackpacks;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeGroup;
import dev.feldsher.deconstructedcore.upgrades.stack.StackUpgradeConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@net.minecraftforge.common.config.Config(modid = DeconstructedBackpacks.MOD_ID)
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class Config {
	@net.minecraftforge.common.config.Config.Comment("Server Settings")
	@net.minecraftforge.common.config.Config.Name("server")
	public static final Server SERVER = new Server();

	private Config() {}

	public static class Server {
		@net.minecraftforge.common.config.Config.Comment("List of items that are not allowed to be put in backpacks - e.g. \"minecraft:shulker_box\"")
		public String[] disallowedItems = new String[0];

		@net.minecraftforge.common.config.Config.Comment("Whether the copper backpack and the starter tier stack upgrade exist. Their recipes take copper from the ore dictionary (ingotCopper, blockCopper); in a pack with none, they're left without recipes.")
		@net.minecraftforge.common.config.Config.RequiresMcRestart
		public boolean copperTierEnabled = false;
		@net.minecraftforge.common.config.Config.Comment("Whether the netherite backpack exists. Its recipe takes netherite from the ore dictionary (ingotNetherite, as Future MC and Unseen's Nether Backport register); in a pack with none, it's left without a recipe.")
		@net.minecraftforge.common.config.Config.RequiresMcRestart
		public boolean netheriteTierEnabled = false;
		@net.minecraftforge.common.config.Config.Comment("With Future MC and its smithing table, the netherite backpack is made there, as in 1.21, instead of on the crafting table. False keeps it on the crafting table.")
		@net.minecraftforge.common.config.Config.RequiresMcRestart
		public boolean futureMcSmithingTable = true;

		@net.minecraftforge.common.config.Config.Comment("Leather Backpack Settings")
		public final BackpackConfig leatherBackpack = new BackpackConfig(27, 1);
		@net.minecraftforge.common.config.Config.Comment("Copper Backpack Settings")
		public final BackpackConfig copperBackpack = new BackpackConfig(45, 1);
		@net.minecraftforge.common.config.Config.Comment("Iron Backpack Settings")
		public final BackpackConfig ironBackpack = new BackpackConfig(54, 2);
		@net.minecraftforge.common.config.Config.Comment("Gold Backpack Settings")
		public final BackpackConfig goldBackpack = new BackpackConfig(81, 3);
		@net.minecraftforge.common.config.Config.Comment("Diamond Backpack Settings")
		public final BackpackConfig diamondBackpack = new BackpackConfig(108, 5);
		@net.minecraftforge.common.config.Config.Comment("Netherite Backpack Settings")
		public final BackpackConfig netheriteBackpack = new BackpackConfig(120, 7);

		@net.minecraftforge.common.config.Config.Comment("Pickup Upgrade Settings")
		public final FilteredUpgradeConfig pickupUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Pickup Upgrade Settings")
		public final FilteredUpgradeConfig advancedPickupUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Filter Upgrade Settings")
		public final FilteredUpgradeConfig filterUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Filter Upgrade Settings")
		public final FilteredUpgradeConfig advancedFilterUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Compacting Upgrade Settings")
		public final FilteredUpgradeConfig compactingUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Compacting Upgrade Settings")
		public final FilteredUpgradeConfig advancedCompactingUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Feeding Upgrade Settings")
		public final FilteredUpgradeConfig feedingUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Feeding Upgrade Settings")
		public final FilteredUpgradeConfig advancedFeedingUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Restock Upgrade Settings")
		public final FilteredUpgradeConfig restockUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Restock Upgrade Settings")
		public final FilteredUpgradeConfig advancedRestockUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Deposit Upgrade Settings")
		public final FilteredUpgradeConfig depositUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Deposit Upgrade Settings")
		public final FilteredUpgradeConfig advancedDepositUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Tool Swapper Upgrade Settings")
		public final FilteredUpgradeConfig toolSwapperUpgrade = new FilteredUpgradeConfig(8, 4);
		@net.minecraftforge.common.config.Config.Comment("Magnet Upgrade Settings")
		public final MagnetUpgradeConfig magnetUpgrade = new MagnetUpgradeConfig(9, 3, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Magnet Upgrade Settings")
		public final MagnetUpgradeConfig advancedMagnetUpgrade = new MagnetUpgradeConfig(16, 4, 5);
		@net.minecraftforge.common.config.Config.Comment("Void Upgrade Settings")
		public final VoidUpgradeConfig voidUpgrade = new VoidUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Void Upgrade Settings")
		public final VoidUpgradeConfig advancedVoidUpgrade = new VoidUpgradeConfig(16, 4);

		@net.minecraftforge.common.config.Config.Comment("Stack Upgrade Settings")
		public final StackUpgradeSettings stackUpgrade = new StackUpgradeSettings();

		@net.minecraftforge.common.config.Config.Comment("Maximum number of upgrades of type per backpack in format of \"UpgradeRegistryName[or UpgradeGroup]|MaxNumber\"")
		public String[] maxUpgradesPerStorage = {"stack_upgrades|3", "furnace_upgrades|1", "jukebox_upgrades|1"};

		@net.minecraftforge.common.config.Config.Ignore
		public final IUpgradeCountLimitConfig maxUpgradesPerStorageConfig = new IUpgradeCountLimitConfig() {
			@Override
			public int getMaxUpgradesPerStorage(String storageType, @Nullable ResourceLocation upgradeRegistryName) {
				return upgradeRegistryName == null ? Integer.MAX_VALUE : getMaxUpgrades().getOrDefault(upgradeRegistryName.getPath(), Integer.MAX_VALUE);
			}

			@Override
			public int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup upgradeGroup) {
				return getMaxUpgrades().getOrDefault(upgradeGroup.name(), Integer.MAX_VALUE);
			}
		};

		@net.minecraftforge.common.config.Config.Comment("List of blocks that inventory interaction upgrades can't interact with - e.g. \"minecraft:shulker_box\"")
		public String[] noInteractionBlocks = {};

		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> disallowedItemsSet = null;
		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> noInteractionBlocksSet = null;
		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Map<String, Integer> maxUpgrades = null;

		public boolean isItemDisallowed(Item item) {
			if (disallowedItemsSet == null) {
				disallowedItemsSet = new HashSet<>();
				for (String itemName : disallowedItems) {
					disallowedItemsSet.add(new ResourceLocation(itemName));
				}
			}
			return disallowedItemsSet.contains(item.getRegistryName());
		}

		public boolean isBlockInteractionDisallowed(Block block) {
			if (noInteractionBlocksSet == null) {
				noInteractionBlocksSet = new HashSet<>();
				for (String blockName : noInteractionBlocks) {
					noInteractionBlocksSet.add(new ResourceLocation(blockName));
				}
			}
			return noInteractionBlocksSet.contains(block.getRegistryName());
		}

		private Map<String, Integer> getMaxUpgrades() {
			if (maxUpgrades == null) {
				maxUpgrades = new HashMap<>();
				for (String mapping : maxUpgradesPerStorage) {
					String[] upgradeMax = mapping.split("\\|");
					if (upgradeMax.length < 2 || !upgradeMax[1].matches("\\d+")) {
						continue;
					}
					maxUpgrades.put(upgradeMax[0], Integer.parseInt(upgradeMax[1]));
				}
			}
			return maxUpgrades;
		}

		private void clearCache() {
			disallowedItemsSet = null;
			noInteractionBlocksSet = null;
			maxUpgrades = null;
			stackUpgrade.config.clearNonStackableItems();
		}
	}

	public static class StackUpgradeSettings {
		@net.minecraftforge.common.config.Config.Comment("List of items that are not supposed to stack in storage even when stack upgrade is inserted. Item registry names are expected here.")
		public String[] nonStackableItems = StackUpgradeConfig.getDefaultNonStackableList();

		@net.minecraftforge.common.config.Config.Ignore
		public final StackUpgradeConfig config = new StackUpgradeConfig(() -> nonStackableItems);
	}

	public static class BackpackConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of inventory slots in the backpack")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 144)
		public int inventorySlotCount;
		@net.minecraftforge.common.config.Config.Comment("Number of upgrade slots in the backpack")
		@net.minecraftforge.common.config.Config.RangeInt(min = 0, max = 10)
		public int upgradeSlotCount;

		BackpackConfig(int inventorySlotCountDefault, int upgradeSlotCountDefault) {
			inventorySlotCount = inventorySlotCountDefault;
			upgradeSlotCount = upgradeSlotCountDefault;
		}
	}

	public static class FilteredUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of the upgrade's filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow;

		FilteredUpgradeConfig(int defaultFilterSlots, int defaultSlotsInRow) {
			filterSlots = defaultFilterSlots;
			slotsInRow = defaultSlotsInRow;
		}
	}

	// Forge's config reads the fields a class declares, not inherited ones: each config class has all of its own

	public static class MagnetUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of the upgrade's filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow;
		@net.minecraftforge.common.config.Config.Comment("Range around storage in blocks at which magnet will pickup items")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int magnetRange;

		MagnetUpgradeConfig(int defaultFilterSlots, int defaultSlotsInRow, int defaultMagnetRange) {
			filterSlots = defaultFilterSlots;
			slotsInRow = defaultSlotsInRow;
			magnetRange = defaultMagnetRange;
		}
	}

	public static class VoidUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of the upgrade's filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow;
		@net.minecraftforge.common.config.Config.Comment("Determines whether void upgrade allows voiding anything or it only has overflow option")
		public boolean voidAnythingEnabled = true;

		VoidUpgradeConfig(int defaultFilterSlots, int defaultSlotsInRow) {
			filterSlots = defaultFilterSlots;
			slotsInRow = defaultSlotsInRow;
		}
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(DeconstructedBackpacks.MOD_ID)) {
			ConfigManager.sync(DeconstructedBackpacks.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
			SERVER.clearCache();
		}
	}
}
