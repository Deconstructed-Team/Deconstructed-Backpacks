package dev.feldsher.deconstructedbackpacks;

import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeGroup;
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

		@net.minecraftforge.common.config.Config.Comment("Leather Backpack Settings")
		public final BackpackConfig leatherBackpack = new BackpackConfig(27, 1);
		@net.minecraftforge.common.config.Config.Comment("Iron Backpack Settings")
		public final BackpackConfig ironBackpack = new BackpackConfig(54, 2);
		@net.minecraftforge.common.config.Config.Comment("Gold Backpack Settings")
		public final BackpackConfig goldBackpack = new BackpackConfig(81, 3);
		@net.minecraftforge.common.config.Config.Comment("Diamond Backpack Settings")
		public final BackpackConfig diamondBackpack = new BackpackConfig(108, 5);

		@net.minecraftforge.common.config.Config.Comment("Pickup Upgrade Settings")
		public final FilteredUpgradeConfig pickupUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Pickup Upgrade Settings")
		public final FilteredUpgradeConfig advancedPickupUpgrade = new FilteredUpgradeConfig(16, 4);

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

		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> disallowedItemsSet = null;
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
			maxUpgrades = null;
		}
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

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(DeconstructedBackpacks.MOD_ID)) {
			ConfigManager.sync(DeconstructedBackpacks.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
			SERVER.clearCache();
		}
	}
}
